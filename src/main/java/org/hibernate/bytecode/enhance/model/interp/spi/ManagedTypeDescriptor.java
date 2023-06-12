/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.interp.spi;

import java.util.Map;

import org.hibernate.bytecode.enhance.model.source.spi.ClassDetails;

/**
 * @author Steve Ebersole
 */
public interface ManagedTypeDescriptor {
	ClassDetails getClassDetails();

	Map<String,PersistentAttribute> getPersistentAttributes();

	default PersistentAttribute getPersistentAttribute(String name) {
		return getPersistentAttributes().get( name );
	}
}
