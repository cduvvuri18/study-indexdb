package com.cduvvuri.sidb.fields;

/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
public class PrimTypeMetadata {
	
	public final FieldType fieldType;
	
	public PrimTypeMetadata(FieldType type) {
		this.fieldType = type;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(fieldType.type).append("|")
				.append(fieldType.size).toString();//header is bit verbose, its OK....optimization/smart techniques later 
	}
}
