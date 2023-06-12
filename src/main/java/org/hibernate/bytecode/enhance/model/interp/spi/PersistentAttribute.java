/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.interp.spi;

import org.hibernate.bytecode.enhance.model.source.spi.FieldDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MemberDetails;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;

/**
 * Details about a persistent attribute
 *
 * @author Steve Ebersole
 */
public interface PersistentAttribute {
	/**
	 * The name of the persistent attribute
	 */
	String getName();

	/**
	 * The type of access for the attribute
	 */
	AccessType getAccessType();

	/**
	 * Whether the attribute explicitly specified the {@linkplain #getAccessType() access type}, i.e.
	 * whether the {@linkplain #getBackingMember() backing member} contains {@linkplain jakarta.persistence.Access @Access}
	 */
	default boolean isAccessTypeExplicit() {
		return getBackingMember().hasAnnotation( Access.class );
	}

	MemberDetails getBackingMember();

	FieldDetails getUnderlyingField();
}
