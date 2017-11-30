package com.cduvvuri.sidb.persistent.btree;

import java.nio.ByteBuffer;
import java.util.List;

import com.cduvvuri.sidb.error.BTreeException;
import com.cduvvuri.sidb.fields.FieldTypeMetadata;
import com.cduvvuri.sidb.fields.PrimTypeMetadata;
import com.cduvvuri.sidb.fields.TypeRW;

/**
 * 
 * @author Chaitanya DS
 * 15-Nov-2017
 */
final class Tuple<K extends Comparable<K>, E> {
	K key;
	E e;

	public Tuple() {

	}

	public Tuple(K k, E e) {
		this.key = k;
		this.e = e;
	}

	//TODO - Try java8 MethodHandle
	//Buffer contains the data whose size equivalent to the record size
	final void write(ByteBuffer buffer, Header<K, E> header) {
		//Data in the tuple is organized in the sequence of field names in the header. 
		//Every field value prefixed with an extra byte that tells about its existence.
		//TODO The metadata of all data fields can be represented with 1 or 2 bytes depending on the number of bits
		switch (header.eType) {
		case PRIMITIVE:
			write(buffer, header.primKeyMetadata, key);
			break;
		default:
			write(buffer, header.keyMetadataLst, key);
			break;
		}

		switch (header.eType) {
		case PRIMITIVE:
			write(buffer, header.primEMetadata, e);
			break;
		default:
			write(buffer, header.fieldTypeMetadataLst, e);
			break;
		}
	}

	//Buffer contains the data equivalent to the record size	
	@SuppressWarnings("unchecked")
	final void read(ByteBuffer buffer, Header<K, E> header) {
		try {
			switch (header.eType) {
			case PRIMITIVE:
				this.key = (K) read(buffer, header.primKeyMetadata);
				break;
			default:
				key = header.keyClass.newInstance();
				read(buffer, header.keyMetadataLst, key);
				break;
			}

			switch (header.eType) {
			case PRIMITIVE:
				this.e = (E) read(buffer, header.primEMetadata);
				break;
			default:
				e = header.eClass.newInstance();
				read(buffer, header.fieldTypeMetadataLst, e);
				break;
			}
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new BTreeException(ex.getMessage(), ex);
		}
	}

	//In case of primitives we do not need to use reflection
	private Object read(ByteBuffer buffer, PrimTypeMetadata primKeyMetadata) {		
		TypeRW value = primKeyMetadata.fieldType.getTypeRWInstance();
		if (buffer.get() == 1)
			return value.read(buffer);
		else {
			return null;
		}
	}

	//In case of primitives we do not need to use reflection
	private void write(ByteBuffer buffer, PrimTypeMetadata primEMetadata, Object object) {
		TypeRW typeRW = primEMetadata.fieldType.getTypeRWInstance();
		if (object == null) {
			buffer.put((byte) 0);
		} else {
			buffer.put((byte) 1);
			typeRW.write(buffer, object);
		}
	}
	
	//Read the key/record data into buffer
	private void read(ByteBuffer buffer, List<FieldTypeMetadata> metadataLst, Object object) {
		metadataLst.forEach(metadata -> {
			try {
				TypeRW value = metadata.fieldType.getTypeRWInstance();
				if (buffer.get() == 1)
					value.read(buffer, metadata.field, object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new BTreeException(e.getMessage(), e);
			}
		});
	}

	//Write the key/record data into buffer
	private void write(ByteBuffer buffer, List<FieldTypeMetadata> metadataLst, Object object) {
		metadataLst.forEach(metadata -> {
			try {
				Object valueObj = metadata.field.get(object);
				TypeRW value = metadata.fieldType.getTypeRWInstance();
				if (valueObj == null) {
					buffer.put((byte) 0);
				} else {
					buffer.put((byte) 1);
					value.write(buffer, valueObj);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new BTreeException(e.getMessage(), e);
			}
		});
	}
}
