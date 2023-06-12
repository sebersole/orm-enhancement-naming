/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.source.internal;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.bytecode.enhance.model.ByteBuddyModelException;
import org.hibernate.bytecode.enhance.model.source.spi.ClassDetails;
import org.hibernate.bytecode.enhance.model.source.spi.ModelProcessingContext;
import org.hibernate.bytecode.enhance.model.source.spi.ClassDetailsRegistry;

import net.bytebuddy.description.type.TypeDescription;

/**
 * @author Steve Ebersole
 */
public class ClassDetailsRegistryImpl implements ClassDetailsRegistry {
	private final ClassDetailsBuilderImpl classDetailsBuilder;
	private final ModelProcessingContext processingContext;
	private final Map<String, ClassDetails> registrations = new HashMap<>();

	public ClassDetailsRegistryImpl(ClassDetailsBuilderImpl classDetailsBuilder, ModelProcessingContext processingContext) {
		this.classDetailsBuilder = classDetailsBuilder;
		this.processingContext = processingContext;
	}

	@Override
	public ClassDetails findClassDetails(String name) {
		return registrations.get( name );
	}

	@Override
	public ClassDetails getClassDetails(String name) {
		final ClassDetails registration = findClassDetails( name );
		if ( registration == null ) {
			throw new ByteBuddyModelException( "No ClassDetails registration for `" + name + "`" );
		}
		return registration;
	}

	@Override
	public ClassDetails resolveClassDetails(String name) {
		ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassDetailsRegistry#resolveClassDetails(%s)", name );

		final ClassDetails existing = findClassDetails( name );
		if ( existing != null ) {
			return existing;
		}

		// NOTE: ClassDetails references register themselves to avoid stack overflow issues
		return classDetailsBuilder.buildClassDetails( name, processingContext );
	}

	public ClassDetails resolveClassDetails(String name, TypeDescription typeDescription) {
		ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassDetailsRegistry#resolveClassDetails(%s, %s) [TypeDescription]", name, typeDescription );

		final ClassDetails existing = findClassDetails( name );
		if ( existing != null ) {
			return existing;
		}

		final ClassDetails created = classDetailsBuilder.buildClassDetails( name, typeDescription, processingContext );
		registerClassDetails( name, created );
		return created;
	}

	/**
	 * Adds a class descriptor using the given {@code name} as the registration key
	 */
	public void addClassDetails(String name, ClassDetails classDetails) {
		ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassDetailsRegistry#addClassDetails(%s, %s) [ClassDetails]", name, classDetails );
		registerClassDetails( name, classDetails );
	}

	/**
	 * Adds a class descriptor using the given {@code name} as the registration key
	 */
	public void addClassDetails(String name, TypeDescription typeDescription) {
		ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassDetailsRegistry#addClassDetails(%s, %s) [TypeDescription]", name, typeDescription );
		registerClassDetails( name, classDetailsBuilder.buildClassDetails( name, typeDescription, processingContext ) );
	}

	private void registerClassDetails(String name, ClassDetails classDetails) {
		registrations.put( name, classDetails );
	}
}
