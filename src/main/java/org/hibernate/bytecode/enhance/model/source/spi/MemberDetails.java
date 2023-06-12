package org.hibernate.bytecode.enhance.model.source.spi;

/**
 * @author Steve Ebersole
 */
public interface MemberDetails extends AnnotationTarget {

	/**
	 * The name of the member.  This would be the name of the method or field.
	 */
	String getName();

	/**
	 * The field type or method return type
	 */
	ClassDetails getType();

	/**
	 * For members representing attributes, determine the name stem for methods related
	 * to it.  This is the name of the attribute with its first letter capitalized.
	 * <p/>
	 * For an attribute named {@code text}, the name stem would be {@code Text} as a base for the
	 * {@code getText} and {@code setText} method names
	 */
	String resolveAttributeMethodNameStem();

	/**
	 * For members representing attributes, determine the attribute name
	 */
	String resolveAttributeName();
}
