package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.common.SampleEntity;
import com.cduvvuri.sidb.common.SampleKey;
import com.cduvvuri.sidb.persistent.btree.Header;
import com.cduvvuri.sidb.persistent.btree.Node;
import com.cduvvuri.sidb.persistent.btree.Tuple;

public class TestNode {
	@Test
	public void testReadWrite() throws IOException {
		Header<SampleKey, SampleEntity> header = new Header<SampleKey, SampleEntity>(SampleKey.class,
				SampleEntity.class);

		header.order = 3;

		header.nodeSize = header.order * (header.keySize + header.recSize) + 1 + Integer.BYTES
				+ (header.order + 1) * Long.BYTES;

		Node<SampleKey, SampleEntity> node = new Node<SampleKey, SampleEntity>(header.order);

		node.isLeaf = true;
		node.fpos = 0;

		Path path = Paths.get("test.idx");
		Files.deleteIfExists(path);

		RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");

		FileChannel fileChannel = raf.getChannel();

		node.setTuple(0, new Tuple<SampleKey, SampleEntity>(new SampleKey(1), new SampleEntity(100, "A")));
		node.setTuple(1, new Tuple<SampleKey, SampleEntity>(new SampleKey(2), new SampleEntity(101, "B")));
		node.setTuple(2, new Tuple<SampleKey, SampleEntity>(new SampleKey(3), new SampleEntity(102, "C")));

		node.write(fileChannel, header);

		fileChannel.close();
		raf.close();

		//Read what has been written
		Node<SampleKey, SampleEntity> newNode = new Node<SampleKey, SampleEntity>(header.order);

		newNode.isLeaf = true;
		newNode.fpos = 0;

		raf = new RandomAccessFile(path.toFile(), "rw");

		fileChannel = raf.getChannel();

		newNode.read(fileChannel, header);

		Assert.assertTrue(newNode.keyTally == 3);

		Arrays.<Tuple<SampleKey, SampleEntity>>stream(newNode.tuples).forEach(
				tuple -> 
				{
					System.out.println(tuple.key.getId() + "," + tuple.e.getField1() + "," + tuple.e.getField2());
					
					
				});

		fileChannel.close();
		raf.close();

	}
}
