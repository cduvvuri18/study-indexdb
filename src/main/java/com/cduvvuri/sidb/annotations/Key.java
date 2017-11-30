package com.cduvvuri.sidb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is restricted to primitives.. for now 
 * @author Chaitanya DS
 * 08-Nov-2017
 * 
 * Fields represent the record associated with the key for each entity
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Key {
	
}
