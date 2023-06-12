/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.interp.internal;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.hibernate.HibernateException;
import org.hibernate.bytecode.enhance.model.AccessTypePlacementException;
import org.hibernate.bytecode.enhance.model.ByteBuddyModelException;
import org.hibernate.bytecode.enhance.model.interp.spi.ManagedTypeDescriptor;
import org.hibernate.bytecode.enhance.model.interp.spi.ManagedTypeModelContext;
import org.hibernate.bytecode.enhance.model.interp.spi.PersistentAttribute;
import org.hibernate.bytecode.enhance.model.source.spi.AnnotationTarget;
import org.hibernate.bytecode.enhance.model.source.spi.ClassDetails;
import org.hibernate.bytecode.enhance.model.source.spi.FieldDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MemberDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MethodDetails;
import org.hibernate.internal.util.collections.CollectionHelper;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Transient;
import net.bytebuddy.dynamic.ClassFileLocator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.hibernate.bytecode.enhance.model.source.internal.ModelSourceLogging.MODEL_SOURCE_LOGGER;
import static org.hibernate.internal.util.collections.CollectionHelper.arrayList;

/**
 * @author Steve Ebersole
 */
public class ModelSourceHelper {
	public static ManagedTypeDescriptor buildManagedTypeDescriptor(
			ClassDetails declaringType,
			AccessType contextAccessType,
			ManagedTypeModelContext processingContext) {
		return new ManagedTypeDescriptorImpl(
				declaringType,
				buildPersistentAttributeList( declaringType, contextAccessType, processingContext ),
				processingContext
		);
	}
	public static List<PersistentAttribute> buildPersistentAttributeList(
			ClassDetails declaringType,
			AccessType contextAccessType,
			ManagedTypeModelContext processingContext) {
		final AccessType classLevelAccessType = determineClassLevelAccessType(
				declaringType,
				declaringType.getIdentifierMember(),
				contextAccessType
		);

		MODEL_SOURCE_LOGGER.debugf( "Building PersistentAttribute list : %s;  class-level access : %s", declaringType.getName(), classLevelAccessType );

		// Categorize all the members
		final LinkedHashMap<String,FieldDetails> allFields = new LinkedHashMap<>();
		final LinkedHashMap<String,FieldDetails> backingFields = new LinkedHashMap<>();
		final LinkedHashMap<String,MethodDetails> backingGetters = new LinkedHashMap<>();
		categorizeMembers(
				declaringType,
				classLevelAccessType,
				allFields::put,
				(attributeName, backingField) -> {
					final MemberDetails previous = backingFields.put(
							backingField.resolveAttributeName(),
							backingField
					);
					if ( previous != null && previous != backingField) {
						throw new HibernateException( "Multiple backing members found : " + backingField.resolveAttributeName() );
					}
				},
				(attributeName, backingMethod) -> {
					final MemberDetails previous = backingGetters.put(
							backingMethod.resolveAttributeName(),
							backingMethod
					);
					if ( previous != null && previous != backingMethod ) {
						throw new HibernateException( "Multiple backing members found : " + backingMethod.resolveAttributeName() );
					}
				}
		);

		final List<PersistentAttribute> attributes = arrayList( backingGetters.size() + backingFields.size() );
		processBackingGetters( backingGetters, backingFields, allFields, attributes::add, declaringType, processingContext );
		processBackingFields( backingFields, attributes::add, declaringType, processingContext );

		return attributes;
	}

	@FunctionalInterface
	private interface PersistentAttributeConsumer {

		void accept(PersistentAttribute attribute);
	}

	private static void processBackingGetters(
			Map<String, MethodDetails> backingGetters,
			Map<String, FieldDetails> backingFields,
			Map<String,FieldDetails> allFields,
			PersistentAttributeConsumer attributeCollector,
			ClassDetails declaringType,
			ManagedTypeModelContext processingContext) {
		if ( backingGetters.isEmpty() ) {
			return;
		}

		final AsmModelNodesRef asmModelNodesRef = new AsmModelNodesRef( declaringType, processingContext );

		backingGetters.forEach( (attributeName, getterDetails) -> {
			final FieldDetails underlyingField = determineGottenField(
					getterDetails,
					backingFields,
					allFields,
					asmModelNodesRef,
					processingContext
			);

			attributeCollector.accept( new PersistentAttributeImpl(
					attributeName,
					AccessType.PROPERTY,
					getterDetails,
					underlyingField
			) );
		} );
	}

	private static void processBackingFields(
			LinkedHashMap<String, FieldDetails> backingFields,
			PersistentAttributeConsumer attributeCollector,
			ClassDetails declaringType,
			ManagedTypeModelContext processingContext) {
		if ( backingFields.isEmpty() ) {
			return;
		}

		backingFields.forEach( (attributeName, fieldDetails) -> {
			attributeCollector.accept( new PersistentAttributeImpl(
					attributeName,
					AccessType.FIELD,
					fieldDetails,
					fieldDetails
			) );
		} );
	}

	private static FieldDetails determineGottenField(
			MethodDetails getterDetails,
			Map<String,FieldDetails> backingFields,
			Map<String,FieldDetails> allFields,
			AsmModelNodesRef asmModelNodesRef,
			ManagedTypeModelContext processingContext) {
		final FieldDetails simpleMatch = allFields.get( getterDetails.getSimpleMatchFieldName() );
		if ( simpleMatch != null ) {
			return simpleMatch;
		}

		// we need to dig a littler deeper and look at the bytecode instructions
		final Map<String, MethodNode> getterMethodNodeMap = asmModelNodesRef.getGetterMethodNodeMap();
		final MethodNode methodNode = getterMethodNodeMap.get( getterDetails.getName() );

		// assume the final GETFIELD instruction is the backing field
		FieldDetails returnedField = null;
		for ( int i = methodNode.instructions.size() - 1; i >= 0; i-- ) {
			final AbstractInsnNode instruction = methodNode.instructions.get( i );
			if ( instruction.getOpcode() == Opcodes.GETFIELD ) {
				final FieldInsnNode getFieldInstruction = (FieldInsnNode) instruction;
				final String returnedFieldName = getFieldInstruction.name;
				returnedField = allFields.get( returnedFieldName );
				break;
			}
		}

		if ( returnedField != null ) {
			backingFields.remove( returnedField.resolveAttributeName() );
			return returnedField;
		}

		throw new ByteBuddyModelException( "Could not locate underlying field : " + getterDetails.getName() );
	}

	private static class AsmModelNodesRef {
		private final ClassDetails declaringType;
		private final ManagedTypeModelContext processingContext;

		private ClassNode classNode = null;
		private Map<String, MethodNode> getterMethodNodeMap = null;

		private AsmModelNodesRef(ClassDetails declaringType, ManagedTypeModelContext processingContext) {
			this.declaringType = declaringType;
			this.processingContext = processingContext;
		}

		public ClassNode getClassNode() {
			if ( classNode == null ) {
				classNode = buildClassNode( declaringType, processingContext );
			}
			return classNode;
		}

		public Map<String, MethodNode> getGetterMethodNodeMap() {
			if ( getterMethodNodeMap == null ) {
				getterMethodNodeMap = extractGetterMethodNodeMap( declaringType, getClassNode(), processingContext );
			}
			return getterMethodNodeMap;
		}
	}

	/**
	 * Accepts a {@linkplain MemberDetails member} which is the backing for a persistent attribute.
	 *
	 * @param <M> The specific type accepted
	 */
	public interface BackingMemberConsumer<M extends MemberDetails> {
		void accept(String attributeName, M memberDetails);
	}

	/**
	 * Collects the members (field or getter) which is "backing" the attribute, keyed by the attribute name
	 *
	 * @param declaringType The type descriptor from which to access field and method descriptors
	 * @param classLevelAccessType The implicit access type in effect for the declaring type
	 * @param backingFieldCollector Collects {@linkplain FieldDetails fields} which define an attribute
	 * @param backingGetterCollector Collects {@linkplain MethodDetails getters} which define an attribute
	 */
	public static void categorizeMembers(
			ClassDetails declaringType,
			AccessType classLevelAccessType,
			BiConsumer<String,FieldDetails> allFieldsCollector,
			BackingMemberConsumer<FieldDetails> backingFieldCollector,
			BackingMemberConsumer<MethodDetails> backingGetterCollector) {
		assert classLevelAccessType != null;

		for ( int i = 0; i < declaringType.getFields().size(); i++ ) {
			final FieldDetails fieldDetails = declaringType.getFields().get( i );
			allFieldsCollector.accept( fieldDetails.getName(), fieldDetails );

			if ( fieldDetails.hasAnnotation( Transient.class ) ) {
				continue;
			}

			final Access localAccess = fieldDetails.getAnnotation( Access.class );
			if ( localAccess != null ) {
				// the field contained `@Access`
				validateAttributeLevelAccess( fieldDetails, localAccess.value(), declaringType );

				if ( localAccess.value() == AccessType.FIELD ) {
					backingFieldCollector.accept( fieldDetails.resolveAttributeName(), fieldDetails );
				}
			}
			else if ( classLevelAccessType == AccessType.FIELD ) {
				backingFieldCollector.accept( fieldDetails.resolveAttributeName(), fieldDetails );
			}
		}

		for ( int i = 0; i < declaringType.getMethods().size(); i++ ) {
			final MethodDetails methodDetails = declaringType.getMethods().get( i );
			if ( methodDetails.getMethodKind() != MethodDetails.MethodKind.GETTER ) {
				continue;
			}

			if ( methodDetails.hasAnnotation( Transient.class ) ) {
				continue;
			}

			final Access localAccess = methodDetails.getAnnotation( Access.class );
			if ( localAccess != null ) {
				// the method contained `@Access`
				validateAttributeLevelAccess( methodDetails, localAccess.value(), declaringType );

				if ( localAccess.value() == AccessType.FIELD ) {
					backingGetterCollector.accept( methodDetails.resolveAttributeName(), methodDetails );
				}
			}
			else if ( classLevelAccessType == AccessType.PROPERTY ) {
				backingGetterCollector.accept( methodDetails.resolveAttributeName(), methodDetails );
			}
		}
	}

	private static ClassNode buildClassNode(ClassDetails declaringType, ManagedTypeModelContext processingContext) {
		try {
			final ClassNode classNode = new ClassNode();
			final ClassFileLocator.Resolution resolution = processingContext
					.getModelProcessingContext()
					.getClassFileLocator()
					.locate( declaringType.getClassName() );
			final ClassReader classReader = new ClassReader( resolution.resolve() );
			classReader.accept( classNode, ClassReader.SKIP_DEBUG );
			return classNode;
		}
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

	private static Map<String,MethodNode> extractGetterMethodNodeMap(
			ClassDetails declaringType,
			ClassNode classNode,
			ManagedTypeModelContext processingContext) {
		final LinkedHashMap<String, MethodNode> getterNodeMap = new LinkedHashMap<>();
		for ( int i = 0; i < classNode.methods.size(); i++ ) {
			final MethodNode methodNode = classNode.methods.get( i );
			if ( CollectionHelper.isNotEmpty( methodNode.parameters ) ) {
				//can't be a getter - has arguments
				continue;
			}

			if ( !methodNode.name.startsWith( "get" ) && !methodNode .name.startsWith( "is" ) ) {
				// can't be a getter - name does not match get/is
				continue;
			}

			getterNodeMap.put( methodNode.name, methodNode );
		}

		return getterNodeMap;
	}

	public static AccessType determineClassLevelAccessType(
			ClassDetails declaringType,
			MemberDetails identifierMember,
			AccessType contextAccessType) {
		final Access annotation = declaringType.getAnnotation( Access.class );
		if ( annotation != null ) {
			return annotation.value();
		}

		if ( declaringType.getSuperType() != null ) {
			final AccessType accessType = determineClassLevelAccessType(
					declaringType.getSuperType(),
					declaringType.getIdentifierMember(),
					null
			);
			if ( accessType != null ) {
				return accessType;
			}
		}

		if ( identifierMember != null ) {
			return identifierMember.getKind() == AnnotationTarget.Kind.FIELD
					? AccessType.FIELD
					: AccessType.PROPERTY;
		}

		return contextAccessType == null ? AccessType.PROPERTY : contextAccessType;
	}

	private static void validateAttributeLevelAccess(
			MemberDetails annotationTarget,
			AccessType attributeAccessType,
			ClassDetails classDetails) {
		// Apply the checks defined in section `2.3.2 Explicit Access Type` of the persistence specification

		// Mainly, it is never legal to:
		//		1. specify @Access(FIELD) on a getter
		//		2. specify @Access(PROPERTY) on a field

		if ( ( attributeAccessType == AccessType.FIELD && annotationTarget.getKind() != AnnotationTarget.Kind.FIELD )
				|| ( attributeAccessType == AccessType.PROPERTY && annotationTarget.getKind() != AnnotationTarget.Kind.METHOD ) ) {
			throw new AccessTypePlacementException( classDetails, annotationTarget );
		}
	}

}
