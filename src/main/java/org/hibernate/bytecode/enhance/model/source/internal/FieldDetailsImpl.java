/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.model.source.internal;

import java.util.Locale;

import org.hibernate.bytecode.enhance.model.source.spi.ClassDetails;
import org.hibernate.bytecode.enhance.model.source.spi.FieldDetails;

import net.bytebuddy.description.field.FieldDescription;

import static org.hibernate.bytecode.enhance.model.source.internal.ModelSourceLogging.MODEL_SOURCE_LOGGER;
import static org.hibernate.internal.util.StringHelper.isEmpty;

/**
 * @author Steve Ebersole
 */
public class FieldDetailsImpl extends AbstractAnnotationTarget implements FieldDetails {
	private final FieldDescription fieldDescriptor;
	private final ClassDetails type;

	private final String methodNameStem;
	private final String toString;

	public FieldDetailsImpl(FieldDescription fieldDescriptor, ClassDetails type) {
		super( fieldDescriptor.getDeclaredAnnotations() );

		MODEL_SOURCE_LOGGER.debugf( "Creating FieldDetails(%s#%s)", fieldDescriptor.getDeclaringType().getActualName(), fieldDescriptor.getName() );

		this.fieldDescriptor = fieldDescriptor;
		this.type = type;

		this.toString = String.format(
				Locale.ROOT,
				"MethodDetails(%s#%s : %s)",
				fieldDescriptor.getDeclaringType().getActualName(),
				fieldDescriptor.getName(),
				type.getName()
		);
		this.methodNameStem = capitalizeFirst( fieldDescriptor.getName() );
	}

	private String capitalizeFirst(String text) {
		if ( isEmpty( text ) ) {
			return null;
		}

		return Character.isUpperCase( text.charAt( 0 ) )
				+ text.substring( 1, text.length() - 1 );
	}

	@Override
	public String getName() {
		return fieldDescriptor.getName();
	}

	@Override
	public ClassDetails getType() {
		return type;
	}

	@Override
	public String resolveAttributeMethodNameStem() {
		return methodNameStem;
	}

	@Override
	public String toString() {
		return toString;
	}
}
