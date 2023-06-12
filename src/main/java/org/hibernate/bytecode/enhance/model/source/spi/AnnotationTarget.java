package org.hibernate.bytecode.enhance.model.source.spi;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.EnumSet;

/**
 * @author Steve Ebersole
 */
public interface AnnotationTarget {
	/**
	 * The kind of target
	 */
	Kind getKind();

	/**
	 * Whether this target defines an annotation of the given type
	 */
	<A extends Annotation> boolean hasAnnotation(Class<A> type);

	/**
	 * Get the annotation of the given type, or null.
	 */
	<A extends Annotation> A getAnnotation(Class<A> type);


	/**
	 * Subset of {@linkplain java.lang.annotation.ElementType annotation targets} supported for mapping annotations
	 */
	enum Kind {
		ANNOTATION( ElementType.ANNOTATION_TYPE ),
		CLASS( ElementType.TYPE ),
		FIELD( ElementType.FIELD ),
		METHOD( ElementType.METHOD ),
		PACKAGE( ElementType.PACKAGE );

		private final ElementType elementType;

		Kind(ElementType elementType) {
			this.elementType = elementType;
		}

		public ElementType getCorrespondingElementType() {
			return elementType;
		}

		public static EnumSet<Kind> from(Target target) {
			if ( target == null ) {
				return EnumSet.allOf( Kind.class );
			}
			return from( target.value() );
		}

		public static EnumSet<Kind> from(ElementType[] elementTypes) {
			final EnumSet<Kind> kinds = EnumSet.noneOf( Kind.class );
			final Kind[] values = values();
			for ( int i = 0; i < elementTypes.length; i++ ) {
				for ( int v = 0; v < values.length; v++ ) {
					if ( values[v].getCorrespondingElementType().equals( elementTypes[i] ) ) {
						kinds.add( values[v] );
					}
				}
			}
			return kinds;
		}

		public static Kind from(ElementType elementType) {
			final Kind[] values = values();
			for ( int i = 0; i < values.length; i++ ) {
				if ( values[i].getCorrespondingElementType().equals( elementType ) ) {
					return values[i];
				}
			}
			return null;
		}
	}
}
