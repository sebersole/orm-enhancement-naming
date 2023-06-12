/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model;

import java.util.function.Consumer;

import org.hibernate.bytecode.enhance.model.interp.internal.ManagedTypeModelContextImpl;
import org.hibernate.bytecode.enhance.model.interp.spi.ManagedTypeModelContext;
import org.hibernate.bytecode.enhance.model.source.internal.ClassFileLocatorImpl;
import org.hibernate.bytecode.enhance.model.source.internal.ModelProcessingContextImpl;

import net.bytebuddy.pool.TypePool;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static void withProcessingContext(Consumer<ModelProcessingContextImpl> action) {
		withProcessingContext( Helper.class.getClassLoader(), action );
	}

	public static void withProcessingContext(ClassLoader classLoader, Consumer<ModelProcessingContextImpl> action) {
		try (ClassFileLocatorImpl classFileLocator = new ClassFileLocatorImpl( classLoader )) {
			final ModelProcessingContextImpl processingContext = new ModelProcessingContextImpl(
					classFileLocator,
					TypePool.Default.WithLazyResolution.of( classFileLocator )
			);

			action.accept( processingContext );
		}
	}

	public static void withManagedTypeModelContext(Consumer<ManagedTypeModelContext> action) {
		withManagedTypeModelContext( Helper.class.getClassLoader(), action );
	}

	public static void withManagedTypeModelContext(ClassLoader classLoader, Consumer<ManagedTypeModelContext> action) {
		try (ClassFileLocatorImpl classFileLocator = new ClassFileLocatorImpl( classLoader )) {
			final ModelProcessingContextImpl processingContext = new ModelProcessingContextImpl(
					classFileLocator,
					TypePool.Default.WithLazyResolution.of( classFileLocator )
			);
			action.accept( new ManagedTypeModelContextImpl( processingContext ) );
		}
	}
}
