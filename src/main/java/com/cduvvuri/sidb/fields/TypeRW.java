package com.cduvvuri.sidb.fields;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


/**
 * 
 * @author Chaitanya DS
 * 30-Nov-2017
 */
//Type read writer
public interface TypeRW {
	public void write(ByteBuffer buff, Object value);

	public void read(ByteBuffer buff, Field field, Object object)
			throws IllegalArgumentException, IllegalAccessException;

	public Object read(ByteBuffer buffer);
	
	public static class ShortTypeRW implements TypeRW {
		private static ShortTypeRW value;

		private ShortTypeRW() {
		}

		public static ShortTypeRW getInstance() {
			if (value == null) {
				value = new ShortTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putShort((Short) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getShort());
		}

		@Override
		public Object read(ByteBuffer buffer) {
			// TODO Auto-generated method stub
			return buffer.getShort();
		}
	}

	public static class ByteTypeRW implements TypeRW {
		private static ByteTypeRW value;

		private ByteTypeRW() {
		}

		public static ByteTypeRW getInstance() {
			if (value == null) {
				value = new ByteTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.put((Byte) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.get());
		}

		@Override
		public Object read(ByteBuffer buff) {
			// TODO Auto-generated method stub
			return buff.get();
		}
	}

	public static class IntTypeRW implements TypeRW {
		private static IntTypeRW value;

		private IntTypeRW() {
		}

		public static IntTypeRW getInstance() {
			if (value == null) {
				value = new IntTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putInt((Integer) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getInt());
		}

		@Override
		public Object read(ByteBuffer buffer) {			
			return buffer.getInt();
		}
	}

	public static class LongTypeRW implements TypeRW {
		private static LongTypeRW value;

		private LongTypeRW() {
		}

		public static LongTypeRW getInstance() {
			if (value == null) {
				value = new LongTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putLong((Long) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getLong());
		}

		@Override
		public Object read(ByteBuffer buffer) {
			// TODO Auto-generated method stub
			return buffer.getLong();
		}
	}

	public static class FloatTypeRW implements TypeRW {
		private static FloatTypeRW value;

		private FloatTypeRW() {
		}

		public static FloatTypeRW getInstance() {
			if (value == null) {
				value = new FloatTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putFloat((Float) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getFloat());
		}

		@Override
		public Object read(ByteBuffer buffer) {
			// TODO Auto-generated method stub
			return buffer.getFloat();
		}
	}

	public static class DoubleTypeRW implements TypeRW {
		private static DoubleTypeRW value;

		private DoubleTypeRW() {
		}

		public static DoubleTypeRW getInstance() {
			if (value == null) {
				value = new DoubleTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putDouble((Double) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getDouble());
		}

		@Override
		public Object read(ByteBuffer buffer) {			
			return buffer.getDouble();
		}
	}

	public static class CharTypeRW implements TypeRW {
		private static CharTypeRW value;

		private CharTypeRW() {
		}

		public static CharTypeRW getInstance() {
			if (value == null) {
				value = new CharTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.putChar((Character) value);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.getChar());
		}

		@Override
		public Object read(ByteBuffer buffer) {
			// TODO Auto-generated method stub
			return buffer.getChar();
		}
	}

	public static class BooleanTypeRW implements TypeRW {
		private static BooleanTypeRW value;

		private BooleanTypeRW() {
		}

		public static BooleanTypeRW getInstance() {
			if (value == null) {
				value = new BooleanTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			buff.put((byte) (((Boolean) value) ? 1 : 0));
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			field.set(object, buff.get() == 1 ? Boolean.TRUE : Boolean.FALSE);
		}

		@Override
		public Object read(ByteBuffer buffer) {			
			return buffer.get() == 1 ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	public static class StringTypeRW implements TypeRW {
		private static StringTypeRW value;

		private StringTypeRW() {
		}

		public static StringTypeRW getInstance() {
			if (value == null) {
				value = new StringTypeRW();
			}
			return value;
		}

		@Override
		public void write(ByteBuffer buff, Object value) {
			byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
			buff.put((byte) bytes.length);
			buff.put(bytes);
		}

		@Override
		public void read(ByteBuffer buff, Field field, Object object)
				throws IllegalArgumentException, IllegalAccessException {
			byte size = buff.get();
			byte[] bytes = new byte[size];
			buff.get(bytes);
			field.set(object, new String(bytes, StandardCharsets.UTF_8));
		}

		@Override
		public Object read(ByteBuffer buffer) {			
			byte size = buffer.get();
			byte[] bytes = new byte[size];
			buffer.get(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}
}
