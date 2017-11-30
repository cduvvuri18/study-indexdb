package com.cduvvuri.sidb.validate;

import java.lang.reflect.Field;

import com.cduvvuri.sidb.annotations.Entity;
import com.cduvvuri.sidb.fields.FieldType;

/**
 * 
 * @author Chaitanya DS
 * 12-Nov-2017
 */
public class EntityValidator<E> implements Validator<Class<E>> {
	public Result validate(Class<E> entity) {
		if(FieldType.FIELD_TYPES.containsKey(entity.getSimpleName())) {
			return new SimpleResult(true, "");
		}
		
		if (!entity.isAnnotationPresent(Entity.class)) {
			return new SimpleResult(false,
					"DBObject or POJO is expected to be annoted with 'com.sidb.annotations.Entity'");
		}

		Field[] fields = entity.getFields();

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
