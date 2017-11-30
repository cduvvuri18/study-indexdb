package com.cduvvuri.sidb.common;

import com.cduvvuri.sidb.annotations.Entity;
import com.cduvvuri.sidb.annotations.Field;
import com.cduvvuri.sidb.annotations.TextField;

/**
 * 
 * @author Chaitanya DS
 * 08-Nov-2017
 */
@Entity
public class SampleEntity {		
	@Field(name = "field1")	
	private Integer field1;
	
	@TextField(name = "field2", length = 20)
	private String field2;
	
	public SampleEntity() {
		
	}
	
	public SampleEntity(Integer field1, String field2) {
		this.field1 =  field1;
		this.field2 = field2;
	}

	public Integer getField1() {
		return field1;
	}

	public void setField1(Integer field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}
}
