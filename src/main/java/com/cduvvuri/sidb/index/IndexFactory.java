package com.cduvvuri.sidb.index;

import java.nio.file.Path;

public interface IndexFactory<K extends Comparable<K>, E> {
	/**
	 * Creates the Index at the given path. Overides If index files exist.
	 * @param entity
	 * @param dbpath
	 * @return
	 */
	DBIndex<K, E> create(Class<K> key, Class<E> entity, Path dbpath);
	
	/**
	 * Open the index at the given path.
	 * @param entity
	 * @param dbpath
	 * @return
	 */
	DBIndex<K, E> open(Class<K> key, Class<E> entity, Path dbpath);
}
