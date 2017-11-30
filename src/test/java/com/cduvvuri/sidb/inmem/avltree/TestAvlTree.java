package com.cduvvuri.sidb.inmem.avltree;

import org.junit.Assert;
import org.junit.Test;

import com.cduvvuri.sidb.inmem.avltree.AVLTree;
import com.cduvvuri.sidb.logger.ILogger;

public class TestAvlTree {
	@Test
	public void testInsert() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(1, "P");
		tree.insert(2, "Q");

		ILogger.info(String.valueOf(tree.root.h));

		Assert.assertTrue(tree.root.h == 1);
		Assert.assertTrue(tree.root.r.h == 0);

		Assert.assertTrue(tree.root.k == 1 && tree.root.e.equals("P"));
		Assert.assertTrue(tree.root.r.k == 2 && tree.root.r.e.equals("Q"));
	}

	@Test
	public void testInsertRR() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(1, "P");
		tree.insert(2, "Q");
		tree.insert(4, "R");
		tree.insert(5, "S");
		tree.insert(3, "X");
		tree.insert(6, "T");

		ILogger.info(String.valueOf(tree.root.h));

		Assert.assertTrue(tree.root.h == 2);

		Assert.assertTrue(tree.root.e.equals("R"));
		Assert.assertTrue(tree.root.l.e.equals("Q"));
		Assert.assertTrue(tree.root.r.e.equals("S"));
	}

	/**
	  			Q(6)
			   / \	  
	  	   (4)R	  P(7)
	  		 / \
	  	 (2)S   X(5)
	  	   /
	   (1)T	
	 */

	@Test
	public void testInsertLL() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(6, "Q");
		tree.insert(4, "R");
		tree.insert(7, "P");
		tree.insert(2, "S");
		tree.insert(5, "X");
		tree.insert(1, "T");

		ILogger.info(String.valueOf(tree.root.h));

		Assert.assertTrue(tree.root.h == 2);

		Assert.assertTrue(tree.root.e.equals("R"));
		Assert.assertTrue(tree.root.l.e.equals("S"));
		Assert.assertTrue(tree.root.r.e.equals("Q"));
	}

	/**
		 		P           P          R 
		 	   /           /          / \
		 	  Q      ==>  R    ==>   Q	 P	 	  
		 	   \         /
		 	    R       Q
	 */

	@Test
	public void testInsertRL() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(4, "P");
		tree.insert(2, "Q");
		tree.insert(3, "R");

		ILogger.info(String.valueOf(tree.root.h));

		Assert.assertTrue(tree.root.h == 1);

		Assert.assertTrue(tree.root.e.equals("R"));
		Assert.assertTrue(tree.root.l.e.equals("Q"));
		Assert.assertTrue(tree.root.r.e.equals("P"));
	}

	/**
			 	P          P              R
			 	 \          \            / \
			 	  Q ==>      R	==>    	P	Q 	  
			 	 /            \ 
			 	R              Q
	 */
	@Test
	public void testInsertLR() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(4, "P");
		tree.insert(6, "Q");
		tree.insert(5, "R");

		ILogger.info(String.valueOf(tree.root.h));

		Assert.assertTrue(tree.root.h == 1);

		Assert.assertTrue(tree.root.e.equals("R"));
		Assert.assertTrue(tree.root.l.e.equals("P"));
		Assert.assertTrue(tree.root.r.e.equals("Q"));
	}

	/**
	  			Q(6)
			   / \	  
	  	   (4)R	  P(7)
	  		 / \
	  	 (2)S   X(5)
	  	   /
	   (1)T	
	   
	   	1. Successor of T is S
		2. Successor of R is X
		3. Successor of Q is P
	 */
	@Test
	public void testSuccessor() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(6, "Q");
		tree.insert(4, "R");
		tree.insert(7, "P");
		tree.insert(8, "Z");
		tree.insert(2, "S");
		tree.insert(5, "X");
		tree.insert(1, "T");

		Assert.assertTrue(tree.root.h == 3);
		Assert.assertTrue(tree.successor(1) == 2);
		Assert.assertTrue(tree.successor(4) == 5);
		Assert.assertTrue(tree.successor(6) == 7);

		Assert.assertTrue(tree.successor(8) == 8);//If there is no successor for the input key
		Assert.assertTrue(tree.successor(10) == null);//If the key itself is not found
	}

	/**
	  			 Q(8)
				/   \	
			   /     \
			  /	      \
	  	   (4)R	     P(10)
	  	   /   \    /
	     (2)S X(5) Z(9)
	  	/
	 (1)T	
	 
	 */
	@Test
	public void testPredecessor() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(8, "Q");
		tree.insert(4, "R");
		tree.insert(10, "P");
		tree.insert(9, "Z");
		tree.insert(5, "R");
		tree.insert(2, "S");
		tree.insert(1, "T");

		Assert.assertTrue(tree.root.h == 3);
		Assert.assertTrue(tree.predecessor(9) == 8);
		Assert.assertTrue(tree.predecessor(8) == 5);
		Assert.assertTrue(tree.predecessor(4) == 2);

		Assert.assertTrue(tree.predecessor(1) == 1);//If there is no predecessor for the input key
		Assert.assertTrue(tree.predecessor(20) == null);//If the key itself is not found
	}

	/**
	  			 Q(8)
				/   \	
			   /     \
			  /	      \
	  	   (4)R	     P(10)
	  	   /   \    /
	     (2)S X(5) Z(9)
	  	/
	 (1)T	
	
	*/
	@Test
	public void testDelete() {
		AVLTree<Integer, String> tree = new AVLTree<Integer, String>();

		tree.insert(8, "Q");
		tree.insert(4, "R");
		tree.insert(10, "P");
		tree.insert(9, "Z");
		tree.insert(5, "R");
		tree.insert(2, "S");
		tree.insert(1, "T");

		//After the delete. Rotation should get applied
		Assert.assertTrue(tree.delete(9));
		Assert.assertTrue(tree.root.h == 2);
		
		Assert.assertTrue(tree.delete(10));
		Assert.assertTrue(tree.delete(100));
		Assert.assertTrue(tree.delete(4));
		
	}

}
