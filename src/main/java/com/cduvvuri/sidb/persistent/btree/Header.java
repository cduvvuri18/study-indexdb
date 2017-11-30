package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cduvvuri.sidb.annotations.TextField;
import com.cduvvuri.sidb.common.Constatnts;
import com.cduvvuri.sidb.common.SizeUtils;
import com.cduvvuri.sidb.error.BTreeException;
import com.cduvvuri.sidb.error.ValidationException;
import com.cduvvuri.sidb.fields.FieldType;
import com.cduvvuri.sidb.fields.FieldTypeMetadata;
import com.cduvvuri.sidb.fields.PrimTypeMetadata;

/**
 * 
 * @author Chaitanya DS
 * 14-Nov-2017
 */
//Do not see any reason to expose Header outside the package(for now). Will revisit while implementing bplus.
//rootPos,keySize,recSize,order,keyHeaderLength,keyHeader,fieldHeaderLength,fieldHeader
final class Header<K extends Comparable<K>, E> {
	//Header variables. All these variables go into index file as is one after other.
	long rootPos;
	int keySize;
	int recSize;
	int order;
	int minOrder;
	int keyHeaderLen;
	int fieldHeaderLen;
	byte[] fieldHeaderInBytes;
	byte[] keyHeaderInBytes;
	Class<K> keyClass;
	Class<E> eClass;
	int nodeSize = Constatnts.BLOCK_SIZE_IN_KB * 1024;

	enum KEType {
		DEFAULT, PRIMITIVE
	}

	KEType keyType = KEType.DEFAULT;
	KEType eType = KEType.DEFAULT;

	//Ancillary/helper/logical variables. whatever you call.
	List<FieldTypeMetadata> fieldTypeMetadataLst = null;
	List<FieldTypeMetadata> keyMetadataLst = null;

	PrimTypeMetadata primKeyMetadata;
	PrimTypeMetadata primEMetadata;

	StringBuilder fieldHeader = new StringBuilder();
	StringBuilder keyHeader = new StringBuilder();

	//Flat the complex entity and key
	Header(Class<K> key, Class<E> entity) {
		this.keyClass = key;
		this.eClass = entity;

		//If the key type is Primitive
		if (FieldType.FIELD_TYPES.containsKey(key.getSimpleName())) {
			this.keyType = KEType.PRIMITIVE;
			makePrimKeyMetadata();
		} else
			makeKeyMetadata();

		if (FieldType.FIELD_TYPES.containsKey(entity.getSimpleName())) {
			this.eType = KEType.PRIMITIVE;
			makePrimEntityMetadata();
		} else
			makeEntityMetadata();

		//set the field header length
		setFieldHeaderLength();
		setKeyHeaderLength();

		//compute the order
		computeOrder();

		//Finally set the root pos
		setRootPos();

		//sort FieldTypeMetadataLst by name
	}

	/**
	 * If the key type is Primitive
	 */
	private void makePrimKeyMetadata() {
		this.primKeyMetadata = makePrimTypeMetada(this.keyClass.getSimpleName());
		
		//update field header
		updateKeyHeader(this.primKeyMetadata);
		updateKeySize(this.primKeyMetadata.fieldType.size);
		//number of fields. In a compound key it will have more than 1
		//1 byte for the metadata of each key field
		//TODO - using the binary string we can represent the metadata with 1 byte for all the fields
		updateKeySize(1);
	}

	/**
	 * If the key is first class java object
	 */
	private void makeKeyMetadata() {
		this.keyMetadataLst = new ArrayList<FieldTypeMetadata>();
		
		makeKeyMetadaLst();
		updateKeySize(keyMetadataLst.size());
	}

	/**
	 * If the entity is first class java object
	 */
	private void makeEntityMetadata() {
		this.fieldTypeMetadataLst = new ArrayList<FieldTypeMetadata>();
		
		makeFieldTypeMetadataLst();
		updateRecordSize(fieldTypeMetadataLst.size());
	}

	/**
	 * If the entity is primitive type
	 */
	private void makePrimEntityMetadata() {	
		this.primEMetadata = makePrimTypeMetada(this.eClass.getSimpleName());
		//update field header
		updateFieldHeader(this.primEMetadata);
		updateRecordSize(this.primEMetadata.fieldType.size);
		//number of fields. In a compound key it will have more than 1
		//1 byte for the metadata of each key field
		//TODO - using the binary string we can represent the metadata with 1 byte for all the fields
		updateRecordSize(1);
	}

	/**
	 * 
	 * @param fileChannel
	 * @throws IOException 
	 */
	final void read(FileChannel fileChannel) throws IOException {
		//root pos logically holds the length of the (header - sizeOf(rootPos))
		ByteBuffer headerBuf = ByteBuffer.allocate(SizeUtils.sizeOf(rootPos) + (int) rootPos);
		fileChannel.position(0); //Read the header

		fileChannel.read(headerBuf);

		//Logically the header was already made using the info from entity. 
		//Index file provided should also have the same physical header. 
		//Validating the same below 		
		try {

			headerBuf.position(0);

			//get the root node position
			assert headerBuf.getLong() == rootPos;

			//get the record size
			assert headerBuf.getInt() == keySize;

			//get the record size
			assert headerBuf.getInt() == recSize;
			//get the order
			assert headerBuf.getInt() == order;

			//get the key header length
			assert headerBuf.getInt() == keyHeaderLen;

			byte[] keyHeaderInBytesTemp = new byte[keyHeaderLen];

			//get the field header
			headerBuf.get(keyHeaderInBytesTemp);

			assert Arrays.equals(keyHeaderInBytesTemp, keyHeaderInBytes) == true;

			//get the file header length
			assert headerBuf.getInt() == fieldHeaderLen;

			byte[] fieldHeaderInBytesTemp = new byte[fieldHeaderLen];

			//get the field header
			headerBuf.get(fieldHeaderInBytesTemp);

			assert Arrays.equals(fieldHeaderInBytesTemp, fieldHeaderInBytes) == true;
		} catch (AssertionError e) {
			throw new ValidationException("Header validation failed!", e);
		}
	}

	/**
	 * Writes the header to index file
	 * @param fileChannel
	 * @throws IOException 
	 */
	final void write(FileChannel fileChannel) throws IOException {
		//Allocate the buffer
		ByteBuffer headerBuf = ByteBuffer.allocate(SizeUtils.sizeOf(rootPos) + (int) rootPos);//Total header size

		//put the root node position
		headerBuf.putLong(rootPos);
		//put the record size
		headerBuf.putInt(keySize);
		//put the record size
		headerBuf.putInt(recSize);
		//put the order
		headerBuf.putInt(order);
		//put the key header
		headerBuf.putInt(keyHeaderLen);
		//put the field header
		headerBuf.put(keyHeaderInBytes);
		//put the file header length
		headerBuf.putInt(fieldHeaderLen);
		//put the field header
		headerBuf.put(fieldHeaderInBytes);

		//position the pointer to start
		headerBuf.flip();

		fileChannel.write(headerBuf);
	}

	private void makeFieldTypeMetadataLst() {
		Field[] fields = eClass.getDeclaredFields();
		Arrays.<Field>stream(fields).forEach(field -> {
			FieldTypeMetadata FieldTypeMetadata = makeFieldTypeMetadata(field);

			fieldTypeMetadataLst.add(FieldTypeMetadata);

			if (FieldTypeMetadata != null) {
				//update field header
				updateFieldHeader(FieldTypeMetadata);
				updateRecordSize(FieldTypeMetadata.fieldType.size);
			}
		});
	}

	private void makeKeyMetadaLst() {
		Field[] fields = keyClass.getDeclaredFields();

		Arrays.<Field>stream(fields).forEach(field -> {
			FieldTypeMetadata metadata = makeFieldTypeMetadata(field);

			//Add to key field list
			keyMetadataLst.add(metadata);

			if (metadata != null) {
				//update field header
				updateKeyHeader(metadata);
				updateKeySize(metadata.fieldType.size);
			}
		});
	}

	//set the field header length
	private void setFieldHeaderLength() {
		this.fieldHeaderInBytes = fieldHeader.toString().getBytes(StandardCharsets.UTF_8);
		this.fieldHeaderLen = this.fieldHeaderInBytes.length;
	}

	private void setKeyHeaderLength() {
		this.keyHeaderInBytes = keyHeader.toString().getBytes(StandardCharsets.UTF_8);
		this.keyHeaderLen = this.keyHeaderInBytes.length;
	}

	//set the root position
	private void setRootPos() {
		//sum of sizes of recSize,order,fieldHeaderLength,length(fieldHeaderInBytes) 
		this.rootPos = SizeUtils.sizeOf(this.recSize) + SizeUtils.sizeOf(this.keySize) + SizeUtils.sizeOf(this.order)
				+ SizeUtils.sizeOf(this.keyHeaderLen) + SizeUtils.sizeOf(this.keyHeaderInBytes)
				+ SizeUtils.sizeOf(this.fieldHeaderLen) + SizeUtils.sizeOf(this.fieldHeaderInBytes)
				+ SizeUtils.sizeOf(this.rootPos);
	}

	//page_size =  m*(fieldsSize + keysSize) + (m+1)*sizeOf(fpos) + isLeaf + keyTally
	//Above formula will deduce to
	//m = (page_size - sizeof(fpos))/(recSize + sizeOf(fpos)) 
	private void computeOrder() {
		this.order = (Constatnts.BLOCK_SIZE_IN_KB * 1024 - Long.BYTES - Integer.BYTES - 1)
				/ (this.recSize + this.keySize + Long.BYTES);
		this.minOrder = this.order / 2;
	}

	private void updateRecordSize(int size) {
		recSize += size;
	}

	private void updateKeySize(int size) {
		keySize += size;
	}

	//update field header
	private void updateFieldHeader(PrimTypeMetadata typeMetadata) {
		if (fieldHeader.length() > 0) {
			fieldHeader.append(",");
		}
		fieldHeader.append(typeMetadata.toString());
	}

	//update key header
	private void updateKeyHeader(PrimTypeMetadata keyMetadata) {
		if (keyHeader.length() > 0) {
			keyHeader.append(",");
		}
		keyHeader.append(keyMetadata.toString());
	}

	//Primitive type metadata
	private PrimTypeMetadata makePrimTypeMetada(String primSimpleName) {
		//primitives come with fixed data length. string is variable
		if ("String".equals(primSimpleName)) {
			return new PrimTypeMetadata(new FieldType("String", 50 + 1));//+1 to store the length
		} else {			
			return new PrimTypeMetadata(FieldType.FIELD_TYPES.get(primSimpleName));
		}
	}
	
	//Prepare field metadata
	private FieldTypeMetadata makeFieldTypeMetadata(Field field) {
		//Look up for variables annotated with Field,TextField or Key
		boolean exists = field.isAnnotationPresent(com.cduvvuri.sidb.annotations.Field.class)
				|| field.isAnnotationPresent(com.cduvvuri.sidb.annotations.TextField.class)
				|| field.isAnnotationPresent(com.cduvvuri.sidb.annotations.Key.class);

		if (!exists) //Return if the fied is none
			return null;

		field.setAccessible(true);

		String fieldTypeName = field.getType().getSimpleName();

		//primitives come with fixed data length. string is variable
		if ("String".equals(fieldTypeName)) {
			TextField textField = field.getAnnotation(com.cduvvuri.sidb.annotations.TextField.class);

			return new FieldTypeMetadata(field.getName(), new FieldType("String", textField.length() + 1), field);//+1 to store the length
		} else {
			//TODO - Move this into validation flow
			if (FieldType.FIELD_TYPES.get(fieldTypeName) == null) {
				throw new BTreeException("Only Primitive wrappers are supported");
			}
			return new FieldTypeMetadata(field.getName(), FieldType.FIELD_TYPES.get(fieldTypeName), field);
		}
	}
}
