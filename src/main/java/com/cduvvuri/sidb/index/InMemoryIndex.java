package com.cduvvuri.sidb.index;

public interface InMemoryIndex<K extends Comparable<K>, E> {
	/**
	 * Insert the entity.
	 * @param t
	 * @return
	 */
	public boolean insert(K key, E entity);
	
	/**
	 * Search the entity
	 * @param entity
	 * @return
	 */
	public E search(K key);
	
	/**
	 * TODO
	 * Delete the entity
	 * 
	 */
	public boolean delete(K key);
	
	/**
	 * Successor, Returns the input key If the successor do not exist
	 * @param key
	 * @return
	 */
	public K successor(K key);
	
	/**
	 * Predecessor, Return the input key If the predecessor do not exist
	 * @param key
	 * @return
	 */
	public K predecessor(K key);

}
