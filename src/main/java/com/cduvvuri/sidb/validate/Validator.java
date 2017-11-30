package com.cduvvuri.sidb.validate;

/**
 * 
 * @author Chaitanya DS
 * 12-Nov-2017
 */
public interface Validator<T> {
	public Result validate(T t);

	public interface Result {
		public static Result OK = new SimpleResult(true, "");
		
		public boolean isOk();
		public String getMessage();
	}
	
	public class SimpleResult implements Result {		
		private String message;
		private boolean isOk;
				
		public SimpleResult(boolean isOk, String mess) {
			this.message = mess;
			this.isOk = isOk;
		}
		
		@Override
		public boolean isOk() {			
			return isOk;
		}

		@Override
		public String getMessage() {			
			return message;
		}
		
	}
}
