package com.cduvvuri.sidb.persistent.btree;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.common.SampleEntity;
import com.cduvvuri.sidb.common.SampleKey;
import com.cduvvuri.sidb.persistent.btree.Header;
import com.cduvvuri.sidb.persistent.btree.Tuple;

public class TestTuple {
	@Test
	public void testWrite() throws InstantiationException, IllegalAccessException {
		Header<SampleKey, SampleEntity> header = new Header<SampleKey, SampleEntity>(SampleKey.class,
				SampleEntity.class);

		SampleKey key = new SampleKey(1);
		SampleEntity e = new SampleEntity(10, "Chaitanya");

		Tuple<SampleKey, SampleEntity> tuple = new Tuple<SampleKey, SampleEntity>(key, e);

		ByteBuffer buffer = ByteBuffer.allocate(header.keySize + header.recSize);

		tuple.write(buffer, header);

		tuple = new Tuple<SampleKey, SampleEntity>(key, e);

		buffer.flip();
		
		tuple.read(buffer, header);

		SampleKey keyNew = tuple.key;
		SampleEntity entityNew = tuple.e;

		Assert.assertTrue(keyNew.getId() == 1);
		Assert.assertTrue(entityNew.getField1() == 10);
		Assert.assertTrue("Chaitanya".equals(entityNew.getField2()));
	}
}
