package com.cduvvuri.sidb.inmem.avltree;

import com.cduvvuri.sidb.index.InMemoryIndex;
import com.cduvvuri.sidb.inmem.avltree.Node.NavigateType;
import com.cduvvuri.sidb.logger.ILogger;

/**
 * TODO* Making it persistent
 * 
 * From WIKI: An AVL tree is a self-balancing binary search tree. It was the first such data structure to be invented. 
   In an AVL tree, the heights of the two child subtrees of any node differ by at most one 
   if at any time they differ by more than one, rebalancing is done to restore this property. 
   Lookup, insertion, and deletion all take O(log n) time in both the average and worst cases, where n is the number of nodes in the tree prior to the operation. 
   Insertions and deletions may require the tree to be rebalanced by one or more tree rotations.
 * 
 * 
 * Space O(n)
 * 
 * Insert O(logn)
 * Delete O(logn)
 * Update O(logn)
 * Search O(logn)
 * Pred O(logn)
 * Succ O(logn)
 * 
 * @author Chaitanya DS
 * 17-Nov-2017
 */
public class AVLTree<K extends Comparable<K>, E> implements InMemoryIndex<K, E> {
	Node<K, E> root;//root node

	/**
	 * TODO Fix the duplicate key issue
	 * @return false If key OR entity is null
	 */
	@Override
	public boolean insert(K k, E e) {
		if (k == null || e == null) {
			return false;
		}
		//If the root is null create the node
		Node<K, E> entry = new Node<K, E>(k, e);

		//traverse the tree to insert
		root = insert(root, entry);

		return true;
	}

	//Insertion implemented with the recursive strategy
	private Node<K, E> insert(Node<K, E> node, Node<K, E> entry) {
		if (node == null) {
			return entry;
		}

		//Navigate to the left or right
		//If the keys are equal node.set API overrides the value of the current node
		node.set(insert(node.next(entry.k).node, entry));

		//If the keys are equal, violation shud never occur
		if (node.isViolation()) {
			//balance the tree
			node = balance(node, entry.k);
		}

		return node;//Finally, root will be returned
	}

	//Entry node required to know the direction in which the new node has been added
	//Rotate - https://en.wikipedia.org/wiki/Tree_rotation
	private Node<K, E> balance(Node<K, E> node, K k) {
		ILogger.info("Tree is unbalanced. Violated node : " + node.k + " degree " + node.degree());
		Rotate rotate = findRotate(node, k);
		ILogger.info("Rotate : " + rotate);
		switch (rotate) {
		case LL:
			return rotateRight(node);
		case RR:
			return rotateLeft(node);
		/**
			P           P          R 
		   /           /          / \
		  Q      ==>  R    ==>   Q	 P	 	  
		   \         /
		    R       Q
		 */
		case LR:
			node.setLeft(rotateLeft(node.l));
			return rotateRight(node);
		/**
		 	P          P              R
		 	 \          \            / \
		 	  Q ==>      R	==>    	P	Q 	  
		 	 /            \ 
		 	R              Q
		 */
		case RL:
			node.setRight(rotateRight(node.r));
			return rotateLeft(node);
		}

		return node;//Loop should not come here
	}

	/**
	  			Q
			   / \	  
	  		  R	  P
	  		 / \
	  		S   X
	  	   /
	  	  T	
	 * Q violated the AVL rule. Rotate Q to RIGHT. Steps required
	 	->Q.left = R.X
	 	->R.right = Q	 	
	 * 
	 * @param node
	 * @return
	 */
	private Node<K, E> rotateRight(Node<K, E> node) {
		System.out.println("rotate right");
		Node<K, E> l = node.l;
		node.setLeft(l.r);
		l.setRight(node);
		return l;
	}

	/**
			Q
		   / \
		  P	  R
			 / \
			X   S
			     \
				  T	
	* @param node
	* @return
	*/
	private Node<K, E> rotateLeft(Node<K, E> node) {
		System.out.println("rotate left");
		Node<K, E> r = node.r;
		node.setRight(r.l);
		r.setLeft(node);
		return r;//new up node
	}

	//TODO Handle the case If the key is same; though the scenario should not occur
	private Rotate findRotate(Node<K, E> node, K k) {
		NavigateType type = node.navigateType(k);

		if (type.equals(NavigateType.L)) {
			return Rotate.valueOf(type + node.l.navigateType(k).toString());
		} else {
			return Rotate.valueOf(NavigateType.R.toString() + node.r.navigateType(k));
		}
	}

	/**
	 * @return null If key is not found
	 * @return Entity If key is found
	 */
	@Override
	public E search(K key) {
		Node<K, E> node = root;
		while (true) {
			//Navigate to left or right
			//Current(this) node reference is returned If keys are equal
			Node<K, E> tempNode = node.next(key).node;

			if (tempNode == node) {
				return node.e;
			} else if (tempNode == null) {
				//Key not found
				return null;
			} else
				//Continue traversal
				node = tempNode;
		}
	}

	/**           P
				/  \
			   /    \
			  Q	     R
			 / \      \
			S   D      B
	 */
	@Override
	public boolean delete(K key) {
		root = delete(root, key);
		return search(key) == null ? true : false;//Optmize it later, we do not need explicit search to validate this.
	}

	private Node<K, E> delete(Node<K, E> node, K key) {
		if (node == null) {
			return null;
		}

		if (node.k.compareTo(key) == 0) {
			//scenario-1, leaf node
			if (node.isLeaf()) {
				return null;
			}

			//scenario-2, promote the child
			if (node.hasOnlyOneChild()) {
				return node.l == null ? node.r : node.l;
			}

			//scenario-3, consists both the childs
			Node<K, E> successor = successor(node, key);
			node.k = successor.k;
			node.e = successor.e;
			key = successor.k;
		}

		//Get the next node
		Node<K, E>.Next nextNode = node.next2(key);

		if (nextNode != null) {
			//Returning node from delete
			Node<K, E> tN = delete(nextNode.node, key);

			if (nextNode.type == NavigateType.L) {
				node.setLeft(tN);
			} else {
				node.setRight(tN);
			}
		}

		if (node.isViolation()) {
			//balance the tree.
			//Let z be the first unbalanced node, y be the larger height child of z, and x be the larger height child of y.
			//Simple hack to force the balance API to traverse in the direction as explained above.
			node = balance(node, node.getLargerHeightNext().getLargerHeightNext().k);
		}

		return node;
	}

	enum Rotate {
		LL, LR, RR, RL
	}

	/**
	 * If the right child exists. Minimum of right child.
	 * If the right child do not exist. Traverse back until you take right turn.
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
	@Override
	public K successor(K key) {
		if (key == null) {
			return null;
		}

		if (root == null) {
			return null;
		}

		Node<K, E> node = successor(root, key);

		if (node == null) {
			return null;
		} else {
			return node.k;
		}
	}

	private Node<K, E> successor(Node<K, E> node, K k) {
		//If the key is not found, return null
		if (node == null) {
			return null;
		}

		Node<K, E>.Next next = node.next(k);

		//If the keys are equal match found
		if (next.node == node) {
			//If the right child exists find the min
			if (node.r != null) {
				return min(node.r);
			} else {
				return next.node;
			}
		}

		Node<K, E> tempNode = successor(next.node, k);

		//If the key itself is not found
		if (tempNode == null)
			return null;

		// While the recursion is retiring first largest node will be the successor.(If the right subtree do not exist for the given key)
		if (tempNode.k.compareTo(k) > 0) {
			return tempNode;
		} else {
			if (node.k.compareTo(k) > 0) {
				return node;
			} else {
				return tempNode;
			}
		}
	}

	private Node<K, E> min(Node<K, E> node) {
		if (node == null) {
			return null;
		}

		if (node.l == null) {
			return node;
		}

		return min(node.l);
	}

	/**
	 	If the left node exists, max of left is the predecessor.
	 	If the left node do not exist, traverse up until you take left turn.
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
	@Override
	public K predecessor(K key) {
		if (key == null) {
			return null;
		}

		if (root == null) {
			return null;
		}

		Node<K, E> node = predecessor(root, key);
		return node != null ? node.k : null;
	}

	private Node<K, E> predecessor(Node<K, E> node, K key) {
		if (node == null) {
			return null;
		}

		//Step-1 Find the key. If the keys are equal node.next API returns the same node
		Node<K, E>.Next next = node.next(key);

		//Key found
		if (next.node == node) {
			//find the max of left subtree, If the left node exists
			//else 
			//return the next.node
			return next.node.l != null ? max(next.node.l) : next.node;
		}

		Node<K, E> tempNode = predecessor(next.node, key);

		//If the entry key itself is not found
		if (tempNode == null)
			return null;

		//If the returned node is predecessor, it must be less than the input key.
		//else find the first smallest node while retiring the recursion. 
		//Theoretically it is the first left turn node 
		if (tempNode.k.compareTo(key) < 0) {
			return tempNode;
		} else {
			if (node.k.compareTo(tempNode.k) < 0) {
				return node;
			}
		}
		return tempNode;
	}

	//Recursive strategy. Traverse along the left path. 
	//If the input node do not have right, return the input node.
	private Node<K, E> max(Node<K, E> node) {
		if (node == null) {
			return null;
		}

		//If there is no right node return the current node which itself is max.
		if (node.r == null) {
			return node;
		}

		return max(node.r);
	}

}
