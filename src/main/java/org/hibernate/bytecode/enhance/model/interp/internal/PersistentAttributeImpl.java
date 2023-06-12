/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.interp.internal;

import org.hibernate.bytecode.enhance.model.interp.spi.PersistentAttribute;
import org.hibernate.bytecode.enhance.model.source.spi.FieldDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MemberDetails;
import org.hibernate.bytecode.enhance.model.source.spi.MethodDetails;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;

/**
 * @author Steve Ebersole
 */
public class PersistentAttributeImpl implements PersistentAttribute {
	private final String name;
	private final AccessType accessType;

	private final MemberDetails backingMember;
	private final FieldDetails underlyingField;

	public PersistentAttributeImpl(
			String name,
			AccessType accessType,
			MemberDetails backingMember,
			FieldDetails underlyingField) {
		this.name = name;
		this.accessType = accessType;
		this.backingMember = backingMember;
		this.underlyingField = underlyingField;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * The implicit or {@link #isAccessTypeExplicit() explicit} access-type for this attribute
	 */
	@Override
	public AccessType getAccessType() {
		return accessType;
	}

	@Override
	public MemberDetails getBackingMember() {
		return backingMember;
	}

	@Override
	public FieldDetails getUnderlyingField() {
		return underlyingField;
	}
}
