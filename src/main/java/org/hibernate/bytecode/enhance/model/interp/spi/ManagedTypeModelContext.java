/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.interp.spi;

import org.hibernate.bytecode.enhance.model.source.spi.ModelProcessingContext;

/**
 * @author Steve Ebersole
 */
public interface ManagedTypeModelContext {
	ManagedTypeDescriptorRegistry getDescriptorRegistry();
	ModelProcessingContext getModelProcessingContext();
}
