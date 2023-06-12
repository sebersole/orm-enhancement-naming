package org.hibernate.bytecode.enhance.model.source.internal;

import java.io.IOException;

import net.bytebuddy.dynamic.ClassFileLocator;

/**
 * @author Steve Ebersole
 */
public class ClassFileLocatorImpl extends ClassFileLocator.ForClassLoader {
	// The name of the class to (possibly be) transformed.
	private String className;
	// The explicitly resolved Resolution for the class to (possibly be) transformed.
	private Resolution resolution;

	/**
	 * Creates a new class file locator for the given class loader.
	 *
	 * @param classLoader The class loader to query which must not be the bootstrap class loader, i.e. {@code null}.
	 */
	public ClassFileLocatorImpl(ClassLoader classLoader) {
		super( classLoader );
	}

	@Override
	public Resolution locate(String className) throws IOException {
		if ( ModelSourceLogging.MODEL_SOURCE_TRACE_ENABLED ) {
			ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassFileLocatorImpl#locate%s)", className );
		}
		if ( className.equals( this.className ) ) {
			return resolution;
		}
		else {
			return super.locate( className );
		}
	}

	void setClassNameAndBytes(String className, byte[] bytes) {
		if ( ModelSourceLogging.MODEL_SOURCE_TRACE_ENABLED ) {
			ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassFileLocatorImpl#setClassNameAndBytes%s)", className );
		}
		assert className != null;
		assert bytes != null;
		this.className = className;
		this.resolution = new Resolution.Explicit( bytes );
	}

	void setClassNameAndBytes(String className, Resolution resolution) {
		if ( ModelSourceLogging.MODEL_SOURCE_TRACE_ENABLED ) {
			ModelSourceLogging.MODEL_SOURCE_LOGGER.tracef( "ClassFileLocatorImpl#setClassNameAndBytes%s)", className );
		}
		assert className != null;
		assert resolution != null;
		this.className = className;
		this.resolution = resolution;
	}
}
