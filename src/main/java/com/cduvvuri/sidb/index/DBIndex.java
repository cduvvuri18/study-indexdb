package com.cduvvuri.sidb.index;

/**
 * 
 * @author Chaitanya DS
 * 08-Nov-2017
 */
public interface DBIndex<K extends Comparable<K>, E> {
	/**
	 * open the index
	 * @return
	 */
	public boolean init();
	
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
	 * Delete the entity
	 * 
	 */
	public boolean delete(K key);
	
	/**
	 * close the index
	 */
	public boolean close();
	
	/**
	 * 
	 * @return K
	 */
	public K succecessor(K k);
	
	/**
	 * 
	 * @return K
	 */
	public K predecessor(K k);
}
