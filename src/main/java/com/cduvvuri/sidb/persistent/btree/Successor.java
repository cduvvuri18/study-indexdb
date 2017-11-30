package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;

/**
 * 
 * @author Chaitanya DS
 * 26-Nov-2017
 */
final class Successor<K extends Comparable<K>, E> {
	private BTree<K, E> btree;

	Successor(BTree<K, E> btree) {
		this.btree = btree;
	}

	static <K extends Comparable<K>, E> Successor<K, E> getNewInstance(BTree<K, E> btree) {
		return new Successor<K, E>(btree);
	}

	//Implementation is Analogous to successor in BST	
	final TupleMetadata<K, E> get(K k) throws IOException {
		Node<K, E> node = btree.rootNode;
		return findSuccessor(node, k);
	}

	private TupleMetadata<K, E> findSuccessor(Node<K, E> node, K k) throws IOException {
		if (node == null) {
			return null;
		}
		
		//Position at which key is found or the child pos to go down the tree
		int pos = findPos(node, k);

		//Key exists in the tree
		if(pos < node.keyTally && node.tuples[pos].key.compareTo(k) == 0) {
			if (node.isLeaf) {
				//Key exists in the leaf. Traverse the node.				
				if (pos + 1 < node.keyTally) {					
					return new TupleMetadata<K, E>(pos + 1, node);
				} else
					//successor do not exist in the node. look up.
					return new TupleMetadata<K, E>(pos, node);
			}

			if (!node.isLeaf && pos < node.keyTally) {
				return findMin(node.getChild(pos + 1, btree.fileChannel, btree.header));//traverse the right child
			}
		}
		
		//key does not exist in the tree
		if(node.isLeaf) {
			return null;
		}
		
		node.read(btree.fileChannel, btree.header);
		Node<K, E> childNode = node.getChild(pos, btree.fileChannel, btree.header);

		TupleMetadata<K, E> retTupleMd = findSuccessor(childNode, k);
		
		if(retTupleMd == null) {
			return null;
		}
		
		//Look up for the successor just like we do in BST
		K retK = retTupleMd.node.tuples[retTupleMd.pos].key;
		if(retK.compareTo(k) == 0) {
			if(pos < node.keyTally && node.tuples[pos].key.compareTo(retK) > 0) {
				retTupleMd.node = node;
				retTupleMd.pos = pos;
				return retTupleMd;	
			}
		}
		return retTupleMd;
	}

	private TupleMetadata<K, E> findMin(Node<K, E> node) throws IOException {
		if(node.isLeaf) {
			return new TupleMetadata<K, E>(0, node);
		}		
		return findMin(node.getChild(0, btree.fileChannel, btree.header));
	}

	private int findPos(Node<K, E> node, K k) {
		int pos = 0;
		while (pos < node.keyTally && node.tuples[pos].key.compareTo(k) < 0) {
			++pos;
		}
		return pos;
	}
}
