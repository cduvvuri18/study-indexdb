package com.cduvvuri.sidb.persistent.btree;

final class TupleMetadata<K extends Comparable<K>, E> {
	int pos;
	Node<K, E> node;	
	
	TupleMetadata(int pos, Node<K,E> node) {
		this.pos = pos;
		this.node = node;
	}
}
