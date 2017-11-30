package com.cduvvuri.sidb.persistent.btree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.annotations.Entity;
import com.cduvvuri.sidb.annotations.Field;
import com.cduvvuri.sidb.annotations.Key;
import com.cduvvuri.sidb.annotations.TextField;
import com.cduvvuri.sidb.common.SampleEntity;
import com.cduvvuri.sidb.common.SampleKey;
import com.cduvvuri.sidb.index.BTreeFactory;
import com.cduvvuri.sidb.index.DBIndex;
import com.cduvvuri.sidb.index.IndexFactory;
import com.cduvvuri.sidb.persistent.btree.BTree;

//TODO Testing in progress
public class TestBTree {
	@Test
	public void testCreateBTree() {
		IndexFactory<SampleKey, SampleEntity> factory = new BTreeFactory<SampleKey, SampleEntity>();
		DBIndex<SampleKey, SampleEntity> index = factory.create(SampleKey.class, SampleEntity.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));

		index.init();
		index.close();

		Path path = Paths.get("/home/chaitanya/chaitanya/geom-db/test-data/SampleEntity.idx");

		Assert.assertTrue(Files.exists(path));
	}

	@Test
	public void testOpenBTree() {
		IndexFactory<SampleKey, SampleEntity> factory = new BTreeFactory<SampleKey, SampleEntity>();
		DBIndex<SampleKey, SampleEntity> index = factory.open(SampleKey.class, SampleEntity.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));

		index.init();
		index.close();

		Path path = Paths.get("/home/chaitanya/chaitanya/geom-db/test-data/SampleEntity.idx");

		Assert.assertTrue(Files.exists(path));
	}

	@Test
	public void testInsert() {
		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		btree.insert(new SampleKey(1), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(2), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(3), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(4), new SampleEntity(103, "D"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		SampleEntity e = btree.search(new SampleKey(1));

		Assert.assertTrue(e.getField1() == 100);
		Assert.assertTrue("A".equals(e.getField2()));

		Assert.assertTrue(btree.isValid());

		btree.close();
	}

	//@Test
	public void testInsertComplex() {
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		List<Integer> list = getRandomList(100);

		for (int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i) + ",");
			btree.insert(new SampleKey(list.get(i)), new SampleEntity(list.get(i), "A" + list.get(i)));
		}

		Assert.assertTrue(btree.isValid());

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		SampleEntity e = btree.search(new SampleKey(99));

		Assert.assertTrue(e.getField1() == 99);
		Assert.assertTrue(("A" + 99).equals(e.getField2()));

		Assert.assertTrue(btree.isValid());

		btree.close();
	}

	@Test
	public void testSuccessor() {
		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		btree.insert(new SampleKey(1), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(2), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(3), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(4), new SampleEntity(103, "D"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.succecessor(new SampleKey(3)).getId() == 4);
		Assert.assertTrue(btree.succecessor(new SampleKey(2)).getId() == 3);
		Assert.assertTrue(btree.succecessor(new SampleKey(4)) == null);
		Assert.assertTrue(btree.succecessor(new SampleKey(1)).getId() == 2);

		btree.close();
	}

	@Test
	public void testDeleteHappyScenario() {

		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		btree.insert(new SampleKey(1), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(2), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(3), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(4), new SampleEntity(103, "D"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//case-1, Happy scenario, leaf has enough elements to meet the BTree property that node should have enough
		/*		btree.delete(new SampleKey(4));		
				Assert.assertTrue(btree.search(new SampleKey(4)) == null); 
		*/
		//case-2, steal from right/left sibling which has enough elements
		btree.delete(new SampleKey(1));
		Assert.assertTrue(btree.search(new SampleKey(1)) == null);

		//case-3

		btree.close();
	}

	@Test
	public void testDeleteStealFromRight() {

		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		btree.insert(new SampleKey(1), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(2), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(3), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(4), new SampleEntity(103, "D"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//case-2, steal from right sibling which has enough elements
		btree.delete(new SampleKey(1));
		Assert.assertTrue(btree.search(new SampleKey(1)) == null);

		//case-3

		btree.close();
	}

	@Test
	public void testDeleteStealFromLeft() {
		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//https://www.cs.usfca.edu/~galles/visualization/BTree.html
		btree.insert(new SampleKey(4), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(8), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(10), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(2), new SampleEntity(103, "D"));
		btree.insert(new SampleKey(1), new SampleEntity(103, "D"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//case-2, steal from left sibling which has enough elements
		btree.delete(new SampleKey(8));
		Assert.assertTrue(btree.search(new SampleKey(8)) == null);

		btree.delete(new SampleKey(10));
		Assert.assertTrue(btree.search(new SampleKey(10)) == null);

		btree.close();
	}

	@Test
	public void testScenarioRootHasOneKeyAndLeftSiblingDoNotHaveEnoughData() {
		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//https://www.cs.usfca.edu/~galles/visualization/BTree.html
		btree.insert(new SampleKey(4), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(8), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(10), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(2), new SampleEntity(103, "D"));
		btree.insert(new SampleKey(1), new SampleEntity(103, "E"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//case-2, steal from left sibling which has enough elements
		btree.delete(new SampleKey(8));
		Assert.assertTrue(btree.search(new SampleKey(8)) == null);

		btree.delete(new SampleKey(10));
		Assert.assertTrue(btree.search(new SampleKey(10)) == null);

		btree.delete(new SampleKey(4));
		Assert.assertTrue(btree.search(new SampleKey(4)) == null);

		btree.close();
	}

	@Test
	public void testScenarioRootHasOneKeyAndRightSiblingDoNotHaveEnoughData() {
		//Test insert
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//https://www.cs.usfca.edu/~galles/visualization/BTree.html
		btree.insert(new SampleKey(4), new SampleEntity(100, "A"));
		btree.insert(new SampleKey(8), new SampleEntity(101, "B"));
		btree.insert(new SampleKey(10), new SampleEntity(102, "C"));
		btree.insert(new SampleKey(2), new SampleEntity(103, "D"));
		btree.insert(new SampleKey(1), new SampleEntity(103, "E"));

		btree.close();

		//Test search
		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		//case-2, steal from left sibling which has enough elements
		btree.delete(new SampleKey(1));
		Assert.assertTrue(btree.search(new SampleKey(1)) == null);

		btree.delete(new SampleKey(2));
		Assert.assertTrue(btree.search(new SampleKey(2)) == null);

		btree.delete(new SampleKey(4));
		Assert.assertTrue(btree.search(new SampleKey(4)) == null);

		btree.close();

		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.search(new SampleKey(1)) == null);
		Assert.assertTrue(btree.search(new SampleKey(2)) == null);
		Assert.assertTrue(btree.search(new SampleKey(4)) == null);

		Assert.assertTrue(btree.search(new SampleKey(8)).getField1() == 101);
		Assert.assertTrue(btree.search(new SampleKey(10)).getField1() == 102);

		btree.close();
	}

	@Test
	public void testDeleteComplex1() {
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		List<Integer> list = getRandomList(20);

		list = new ArrayList<Integer>(
				Arrays.asList(new Integer[] { 16, 11, 2, 12, 7, 18, 1, 0, 10, 13, 5, 9, 8, 17, 3, 19, 15, 14, 6, 4 }));

		for (int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i) + ",");
			btree.insert(new SampleKey(list.get(i)), new SampleEntity(list.get(i), "A" + list.get(i)));
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		System.out.println("Delete**********************");

		for (int i = 0; i < 5; i++) {
			System.out.print(list.get(i) + ",");
			btree.delete(new SampleKey(list.get(i)));
			System.out.print("-" + list.get(i) + ",");

			System.out.println();
			//Assert.assertTrue(btree.isValid());
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		btree.close();

		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.isValid());

		btree.close();
	}

	@Test
	public void testDeleteComplex2() {
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		List<Integer> list = getRandomList(20);

		list = new ArrayList<Integer>(
				Arrays.asList(new Integer[] { 13, 1, 15, 19, 11, 10, 6, 7, 18, 12, 17, 9, 8, 2, 0, 14, 3, 4, 16, 5 }));

		for (int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i) + ",");
			btree.insert(new SampleKey(list.get(i)), new SampleEntity(list.get(i), "A" + list.get(i)));
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		System.out.println("Delete**********************");

		for (int i = 0; i < 5; i++) {
			System.out.print(list.get(i) + ",");
			btree.delete(new SampleKey(list.get(i)));
			System.out.print("-" + list.get(i) + ",");

			System.out.println();
			//Assert.assertTrue(btree.isValid());
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		btree.close();

		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.isValid());

		btree.close();
	}

	@Test
	public void testDeleteComplex3() {
		BTree<SampleKey, SampleEntity> btree = (BTree<SampleKey, SampleEntity>) createBTree();

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		List<Integer> list = getRandomList(20);

		list = new ArrayList<Integer>(
				Arrays.asList(new Integer[] { 10, 3, 6, 4, 1, 18, 12, 7, 0, 11, 13, 16, 2, 17, 9, 19, 14, 8, 15, 5 }));

		for (int i = 0; i < list.size(); i++) {
			System.out.print(list.get(i) + ",");
			btree.insert(new SampleKey(list.get(i)), new SampleEntity(list.get(i), "A" + list.get(i)));
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		System.out.println("Delete**********************");

		for (int i = 0; i < 5; i++) {
			System.out.print(list.get(i) + ",");
			btree.delete(new SampleKey(list.get(i)));
			System.out.print("-" + list.get(i) + ",");

			System.out.println();
			//Assert.assertTrue(btree.isValid());
		}

		System.out.println();

		Assert.assertTrue(btree.isValid());

		btree.close();

		btree = (BTree<SampleKey, SampleEntity>) openBTree();
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.isValid());

		btree.close();
	}

	@Test
	public void testInsertAndSearchForCustomTypes() {
		IndexFactory<StudentKey, Student> factory = new BTreeFactory<StudentKey, Student>();
		BTree<StudentKey, Student> btree = (BTree<StudentKey, Student>) factory.create(StudentKey.class, Student.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		String firstName = "Chaitanya";
		String lastName = "Duvvuri";
		Integer rollNo = 1;
		Short courseId = 100;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1990, 1, 1);
		
		Long dob = calendar.getTime().getTime();//TODO Support for date type
		Character gender = 'M';
		Boolean isSchlrshipProvided = false;
		Float cgpa = 8.9f;
		Double totalMarks = 99.95d;
		Byte branchId = 10;
		
		btree.insert(new StudentKey(1, 1990), new Student("Chaitanya", "Duvvuri", rollNo, courseId, dob, gender, isSchlrshipProvided, cgpa, totalMarks, branchId));

		btree.close();

		//Test search
		btree = (BTree<StudentKey, Student>) factory.open(StudentKey.class, Student.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Student e = btree.search(new StudentKey(1, 1990));

		Assert.assertTrue(e.getFirstName().equals(firstName));
		Assert.assertTrue(e.getLastName().equals(lastName));
		Assert.assertTrue(e.getRollNo().equals(rollNo));
		Assert.assertTrue(e.getCourseId().equals(courseId));
		Assert.assertTrue(e.getDob().equals(dob));
		Assert.assertTrue(e.getGender().equals(gender));
		Assert.assertTrue(e.getIsSchlrshipProvided().equals(isSchlrshipProvided));
		Assert.assertTrue(e.getCgpa().equals(cgpa));
		Assert.assertTrue(e.getTotalMarks().equals(totalMarks));
		Assert.assertTrue(e.getBranchId().equals(branchId));

		btree.close();
	}

	@Test
	public void testInsertAndSearchForPrimitiveTypes_INT_STRING() {
		IndexFactory<Integer, String> factory = new BTreeFactory<Integer, String>();
		BTree<Integer, String> btree = (BTree<Integer, String>) factory.create(Integer.class, String.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		
		btree.insert(1, "Chaitanya");
		btree.insert(2, "Duvvuri");

		btree.close();

		//Test search
		btree = (BTree<Integer, String>) factory.open(Integer.class, String.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));
		
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue("Duvvuri".equals(btree.search(2)));
		Assert.assertTrue("Chaitanya".equals(btree.search(1)));

		btree.close();
	}
	
	@Test
	public void testInsertAndSearchForPrimitiveTypes_STRING_INT() {
		IndexFactory<String, Integer> factory = new BTreeFactory<String, Integer>();
		BTree<String, Integer> btree = (BTree<String, Integer>) factory.create(String.class, Integer.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));

		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		
		btree.insert("Chaitanya", 1);
		btree.insert("Duvvuri", 2);

		btree.close();

		//Test search
		btree = (BTree<String, Integer>) factory.open(String.class, Integer.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));
		
		btree.header.order = 3;
		btree.header.minOrder = 3 / 2;

		btree.header.nodeSize = btree.header.order * (btree.header.keySize + btree.header.recSize) + 1 + Integer.BYTES
				+ (btree.header.order + 1) * Long.BYTES;

		btree.init();

		Assert.assertTrue(btree.search("Chaitanya") == 1);
		Assert.assertTrue(btree.search("Duvvuri") == 2);

		btree.close();
	}
	
	private DBIndex<SampleKey, SampleEntity> createBTree() {
		IndexFactory<SampleKey, SampleEntity> factory = new BTreeFactory<SampleKey, SampleEntity>();
		DBIndex<SampleKey, SampleEntity> index = factory.create(SampleKey.class, SampleEntity.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));
		return index;
	}

	private DBIndex<SampleKey, SampleEntity> openBTree() {
		IndexFactory<SampleKey, SampleEntity> factory = new BTreeFactory<SampleKey, SampleEntity>();
		DBIndex<SampleKey, SampleEntity> index = factory.open(SampleKey.class, SampleEntity.class,
				Paths.get("/home/chaitanya/chaitanya/geom-db/test-data"));
		return index;
	}

	private static List<Integer> getRandomList(int size) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			list.add(new Integer(i));
		}
		Collections.shuffle(list);
		return list;
	}
}

@Key
class StudentKey implements Comparable<StudentKey> {
	@Field(name = "id")
	private Integer id;

	@Field(name = "yob")
	private Integer yearOfBirth;

	StudentKey() {

	}

	StudentKey(Integer id, Integer yearOfBirth) {
		this.id = id;
		this.yearOfBirth = yearOfBirth;
	}

	@Override
	public int compareTo(StudentKey other) {
		int t = this.id.compareTo(other.id);
		if (t != 0) {
			return t;
		}

		return this.yearOfBirth.compareTo(other.yearOfBirth);
	}
}

@Entity
class Student {
	@TextField(name = "fName", length = 30)
	private String firstName;

	@TextField(name = "lName", length = 30)
	private String lastName;

	@Field(name = "rollNo")
	private Integer rollNo;

	@Field(name = "courseId")
	private Short courseId;

	@Field(name = "dob")
	private Long dob;//TODO Support for date type

	@Field(name = "gender")
	private Character gender = 'M';

	@Field(name = "isSchlrshipProvided")
	private Boolean isSchlrshipProvided;

	@Field(name = "cgpa")
	private Float cgpa;

	@Field(name = "totalMarks")
	private Double totalMarks;

	@Field(name = "branchId")
	private Byte branchId;

	Student() {

	}

	Student(String firstName, String lastName, Integer rollNo, Short courseId, Long dob, Character gender,
			Boolean isSchlrshipProvided, Float cgpa, Double totalMarks, Byte branchId) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.rollNo = rollNo;
		this.courseId = courseId;
		this.dob = dob;
		this.gender = gender;
		this.isSchlrshipProvided = isSchlrshipProvided;
		this.cgpa = cgpa;
		this.totalMarks = totalMarks;
		this.branchId = branchId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Integer getRollNo() {
		return rollNo;
	}

	public Short getCourseId() {
		return courseId;
	}

	public Long getDob() {
		return dob;
	}

	public Character getGender() {
		return gender;
	}

	public Boolean getIsSchlrshipProvided() {
		return isSchlrshipProvided;
	}

	public Float getCgpa() {
		return cgpa;
	}

	public Double getTotalMarks() {
		return totalMarks;
	}

	public Byte getBranchId() {
		return branchId;
	}
}
