package org.hibernate.bytecode.enhance.model;

import org.hibernate.HibernateException;

/**
 * @author Steve Ebersole
 */
public class ByteBuddyModelException extends HibernateException {
	public ByteBuddyModelException(String message) {
		super( message );
	}

	public ByteBuddyModelException(String message, Throwable cause) {
		super( message, cause );
	}
}
