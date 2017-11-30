package com.cduvvuri.sidb.error;

/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
public class AVLException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AVLException() {
		super();
	}
	
	public AVLException(String message) {
		super(message);
	}
	
	public AVLException(String message, Throwable t) {
		super(message, t);
	}		
}
