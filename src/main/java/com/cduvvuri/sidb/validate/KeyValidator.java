package com.cduvvuri.sidb.validate;

import java.lang.reflect.Field;

import com.cduvvuri.sidb.annotations.Key;
import com.cduvvuri.sidb.fields.FieldType;

/**
 * 
 * @author Chaitanya DS
 * 28-Nov-2017
 */
public class KeyValidator<K extends Comparable<K>> implements Validator<Class<K>> {
	public Result validate(Class<K> key) {
		if(FieldType.FIELD_TYPES.containsKey(key.getSimpleName())) {
			return new SimpleResult(true, "");
		}

		if (!key.isAnnotationPresent(Key.class)) {
			return new SimpleResult(false,
					"DBObject or POJO is expected to be annoted with 'com.sidb.annotations.Key'");
		}

		Field[] fields = key.getFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.isAnnotationPresent(com.cduvvuri.sidb.annotations.Field.class) && !field.getType().isPrimitive()) {
				return new SimpleResult(false,
						"'com.sidb.annotations.Field' is restricted to primitive types");
			}
			
			if (field.isAnnotationPresent(com.cduvvuri.sidb.annotations.TextField.class) && !field.getType().isAssignableFrom(String.class)) {
				return new SimpleResult(false,
						"'com.sidb.annotations.TextField' is restricted to string type");
			}
		}
		
		return new SimpleResult(true, "");
	}
}
