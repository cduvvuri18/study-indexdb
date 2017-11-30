package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;

/**
 * 
 * @author Chaitanya DS
 * 17-Nov-2017
 */
final class Search<K extends Comparable<K>, E> {
	private final BTree<K, E> btree;	

	
	
	private Search(BTree<K, E> btree) {
		this.btree = btree;
	}

	static<K extends Comparable<K>, E> Search<K, E> getNewInstance(BTree<K, E> btree) {
		return new Search<K, E>(btree);
	}

	//Search
	////Reference - Adam drozdek, CLRS
	final TupleMetadata<K,E> search(K k) throws IOException {
		Node<K, E> node = btree.rootNode;

		while (true) {
			//Gets incremented only If comparision GT 0
			int i = 0;
			while (i < node.keyTally && k.compareTo(node.tuples[i].key) > 0) {
				++i;
			}

			if (i < node.keyTally && k.compareTo(node.tuples[i].key) == 0) {
				return new TupleMetadata<K,E>(i, node);				
			}

			if (node.isLeaf) {
				return null;
			} else {
				node.children[i].read(btree.fileChannel, btree.header);
				node = node.children[i];
			}
		}
	}
}
