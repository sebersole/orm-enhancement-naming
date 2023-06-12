/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.hibernate.bytecode.enhance.model.interp.internal.ModelSourceHelper;
import org.hibernate.bytecode.enhance.model.interp.spi.PersistentAttribute;
import org.hibernate.bytecode.enhance.model.source.spi.ClassDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MemberDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MethodDetails;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
public class SimpleEntityTests {
	@Test
	void testClassDetailsBuilding() {
		Helper.withProcessingContext( (processingContext) -> {
			final ClassDetails classDetails = processingContext
					.getClassDetailsRegistry()
					.resolveClassDetails( SimpleEntity.class.getName() );
			assertThat( classDetails ).isNotNull();
			assertThat( classDetails.getSuperType() ).isNull();

			final Access classLevelAccessAnn = classDetails.getAnnotation( Access.class );
			assertThat( classLevelAccessAnn ).isNull();

			// id, name
			assertThat( classDetails.getFields() ).hasSize( 2 );
			// getId, setId, getPrimaryName, setPrimaryName
			assertThat( classDetails.getMethods() ).hasSize( 7 );
			assertThat( collectGettersAndSetters( classDetails ) ).hasSize( 4 );

			final LinkedHashMap<String, MemberDetails> backingMembers = ModelSourceHelper.collectBackingMembers(
					classDetails,
					AccessType.PROPERTY,
					(s, fieldDetails) -> {},
					(s, methodDetails) -> {},
					(s, methodDetails) -> {}
			);

			assertThat( backingMembers ).hasSize( 2 );

			final AccessType determinedAccessType = ModelSourceHelper.determineClassLevelAccessType(
					classDetails,
					classDetails.getIdentifierMember(),
					null
			);
			assertThat( determinedAccessType ).isEqualTo( AccessType.FIELD );
		} );
	}

	private List<MethodDetails> collectGettersAndSetters(ClassDetails classDetails) {
		final ArrayList<MethodDetails> result = new ArrayList<>();
		for ( int i = 0; i < classDetails.getMethods().size(); i++ ) {
			final MethodDetails methodDetails = classDetails.getMethods().get( i );
			if ( methodDetails.getMethodKind() == MethodDetails.MethodKind.GETTER
					|| methodDetails.getMethodKind() == MethodDetails.MethodKind.SETTER ) {
				result.add( methodDetails );
			}
		}
		return result;
	}

	@Test
	void testPersistentAttributeResolution() {
		Helper.withManagedTypeModelContext( (managedTypeModelContext) -> {
			final ClassDetails classDetails = managedTypeModelContext
					.getModelProcessingContext()
					.getClassDetailsRegistry()
					.resolveClassDetails( SimpleEntity.class.getName() );
			final List<PersistentAttribute> persistentAttributes = ModelSourceHelper.buildPersistentAttributeList(
					classDetails,
					null,
					managedTypeModelContext
			);
			assertThat( persistentAttributes ).hasSize( 2 );
			assertThat( persistentAttributes.get( 0 ).getAccessType() ).isEqualTo( AccessType.FIELD );
			assertThat( persistentAttributes.get( 1 ).getAccessType() ).isEqualTo( AccessType.FIELD );
		} );
	}

	@Entity
	public static class SimpleEntity {
		@Id
		private Integer id;
		@Basic
		private String name;

		protected SimpleEntity() {
			// for Hibernate use
		}

		public SimpleEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "SimpleEntity{" +
					"id=" + id +
					", name='" + name + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			SimpleEntity that = (SimpleEntity) o;
			return Objects.equals( id, that.id ) && Objects.equals( name, that.name );
		}

		@Override
		public int hashCode() {
			return Objects.hash( id, name );
		}
	}
}
