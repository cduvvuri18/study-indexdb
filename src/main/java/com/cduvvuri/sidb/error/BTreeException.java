package com.cduvvuri.sidb.error;

/**
 * 
 * @author Chaitanya DS
 * 15-Nov-2017
 */
public class BTreeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BTreeException() {
		super();
	}
	
	public BTreeException(String message) {
		super(message);
	}
	
	public BTreeException(String message, Throwable t) {
		super(message, t);
	}		
}
