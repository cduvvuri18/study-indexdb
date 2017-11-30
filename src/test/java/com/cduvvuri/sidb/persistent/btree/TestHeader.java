package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.common.SampleEntity;
import com.cduvvuri.sidb.common.SampleKey;
import com.cduvvuri.sidb.common.SizeUtils;
import com.cduvvuri.sidb.logger.ILogger;
import com.cduvvuri.sidb.persistent.btree.Header;

public class TestHeader {
	@Test
	public void testHeader() throws IOException {
		Header<SampleKey, SampleEntity> header = new Header<SampleKey, SampleEntity>(SampleKey.class, SampleEntity.class);

		ILogger.info("Root pos :: " + header.rootPos);//8
		ILogger.info("Rec size :: " + header.recSize);//20+4+2
		ILogger.info("Rec size :: " + header.keySize);//4+1
		ILogger.info("Order :: " + header.order);
		ILogger.info("Field header :: " + header.fieldHeader);//field1|Integer|4,field2|String|20
		ILogger.info("Field header length :: " + header.fieldHeaderLen);//33
		ILogger.info("Key header :: " + header.keyHeader);//id|Integer|4
		ILogger.info("Key header length :: " + header.keyHeaderLen);//12

		Assert.assertTrue(header.rootPos == 73);
		Assert.assertTrue(header.recSize == 27);
		Assert.assertTrue(header.order == 204);
		Assert.assertTrue(header.fieldHeaderLen == 33);

		int tempRootPos = SizeUtils.sizeOf(header.recSize) + SizeUtils.sizeOf(header.keySize)
				+ SizeUtils.sizeOf(header.order) 
				+ SizeUtils.sizeOf(header.fieldHeaderLen)+ SizeUtils.sizeOf(header.fieldHeaderInBytes) 
				+ SizeUtils.sizeOf(header.keyHeaderLen)+ SizeUtils.sizeOf(header.keyHeaderInBytes)
				+ SizeUtils.sizeOf(header.rootPos);

		Assert.assertTrue(header.rootPos == tempRootPos);

		Path path = Paths.get(SampleEntity.class.getSimpleName() + ".idx");

		Files.deleteIfExists(path);

		RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");

		FileChannel fileChannel = raf.getChannel();

		header.write(fileChannel);

		header.read(fileChannel);//should not throw assertion exception

		raf.close();
	}
}
