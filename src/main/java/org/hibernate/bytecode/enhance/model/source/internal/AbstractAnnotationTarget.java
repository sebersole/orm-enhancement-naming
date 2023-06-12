package org.hibernate.bytecode.enhance.model.source.internal;

import java.lang.annotation.Annotation;

import org.hibernate.bytecode.enhance.model.source.spi.AnnotationTarget;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget {
	private final AnnotationList annotationAccess;

	public AbstractAnnotationTarget(AnnotationList annotationAccess) {
		this.annotationAccess = annotationAccess;
	}

	@Override
	public <A extends Annotation> boolean hasAnnotation(Class<A> type) {
		return annotationAccess.isAnnotationPresent( type );
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> type) {
		final AnnotationDescription.Loadable<A> reference = annotationAccess.ofType( type );
		if ( reference == null ) {
			return null;
		}
		return reference.load();
	}
}
