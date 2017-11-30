package com.cduvvuri.sidb.inmem.redblack;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.inmem.redblack.RedBlackTree;
import com.cduvvuri.sidb.inmem.redblack.Node.Color;

public class TestRedBlackTree {
	//Generic test cases
	@Test
	public void testRedBlackTree() {
		RedBlackTree<Integer, Integer> redBlack = new RedBlackTree<Integer, Integer>();
		redBlack.insert(10, 10);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(5, 5);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(50, 50);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(4, 4);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(6, 6);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(30, 30);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(70, 70);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(15, 15);
		Assert.assertTrue(redBlack.isValidTree());
		redBlack.insert(40, 40);
		Assert.assertTrue(redBlack.isValidTree());
		
		
		for(int i = 71;i < 200;i++) {
			redBlack.insert(i, i);
		}
		
		Assert.assertTrue(redBlack.isValidTree());
		
		redBlack.printTree(redBlack.root, 0);
		
		Map<String, Integer> stats = redBlack.treeStats();
		
		System.out.println("BLACK_NODES_COUNT :: "+stats.get("BLACK_NODES_COUNT"));
		System.out.println("RED_NODES_COUNT :: "+stats.get("RED_NODES_COUNT"));
		System.out.println("BLACK_HEIGHT_FROM_ROOT :: "+stats.get("BLACK_HEIGHT_FROM_ROOT"));
		System.out.println("TREE_HEIGHT :: "+stats.get("TREE_HEIGHT"));
		System.out.println("TTL_NODES :: "+stats.get("TTL_NODES"));
		System.out.println("IS_VALID :: "+stats.get("IS_VALID"));
		
	}
	
	//Dev test cases
	
	/**
	 		P(B)
	 	   /    \
	 	  Q(R)  S(R) 
	 	  
	 */
	//@Test
	public void testInsertScenario1() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(5, "P");
		redBlack.insert(6, "S");
		redBlack.insert(4, "Q");

		Assert.assertTrue(redBlack.isValidTree());
		
		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.r.color == Color.RED);
	}

	/** Insert T in the above scenario
	 		P(R)
	 	   /    \
	 	  Q(B)  S(B) 
		 /
		T(R) 	
	*/
	//@Test
	public void testInsertScenario2() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(5, "P");
		redBlack.insert(8, "S");
		redBlack.insert(4, "Q");

		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.r.color == Color.RED);

		redBlack.insert(3, "R");

		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.r.color == Color.BLACK);

		redBlack.insert(7, "X");

		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.r.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.r.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.l.l.color == Color.RED);

		redBlack.insert(9, "X");

		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.r.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.r.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.l.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.r.r.color == Color.RED);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	/** Insert T in the above scenario
		P(R)
	   /    \
	  Q(B)  S(B) 
	/
	T(R) 	
	*/
	//https://www.cs.usfca.edu/~galles/visualization/RedBlack.html
	//@Test
	public void testInsertScenario3() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");
		redBlack.insert(60, "C");
		redBlack.insert(20, "D");
		redBlack.insert(80, "E");
		redBlack.insert(45, "F");
		redBlack.insert(10, "G");
		redBlack.insert(70, "H");

		Assert.assertTrue(redBlack.root.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.color == Color.RED);
		Assert.assertTrue(redBlack.root.l.l.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.r.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.l.l.l.color == Color.RED);
		
		
		Assert.assertTrue(redBlack.root.r.color == Color.BLACK);
		Assert.assertTrue(redBlack.root.r.r.color == Color.RED);
		Assert.assertTrue(redBlack.root.r.l.color == Color.RED);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//https://www.cs.usfca.edu/~galles/visualization/RedBlack.html
	//@Test
	public void testInsertSuccessor() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");
		redBlack.insert(60, "C");
		redBlack.insert(20, "D");
		redBlack.insert(80, "E");
		redBlack.insert(45, "F");
		redBlack.insert(10, "G");
		redBlack.insert(70, "H");
		redBlack.insert(30, "H");

		Assert.assertTrue(redBlack.successor(70) == 80);
		Assert.assertTrue(redBlack.successor(45) == 50);
		Assert.assertTrue(redBlack.successor(80) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
	//@Test
	public void testSearch() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");
		redBlack.insert(60, "C");
		redBlack.insert(20, "D");
		redBlack.insert(80, "E");
		redBlack.insert(45, "F");
		redBlack.insert(10, "G");
		redBlack.insert(70, "H");
		redBlack.insert(30, "H");

		Assert.assertTrue(redBlack.search(70) == "H");
		Assert.assertTrue(redBlack.search(45) == "F");
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
    //@Test
	public void testDelete() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");
		redBlack.insert(60, "C");
		redBlack.insert(20, "D");
		redBlack.insert(80, "E");
		redBlack.insert(45, "F");
		redBlack.insert(10, "G");
		redBlack.insert(70, "H");
		redBlack.insert(30, "H");

		//Scenario-1: Leaf
		Assert.assertTrue(redBlack.delete(10));
		Assert.assertTrue(redBlack.search(10) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
		
		//Scenario-2: promote
		Assert.assertTrue(redBlack.delete(80));
		Assert.assertTrue(redBlack.search(80) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
				
		Assert.assertTrue(redBlack.delete(45));
		Assert.assertTrue(redBlack.search(45) == null);				
		
		Assert.assertTrue(redBlack.isValidTree());
		
		
		//case-5
		Assert.assertTrue(redBlack.delete(45) == false);
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
	//@Test
	public void testDeleteRoot() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");
		redBlack.insert(60, "C");
		redBlack.insert(20, "D");
		redBlack.insert(80, "E");
		redBlack.insert(45, "F");
		redBlack.insert(10, "G");
		redBlack.insert(70, "H");
		redBlack.insert(30, "H");

		//Scenario-1: Leaf
		Assert.assertTrue(redBlack.delete(50));
		Assert.assertTrue(redBlack.search(50) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
	//@Test
	public void testDeleteRoot2() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");

		Assert.assertTrue(redBlack.delete(50));
		Assert.assertTrue(redBlack.search(50) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//@Test
	public void testDelete_onlyTwoElementsInTree() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");

		Assert.assertTrue(redBlack.delete(40));
		Assert.assertTrue(redBlack.search(40) == null);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//@Test
	public void testDeleteRoot3() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(50, "A");
		redBlack.insert(40, "B");

		Assert.assertTrue(redBlack.delete(50));
		Assert.assertTrue(redBlack.search(50) == null);
		Assert.assertTrue(redBlack.root.k == 40);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
	//@Test
	public void testDeleteCase6Doubleblackleft() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(10, "A");
		redBlack.insert(9, "B");
		redBlack.insert(30, "C");
		redBlack.insert(25, "D");
		redBlack.insert(40, "E");		
		
		System.out.println("case61-*****************************");
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(9));
		Assert.assertTrue(redBlack.search(9) == null);
		
		System.out.println("delete9*****************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//@Test
	public void testDeleteCase6DoubleblackRight() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(30, "A");
		redBlack.insert(31, "B");
		redBlack.insert(20, "C");
		redBlack.insert(19, "D");
		redBlack.insert(21, "E");		
		
		System.out.println("case62-*****************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(31));
		Assert.assertTrue(redBlack.search(31) == null);
		
		System.out.println("delete31*****************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		
		
		//If root is not in rotation
		redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(30, "A");
		redBlack.insert(20, "B");
		redBlack.insert(40, "C");
		redBlack.insert(15, "D");
		redBlack.insert(25, "E");
		redBlack.insert(35, "F");
		redBlack.insert(50, "G");
		redBlack.insert(10, "H");
		
		System.out.println("case622-*****************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(25));
		Assert.assertTrue(redBlack.search(25) == null);
		
		System.out.println("delete25*****************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
	
	//@Test
	public void testDeleteCase3Doubleblackleft() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(10, "A");
		redBlack.insert(-10, "B");
		redBlack.insert(30, "C");
		
		redBlack.root.l.color = Color.BLACK;
		redBlack.root.r.color = Color.BLACK;
		
		System.out.println("case63************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(-10));
		
		System.out.println("delete-10************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//@Test
	public void testDeleteCase2() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(10, "A");
		redBlack.insert(-10, "B");
		redBlack.insert(30, "C");
		redBlack.insert(29, "D");
		redBlack.insert(31, "E");
		
		System.out.println("case2************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(-10));
		
		System.out.println("delete-10************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}

	//@Test
	public void testDeleteCase3to5to6() {
		RedBlackTree<Integer, String> redBlack = new RedBlackTree<Integer, String>();
		redBlack.insert(10, "A");
		redBlack.insert(5, "B");
		redBlack.insert(50, "C");
		redBlack.insert(4, "E");
		redBlack.insert(6, "D");
		redBlack.insert(30, "E");
		redBlack.insert(70, "E");
		redBlack.insert(15, "E");
		redBlack.insert(40, "E");
		
		redBlack.root.l.color = Color.BLACK;		
		redBlack.root.l.r.color = Color.BLACK;
		redBlack.root.l.l.color = Color.BLACK;
		redBlack.root.r.color = Color.BLACK;
		
		redBlack.root.r.l.color = Color.RED;
		redBlack.root.r.l.r.color = Color.BLACK;
		redBlack.root.r.l.l.color = Color.BLACK;
		
		
		System.out.println("case2************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.delete(4));
		
		System.out.println("delete-4************************************");
		
		redBlack.printTree(redBlack.root, 0);
		
		Assert.assertTrue(redBlack.isValidTree());
	}
}
