package com.cduvvuri.sidb.common;

/**
 * 
 * @author Chaitanya DS
 * 15-Nov-2017
 */
public class SizeUtils {
	public static int sizeOf(int i) {
		return Integer.BYTES;
	}
	
	public static int sizeOf(boolean b) {
		return 1;//1 byte
	}

	public static int sizeOf(long l) {
		return Long.BYTES; 
	}
	
	/**
	 * TODO
	 * @param entity
	 * @return
	 */
	public static int sizeOfEntity(Class<?> entity) {
		return -1;
	}

	public static int sizeOf(byte[] fieldHeaderInBytes) {
		return fieldHeaderInBytes.length;
	}
}
