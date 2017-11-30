package com.cduvvuri.sidb.common;

import com.cduvvuri.sidb.annotations.Field;
import com.cduvvuri.sidb.annotations.Key;

/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
@Key
public class SampleKey implements Comparable<SampleKey> {
	@Field(name = "id")
	private Integer id;

	public SampleKey() {

	}

	public SampleKey(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public int compareTo(SampleKey sk) {
		if (this.id < sk.id) {
			return -1;
		} else if (this.id > sk.id) {
			return 1;
		}

		return 0;
	}

	public String toString() {
		return String.valueOf(id);
	}
}
