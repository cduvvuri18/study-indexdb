package com.cduvvuri.sidb.fields;

import java.util.HashMap;
import java.util.Map;

import com.cduvvuri.sidb.fields.TypeRW.*;

/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
public class FieldType {
	public final String type;
	public final int size;

	public FieldType(String type, int size) {
		this.type = type;
		this.size = size;
	}

	public TypeRW getTypeRWInstance() {
		switch (type) {
		case "Byte":
			return ByteTypeRW.getInstance();
		case "Short":
			return ShortTypeRW.getInstance();
		case "Integer":
			return IntTypeRW.getInstance();
		case "Long":
			return LongTypeRW.getInstance();
		case "Float":
			return FloatTypeRW.getInstance();
		case "Double":
			return DoubleTypeRW.getInstance();
		case "Boolean":
			return BooleanTypeRW.getInstance();
		case "Character":
			return CharTypeRW.getInstance();
		case "String":
			return StringTypeRW.getInstance();
		default:
			break;
		}
		return null;
	}
	
	public final static Map<String, FieldType> FIELD_TYPES = new HashMap<String, FieldType>();

	static {
		FIELD_TYPES.put("Byte", new FieldType("Byte", Byte.BYTES));
		FIELD_TYPES.put("Short", new FieldType("Short", Short.BYTES));
		FIELD_TYPES.put("Integer", new FieldType("Integer", Integer.BYTES));
		FIELD_TYPES.put("Long", new FieldType("Long", Long.BYTES));
		FIELD_TYPES.put("Float", new FieldType("Float", Float.BYTES));
		FIELD_TYPES.put("Double", new FieldType("Double", Double.BYTES));
		FIELD_TYPES.put("Boolean", new FieldType("Boolean", 1));
		FIELD_TYPES.put("Character", new FieldType("Character", Character.BYTES));
		FIELD_TYPES.put("String", null);
	}
}
