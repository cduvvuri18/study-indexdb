package com.cduvvuri.sidb.fields;

import java.lang.reflect.Field;

/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
public final class FieldTypeMetadata extends PrimTypeMetadata {
	public final String name;
	public final Field field;

	public FieldTypeMetadata(String name, FieldType type, Field field) {
		super(type);
		this.name = name;		
		this.field = field;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(name).append("|").append(fieldType.type).append("|")
				.append(fieldType.size).toString();//header is bit verbose, its OK....optimization/smart techniques later 
	}
}
