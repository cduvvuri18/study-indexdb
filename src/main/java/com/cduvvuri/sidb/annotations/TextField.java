package com.cduvvuri.sidb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is restricted to string type
 * @author Chaitanya DS
 * 11-Nov-2017
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TextField {
	int length() default 20;
	String name();
}
