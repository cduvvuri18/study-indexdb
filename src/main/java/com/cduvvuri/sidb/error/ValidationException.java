package com.cduvvuri.sidb.error;

/**
 * 
 * @author Chaitanya DS
 * 08-Nov-2017
 * 
 */
public class ValidationException extends RuntimeException {
	private static final long serialVersionUID = -2209221617833800956L;
	
	public ValidationException() {
		super();
	}
	
	public ValidationException(String message) {
		super(message);
	}
	
	public ValidationException(String message, Throwable t) {
		super(message, t);
	}		

}
