package com.cduvvuri.sidb.inmem.redblack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.cduvvuri.sidb.index.InMemoryIndex;
import com.cduvvuri.sidb.inmem.redblack.Node.Color;
import com.cduvvuri.sidb.inmem.redblack.Node.NextType;
import com.cduvvuri.sidb.inmem.redblack.Node.RotateType;

/**
 * TODO* Making it persistent
 * 
 * Redblack tree properties
 	1. Node should be either Red or Black.
 	2. Root will always be Black.(Not sure if this has to be followed strictly) In the current implementation 
 	3. Red node parent should always be Black. In other words, adjacent nodes of red node are black.
 	4. Every path from root to leaf contain the same number of black nodes.
 	
 	-Duplicates will override the already inserted value similar to Map in Java
 	-TODO Space optimization - Re-use the same nil reference for all leaf and parent nodes.
 	-TODO More testing on delete
 	-Code review and optimization
 	
 * @author Chaitanya DS
 * 21-Nov-2017
 */
public class RedBlackTree<K extends Comparable<K>, E> implements InMemoryIndex<K, E> {
	Node<K, E> root;

	@Override
	public boolean insert(K key, E entity) {
		Node<K, E> ne = new Node<K, E>(key, entity);

		root = insert(root, ne);

		root.color = Color.BLACK;

		return false;
	}

	/**
	 * Scenario - 1: If the parent is black, mark the new node red and return
	 * Scenario - 2: If the grand parent exists which is black and uncle is red
	 * Scenario - 3: If the grand parent exists which is black and uncle is also black
	 * @param node
	 * @param newEntry
	 * @return
	 */
	private final Node<K, E> insert(Node<K, E> node, Node<K, E> ne) {
		if (node == null || node.isNill) {
			return ne;
		}

		//Update the node If the key is found
		//Should I throw the exception here to bypass everything?
		if (node.k.compareTo(ne.k) == 0) {
			node.e = ne.e;
			return node;
		}

		Node<K, E> retNode = insert(node.next(ne.k).node, ne);

		//If the node is child of returning node which implies rotation occurred during bottom up traversing.
		if (retNode.l == node || retNode.r == node) {
			return node = retNode;
		} else
			node.setNext(retNode);

		//If the returning node is black, do not need to process.		
		if (retNode.color == Color.BLACK) {
			return node;
		}

		//scenario-1(If the parent is black)
		if (node.color == Color.BLACK) {
			retNode.color = Color.RED;
		}

		//scenario-2(If the grand parent exists which is black and uncle is red)
		if (node.p != null && node.color == Color.RED && node.p.color == Color.BLACK
				&& node.p.getSiblingColor(node) == Color.RED) {
			//Change the color
			node.color = Color.BLACK;
			node.p.color = Color.RED;
			node.p.setSiblingColor(node, Color.BLACK);
			retNode.color = Color.RED;
		}

		//Scenario-3
		//If the grand parent exists which is black and uncle is also black
		if (node.p != null && node.color == Color.RED && node.p.color == Color.BLACK
				&& node.p.getSiblingColor(node) == Color.BLACK) {
			retNode.color = Color.RED;
			node = balance(node.p, ne.k);

			//rotation
			//swap colors of grand parent and parent
			Color tc = node.l.color;
			node.l.color = node.color;
			node.color = tc;
		}

		return node;
	}

	private Node<K, E> balance(Node<K, E> p, K k) {
		RotateType type = findRotate(p, k);
		switch (type) {
		case LL:
			return rotateRight(p);

		case RR:
			return rotateLeft(p);
		case LR:
			p.setNext(rotateLeft(p.next(k).node));
			return rotateRight(p);
		case RL:
			p.setNext(rotateRight(p.next(k).node));
			return rotateLeft(p);
		}
		return null;
	}

	/**
			P
			 \
			  Q
			   \
			    S
	* @param p
	* @return
	*/

	private Node<K, E> rotateLeft(Node<K, E> n) {
		Node<K, E> tr = n.r;
		n.r = tr.l;
		tr.l = n;

		//set the parents
		tr.p = n.p;
		n.p = tr;

		return tr;
	}

	/**
		 	 P
		 	/
	 	   Q
	 	  /
	 	 R 
	 * @param p
	 * @return
	 */
	private Node<K, E> rotateRight(Node<K, E> n) {
		Node<K, E> tl = n.l;
		n.l = tl.r;
		tl.r = n;

		//set the parents
		tl.p = n.p;
		n.p = tl;

		return tl;
	}

	private RotateType findRotate(Node<K, E> p, K k) {
		Node<K, E>.Next next = p.next(k);
		if (next.type == NextType.L) {
			return RotateType.valueOf(NextType.L.name() + next.node.next(k).type.name());
		}
		if (next.type == NextType.R) {
			return RotateType.valueOf(NextType.R.name() + next.node.next(k).type.name());
		}

		throw new RuntimeException("Unknown error, Unable to eval RotateType");
	}

	/**
	 * @return null If the key do not exist else entity.
	 */
	@Override
	public E search(K key) {
		Node<K, E> node = search(root, key);
		return node == null ? null : node.e;
	}

	private final Node<K, E> search(Node<K, E> node, K k) {
		if (node == null) {
			return null;
		}
		if (node.isNill) {
			return null;
		}
		return node.k.compareTo(k) == 0 ? node : search(node.next(k).node, k);
	}

	@Override
	public boolean delete(K key) {
		try {
			root = delete(root, key);

			//This cases arises, If root is the only node and deleted
			if (root.isNill) {
				root = null;
				return true;
			}
			//If root is double black
			if (root.color == Color.DOUBLEBLACK) {
				fixDoubleblackOnRoot(root);
			}
		} catch (KeyNotFoundException e) {
			return false;
		}

		return true;
	}

	//Perform BST delete
	private final Node<K, E> delete(Node<K, E> node, K k) {
		if (node == null) {
			throw new KeyNotFoundException();
		}

		if (node.isNill) {
			throw new KeyNotFoundException();
		}

		//It is not intuitive to put this statement here. Think better.
		Node<K, E>.Next next = node.next(k);

		if (node.k.compareTo(k) == 0) {
			//3 - scenerios
			//scenario-1
			if (node.isLeaf()) {
				Node<K, E> nilNode = node.l;
				//promote the nil child
				if (!node.isRed()) {
					//set the color as doubleblack
					nilNode.color = Color.DOUBLEBLACK;
				}

				return nilNode;
			}

			//scenario-2, promote
			Node<K, E>.Next oc = node.getOnlyChild();
			if (oc != null) {
				//promote the child
				if (node.isRed() || oc.node.isRed()) {
					//set the color as double black
					oc.node.color = Color.BLACK;
				} else
					oc.node.color = Color.DOUBLEBLACK;
				return oc.node;
			}

			//Get the successor, copy the contents and continue the deletion with the new key
			if (node.isInternalNode()) {
				Node<K, E> ks = successor(node, k);
				//Override the previously retrieved next node 
				next = node.next(ks.k);

				//Copy the contents
				node.k = ks.k;
				node.e = ks.e;
				k = ks.k;
			}
		}

		Node<K, E> retNode = delete(next.node, k);
		if (next.type == NextType.L)
			node.setLeft(retNode);
		else
			node.setRight(retNode);

		//https://en.wikipedia.org/wiki/Red%E2%80%93black_tree
		//Have to refactor inline with CLRS.(restructuring, re-coloring)
		//fix the double black node if exists.
		if (retNode.isDoubleBlack()) {
			Node<K, E> sibling = retNode.getSibling(node).node;

			if (node.isBlack() && sibling.isRed() && sibling.l.isBlack() && sibling.r.isBlack()) {
				return fixDoubleBlackCase2(node, retNode);
			}

			//case-3
			if (node.isBlack() && sibling.isBlack() && sibling.l.isBlack() && sibling.r.isBlack()) {
				return fixDoubleBlackCase3(node, retNode);
			}

			//case-4
			if (node.isRed() && sibling.isBlack() && sibling.l.isBlack() && sibling.r.isBlack()) {
				return fixDoubleBlackCase4(node, retNode);
			}

			//case-5
			if (sibling.isBlack()) {
				return fixDoubleBlackCase5(node, retNode);
			}
		}

		return node;
	}

	//case-1
	private Node<K, E> fixDoubleblackOnRoot(Node<K, E> root) {
		if (root.isDoubleBlack()) {
			root.color = Color.BLACK;
		}
		return root;
	}

	//parent is black, sibling is red, inner nodes black
	private Node<K, E> fixDoubleBlackCase2(Node<K, E> parent, Node<K, E> dbnode) {
		if (parent.l == dbnode) {
			parent = rotateLeft(parent);
			parent.color = Color.BLACK;
			parent.l.color = Color.RED;
			fixDoubleBlackCase4(parent.l, parent.l.l);
		} else if (parent.r == dbnode) {
			parent = rotateRight(parent);
			parent.color = Color.BLACK;
			parent.r.color = Color.RED;
			fixDoubleBlackCase4(parent.r, parent.r.r);
		}

		return parent;
	}

	private Node<K, E> fixDoubleBlackCase4(Node<K, E> parent, Node<K, E> dbnode) {
		Node<K, E> sibling = dbnode.getSibling(dbnode.p).node;
		dbnode.p.color = Color.RED;
		sibling.color = Color.RED;
		dbnode.color = Color.BLACK;
		return dbnode.p;
	}

	private Node<K, E> fixDoubleBlackCase3(Node<K, E> parent, Node<K, E> dbnode) {
		Node<K, E> sibling = dbnode.getSibling(dbnode.p).node;
		sibling.color = Color.RED;
		dbnode.color = Color.BLACK;
		dbnode.p.color = Color.DOUBLEBLACK;
		return parent;
	}

	private Node<K, E> fixDoubleBlackCase5(Node<K, E> parent, Node<K, E> dbnode) {
		Node<K, E>.Next sibling = dbnode.getSibling(parent);
		if (sibling.type == NextType.R && sibling.node.l.color == Color.RED && sibling.node.r.color == Color.BLACK) {
			Node<K, E> nn = rotateRight(sibling.node);
			parent.setNext(nn);
			nn.color = Color.BLACK;
			nn.r.color = Color.RED;
		} else if (sibling.type == NextType.L && sibling.node.r.color == Color.RED
				&& sibling.node.l.color == Color.BLACK) {
			Node<K, E> nn = rotateLeft(sibling.node);
			parent.setNext(nn);
			nn.color = Color.BLACK;
			nn.l.color = Color.RED;
		}
		parent = fixDoubleBlackCase6(parent, dbnode);
		return parent;
	}

	private Node<K, E> fixDoubleBlackCase6(Node<K, E> parent, Node<K, E> dbnode) {
		Node<K, E>.Next sibling = dbnode.getSibling(parent);
		if (sibling.type == NextType.R && sibling.node.r.color == Color.RED) {
			parent.r.color = parent.color;
			Node<K, E> nn = rotateLeft(parent);
			nn.l.color = Color.BLACK;
			nn.r.color = Color.BLACK;
			dbnode.color = Color.BLACK;
			return nn;
		} else if (sibling.type == NextType.L && sibling.node.l.color == Color.RED) {
			parent.l.color = parent.color;
			Node<K, E> nn = rotateRight(parent);
			nn.r.color = Color.BLACK;
			nn.l.color = Color.BLACK;
			dbnode.color = Color.BLACK;
			return nn;
		}
		return parent;
	}

	/**
	 * If the key is found - two cases exist.
	 * 1.If the right subtree is found min(right subtree) returns the successor
	 * 2.If the right subtree is not found, 
	 * while recursion is retiring the first element which is greater than the key is the successor.
	 */
	@Override
	public K successor(K key) {
		Node<K, E> node = successor(root, key);
		return node == null ? null : node.k == key ? null : node.k;
	}

	private Node<K, E> successor(Node<K, E> node, K key) {
		if (node == null) {
			return null;
		}

		if (node.isNill) {
			return null;
		}

		//If key is found return min of right subtree
		if (node.k.compareTo(key) == 0) {
			return min(node.r);
		}

		Node<K, E> retNode = successor(node.next(key).node, key);

		//key is not found return null
		//key is found, no right subtree, while traversing up look for the occurence of first key which is greater than the input key

		if (retNode != null && retNode.k.compareTo(key) > 0) {
			return retNode;
		}

		if (node.k.compareTo(key) > 0) {
			return (retNode = node);
		} else
			return retNode;
	}

	private Node<K, E> min(Node<K, E> node) {
		if (node == null)
			return null;

		if (node.isNill)
			return null;

		if (node.l.isNill) {
			return node;
		}

		return min(node.l);
	}

	/**
	 * TODO
	 */
	@Override
	public K predecessor(K key) {
		return null;
	}

	//Inorder like traversing.
	public void printTree(Node<K, E> node, int margin) {
		if (node == null) {
			return;
		}

		printTree(node.r, margin + 5);
		for (int i = 0; i < margin; i++) {
			System.out.print(" ");
		}
		System.out.println((node.k == null ? "na" : node.k) + node.color.name().substring(0, 1).toLowerCase());

		printTree(node.l, margin + 5);
	}

	/**
	 * TODO
	 * @returns Map<>
	 */
	public Map<String, Integer> treeStats() {
		if (root == null) {
			return null;
		}

		//black height from root
		//tree height
		//no of elements
		//isTreeValid

		//Black nodes count
		AtomicInteger bnc = new AtomicInteger();
		//Red node count
		AtomicInteger rnc = new AtomicInteger();
		//Black height from root
		AtomicInteger bh = new AtomicInteger();
		//Tree height
		AtomicInteger th = new AtomicInteger();
		//No of elements
		AtomicInteger ttln = new AtomicInteger();

		treeStats(root, bnc, rnc, ttln, th, bh, 0, 0);

		Map<String, Integer> map = new HashMap<String, Integer>();

		map.put("BLACK_NODES_COUNT", Integer.valueOf(bnc.get()));
		map.put("RED_NODES_COUNT", Integer.valueOf(rnc.get()));
		map.put("BLACK_HEIGHT_FROM_ROOT", Integer.valueOf(bh.get()));
		map.put("TREE_HEIGHT", Integer.valueOf(th.get()));
		map.put("TTL_NODES", Integer.valueOf(ttln.get()));
		map.put("IS_VALID", isValidTree() ? 1 : 0);

		return map;
	}

	public Boolean isValidTree() {
		//Adjacent nodes of red should be black
		//Every path from root to leaf contain the same number of black nodes
		//Root node should be black

		if (root == null) {
			return true;
		}

		AtomicInteger pbh = new AtomicInteger(-1);

		return isValidTree(root, pbh, 0);
	}

	private boolean isValidTree(Node<K, E> node, AtomicInteger pbh, int cbh) {
		//increment cbh if node is black
		if (node.p != null && node.isBlack()) {
			++cbh;
		}

		//If the node is red adjacent nodes should be black
		if (node.isRed()
				&& !(node.p.color == Color.BLACK && node.l.color == Color.BLACK && node.r.color == Color.BLACK))
			return false;

		//If node is nill
		if (node.isNill) {
			if (pbh.get() == -1 || pbh.get() == cbh) {
				pbh.set(cbh);
				return true;
			}
			return false;
		}

		return isValidTree(node.l, pbh, cbh) && isValidTree(node.r, pbh, cbh);
	}

	private void treeStats(Node<K, E> node, AtomicInteger blackNodeCount, AtomicInteger redNodeCount, AtomicInteger ttl,
			AtomicInteger h, AtomicInteger bd, int currentHeight, int currentBlackDepth) {
		++currentHeight;

		if (!node.isNill) {
			ttl.set(ttl.get() + 1);
		}

		if(node.isBlack()) {
			++currentBlackDepth;
		}
		
		if (node.isBlack() && !node.isNill) {
			blackNodeCount.set(blackNodeCount.get() + 1);			
		}

		if (node.isRed()) {
			redNodeCount.set(redNodeCount.get() + 1);
		}

		if (node.isNill) {
			bd.set(currentBlackDepth);
			h.set(h.get() > currentHeight ? h.get() : currentHeight);
			return;
		}

		treeStats(node.l, blackNodeCount, redNodeCount, ttl, h, bd, currentHeight, currentBlackDepth);
		treeStats(node.r, blackNodeCount, redNodeCount, ttl, h, bd, currentHeight, currentBlackDepth);
	}
}

class KeyNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
}



