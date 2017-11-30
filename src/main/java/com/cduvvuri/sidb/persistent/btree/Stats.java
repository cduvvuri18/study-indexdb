package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;

import com.cduvvuri.sidb.error.BTreeException;
import com.cduvvuri.sidb.logger.ILogger;

class Stats<K extends Comparable<K>, E> {
	private BTree<K, E> btree;

	Stats(BTree<K, E> btree) {
		this.btree = btree;
	}

	boolean isValid() {
		try {
			validate(btree.rootNode, null, null, 0, null);
			return true;
		} catch (BTreeException e) {
			ILogger.info(e.getMessage());
		}
		return false;
	}

	void validate(Node<K, E> node, K k, String type, Integer level, Integer pos) {
		//1. Unless it is root every node should have min elements of minOrder
		//2. Every node should have its elements sorted
		//3. Every child node derived from parent should have elements lesser than key from which it is derived

		//check properties 1,2,3 - If the node is root it will not meet 1,2

		//property-1
		if (node.parent != null && node.keyTally < btree.header.minOrder) {
			throw new BTreeException("BTree propert1 violated - " + k + "," + type + "," + level + "," + pos);
		}

		//property-3
		if (type != null && type.equals("L")) {
			boolean isOK = node.tuples[0].key.compareTo(k) < 0;

			if (!isOK) {
				throw new BTreeException("BTree propert3 violated - " + k + "," + type + "," + level + "," + pos);
			}
		}

		if (type != null && type.equals("R")) {
			boolean isOK = node.tuples[0].key.compareTo(k) > 0;

			if (!isOK) {
				throw new BTreeException("BTree propert3 violated - " + k + "," + type + "," + level + "," + pos);
			}
		}
		

		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}

		//property-2		
		for (int i = 0; i < node.keyTally; i++) {
			System.out.print(node.tuples[i].key + ",");
			if (i != 0) {
				boolean isOK = node.tuples[i - 1].key.compareTo(node.tuples[i].key) <= 0;
				if (!isOK) {
					throw new BTreeException("BTree property2 violated - " + k + "," + type + "," + level + "," + pos);
				}
			}
		}

		System.out.println();
		
		//traverse the node from left to right if the node is not leaf
		if (node.isLeaf) {
			return;
		}
		try {
			for (int i = 0; i < node.keyTally; i++) {
				validate(node.getChild(i, btree.fileChannel, btree.header), node.tuples[i].key, "L",
						Integer.valueOf(level + 5), Integer.valueOf(i));
			}

			validate(node.getChild(node.keyTally, btree.fileChannel, btree.header), node.tuples[node.keyTally - 1].key,
					"R", Integer.valueOf(level + 5), Integer.valueOf(node.keyTally));
		} catch (IOException e) {
			throw new BTreeException("IO error", e);
		}
	}
}
