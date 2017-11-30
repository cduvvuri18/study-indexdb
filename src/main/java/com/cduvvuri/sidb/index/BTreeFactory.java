package com.cduvvuri.sidb.index;

import java.nio.file.Path;

import com.cduvvuri.sidb.persistent.btree.BTree;

/**
 *  
 * @author Chaitanya DS
 * 13-Nov-2017
 */
public class BTreeFactory<K extends Comparable<K>, E> implements IndexFactory<K,E> {
	@Override
	public DBIndex<K,E> create(Class<K> key, Class<E> entity, Path dbpath) {
		//TODO create dyamic proxying to deduce AOP
		return new BTree<K,E>(new BTree.CreateRequest<K, E>(key, entity, dbpath, true));
	}

	@Override
	public DBIndex<K,E> open(Class<K> key, Class<E> entity, Path dbpath) {
		//TODO create dyamic proxying to deduce AOP
		return new BTree<K,E>(new BTree.CreateRequest<K, E>(key, entity, dbpath, false));
	}
}
