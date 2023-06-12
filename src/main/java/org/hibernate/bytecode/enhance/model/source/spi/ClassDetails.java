package org.hibernate.bytecode.enhance.model.source.spi;

import java.util.List;

import net.bytebuddy.description.type.TypeDescription;

/**
 * @author Steve Ebersole
 */
public interface ClassDetails extends AnnotationTarget {
	@Override
	default Kind getKind() {
		return Kind.CLASS;
	}

	/**
	 * The name of the class.
	 * <p/>
	 * Generally this is the same as the {@linkplain #getClassName() class name}.
	 * But in the case of Hibernate's {@code entity-name} feature, this would
	 * be the {@code entity-name}
	 */
	String getName();

	/**
	 * The name of the {@link Class}, or {@code null} for dynamic models.
	 *
	 * @apiNote Will be {@code null} for dynamic models
	 */
	String getClassName();

	/**
	 * Whether the class should be considered abstract.
	 */
	boolean isAbstract();

	/**
	 * Details for the class that is the super type for this class.
	 */
	ClassDetails getSuperType();

	/**
	 * Get the fields for this class
	 */
	List<FieldDetails> getFields();

	/**
	 * Get the methods for this class
	 */
	List<MethodDetails> getMethods();

	/**
	 * Get the member on the class defined as the identifier, if one.  If multiple
	 * members are defined as identifier (non-aggregated), this returns a random one,
	 * although it is verified they all exist on the same "level" - i.e. all on
	 * {@linkplain AnnotationTarget.Kind#FIELD fields} or on
	 * {@linkplain AnnotationTarget.Kind#METHOD methods}.
	 */
	MemberDetails getIdentifierMember();

	boolean isImplementorOf(Class<?> checkType);

	default boolean isImplementorOf(TypeDescription.Generic checkType) {
		return isImplementorOf( checkType.asErasure() );
	}

	boolean isImplementorOf(TypeDescription checkType);
}
