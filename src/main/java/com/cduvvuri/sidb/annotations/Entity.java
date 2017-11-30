package com.cduvvuri.sidb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Entity that encapsulates the key and fields
 * 
 * @author Chaitanya DS
 * 08-Nov-2017
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Entity {

}
