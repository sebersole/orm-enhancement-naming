package org.hibernate.bytecode.enhance.model;

import org.hibernate.bytecode.enhance.model.interp.internal.ManagedTypeModelContextImpl;
import org.hibernate.bytecode.enhance.model.interp.spi.ManagedTypeDescriptor;
import org.hibernate.bytecode.enhance.model.interp.spi.PersistentAttribute;
import org.hibernate.bytecode.enhance.model.source.internal.ClassFileLocatorImpl;
import org.hibernate.bytecode.enhance.model.source.internal.ModelProcessingContextImpl;
import org.hibernate.bytecode.enhance.model.source.spi.FieldDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MethodDetails;

import org.junit.jupiter.api.Test;

import jakarta.persistence.AccessType;
import net.bytebuddy.pool.TypePool;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
public class BasicTests {

	@Test
	void testSimpleFieldEntityProcessing() {
		final ClassFileLocatorImpl classFileLocator = new ClassFileLocatorImpl( getClass().getClassLoader() );
		final TypePool typePool = TypePool.Default.WithLazyResolution.of( classFileLocator );

		final ModelProcessingContextImpl modelProcessingContext = new ModelProcessingContextImpl(
				classFileLocator,
				typePool
		);

		final ManagedTypeModelContextImpl managedTypeModelContext = new ManagedTypeModelContextImpl( modelProcessingContext );
		final ManagedTypeDescriptor managedTypeDescriptor = managedTypeModelContext
				.getDescriptorRegistry()
				.resolveDescriptor( "org.hibernate.bytecode.enhance.model.SimpleFieldEntity" );

		assertThat( managedTypeDescriptor ).isNotNull();
		assertThat( managedTypeDescriptor.getPersistentAttributes() ).hasSize( 2 );

		final PersistentAttribute idAttribute = managedTypeDescriptor.getPersistentAttribute( "id" );
		assertThat( idAttribute ).isNotNull();
		assertThat( idAttribute.getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( idAttribute.getBackingMember() ).isInstanceOf( FieldDetails.class );
		assertThat( idAttribute.getUnderlyingField() ).isNotNull();
		assertThat( idAttribute.getBackingMember() ).isSameAs( idAttribute.getUnderlyingField() );

		final PersistentAttribute nameAttribute = managedTypeDescriptor.getPersistentAttribute( "name" );
		assertThat( nameAttribute ).isNotNull();
		assertThat( nameAttribute.getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( nameAttribute.getBackingMember() ).isInstanceOf( FieldDetails.class );
		assertThat( nameAttribute.getUnderlyingField() ).isNotNull();
		assertThat( nameAttribute.getBackingMember() ).isSameAs( nameAttribute.getUnderlyingField() );
	}

	@Test
	void testSimplePropertyEntityProcessing() {
		final ClassFileLocatorImpl classFileLocator = new ClassFileLocatorImpl( getClass().getClassLoader() );
		final TypePool typePool = TypePool.Default.WithLazyResolution.of( classFileLocator );

		final ModelProcessingContextImpl modelProcessingContext = new ModelProcessingContextImpl(
				classFileLocator,
				typePool
		);

		final ManagedTypeModelContextImpl managedTypeModelContext = new ManagedTypeModelContextImpl( modelProcessingContext );
		final ManagedTypeDescriptor managedTypeDescriptor = managedTypeModelContext
				.getDescriptorRegistry()
				.resolveDescriptor( "org.hibernate.bytecode.enhance.model.SimplePropertyEntity" );

		assertThat( managedTypeDescriptor ).isNotNull();
		assertThat( managedTypeDescriptor.getPersistentAttributes() ).hasSize( 2 );

		final PersistentAttribute idAttribute = managedTypeDescriptor.getPersistentAttribute( "id" );
		assertThat( idAttribute ).isNotNull();
		assertThat( idAttribute.getAccessType() ).isEqualTo( AccessType.PROPERTY );
		assertThat( idAttribute.getBackingMember() ).isInstanceOf( MethodDetails.class );
		assertThat( idAttribute.getUnderlyingField() ).isNotNull();

		final PersistentAttribute nameAttribute = managedTypeDescriptor.getPersistentAttribute( "name" );
		assertThat( nameAttribute ).isNotNull();
		assertThat( nameAttribute.getAccessType() ).isEqualTo( AccessType.PROPERTY );
		assertThat( nameAttribute.getBackingMember() ).isInstanceOf( MethodDetails.class );
		assertThat( nameAttribute.getUnderlyingField() ).isNotNull();
	}
}
