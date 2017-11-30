package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Node/Block
 * @author Chaitanya DS
 * 15-Nov-2017
 */
final class Node<K extends Comparable<K>, E> {
	//records
	final Tuple<K, E>[] tuples;

	//children
	final Node<K, E>[] children;

	//parent
	Node<K, E> parent;

	//Number of keys in the node
	int keyTally;

	boolean isLeaf = false;
	boolean isLoaded = false;
	long fpos = -1;

	@SuppressWarnings("unchecked")
	Node(int order) {
		this.tuples = new Tuple[order];
		this.children = new Node[order + 1];
	}

	/**
	 * Write the node to disk
	 */
	final boolean write(FileChannel fileChannel, Header<K, E> header) throws IOException {
		if (fileChannel == null) {
			return false;
		}

		//calculate the buffer size
		int bufferSize = 1;//leaf status
		bufferSize += Integer.BYTES;
		bufferSize += (header.keySize + header.recSize) * keyTally;
		bufferSize += isLeaf ? 0 : (keyTally + 1) * Long.BYTES;

		ByteBuffer buf = ByteBuffer.allocate(bufferSize);

		//Buffer leaf
		buf.put((byte) (isLeaf ? 1 : 0));
		//Buffer element count
		buf.putInt(keyTally);

		//Buffer elements. Iterate and store		    
		for (int i = 0; i < keyTally; i++) {
			tuples[i].write(buf, header);
		}

		for (int i = 0; i < (keyTally + 1) && !isLeaf; i++) {
			buf.putLong(children[i].fpos);
		}

		buf.flip();

		fileChannel.position(fpos);
		fileChannel.write(buf);
		return true;
	}

	/**
	 * Read the node from the index file
	 */
	final boolean read(FileChannel fileChannel, Header<K, E> header) throws IOException {
		// Null check
		if (fileChannel == null) {
			return false;
		}

		if (isLoaded) {
			return true;
		}

		fileChannel.position(fpos);

		//Create the byte buffer with the capacity of node size 
		ByteBuffer buf = ByteBuffer.allocate(header.nodeSize);

		//Read the page
		fileChannel.read(buf);

		buf.flip();

		//IsLeaf
		isLeaf = buf.get() == 1 ? true : false;

		//Read elements count
		this.keyTally = buf.getInt();

		//Init all the elements
		for (int i = 0; i < this.keyTally; i++) {
			tuples[i] = new Tuple<K, E>();
			tuples[i].read(buf, header);
		}

		//Init fpos of children
		for (int i = 0; i < (keyTally + 1) && !isLeaf; i++) {
			Node<K, E> childNode = new Node<K, E>(header.order);
			childNode.fpos = buf.getLong();
			childNode.isLoaded = false;
			childNode.parent = this;

			children[i] = childNode;
		}

		//Mark as loaded
		isLoaded = true;

		return true;
	}

	/**
	 * Find the appropriate position in the tuple[]
	 * to insert the tuple
	 * @param tuple
	 * @return
	 */
	final int findPos(Tuple<K, E> tuple) {
		int pos = 0;
		for (; pos < this.keyTally; pos++) {
			if (tuple.key.compareTo(this.tuples[pos].key) < 0) {
				break;
			}
		}
		return pos;
	}

	/**
	 * Set the tuple at the given index and resize the array
	 * @param tuple
	 * @return true
	 */
	final boolean setTuple(int indx, Tuple<K, E> tuple) {
		//shuffle the tuples to be in the increasing order
		//0,1,2,3 --> insert at 2 -->0,1,-,3,4 (2,3 moves into 3,4)
		for (int ctr = this.keyTally; ctr > indx; ctr--) {
			this.tuples[ctr] = this.tuples[ctr - 1];
		}
		//0,1,-,3,4 Add the tuple at vacant pos
		this.tuples[indx] = tuple;
		++this.keyTally;
		return true;
	}

	/**
	 * @return
	 * Copy tuple
	 */
	final boolean overrideTuple(int indx, Tuple<K, E> tuple) {
		this.tuples[indx] = tuple;
		return true;
	}
	
	/**
	 * Delete the tuple at the given index and shrink the array
	 * @param tuple
	 * @return
	 */
	final boolean deleteTuple(int indx) {
		//-,2,3,4
		int ctr = indx + 1;
		for (; ctr < this.keyTally; ctr++) {
			this.tuples[ctr - 1] = this.tuples[ctr];
		}
		//Shrink. Set the last tuple as null
		this.tuples[this.keyTally - 1] = null;
		--this.keyTally;
		return true;
	}

	/**
	 * Set the children at the given index
	 * @param idx
	 * @param node
	 * @return
	 */
	final boolean setChild(int indx, Node<K, E> node) {
		node.parent = this;
		int childCount = this.keyTally;
		//1,2,3,4		
		for (int ctr = childCount; ctr > indx; ctr--) {
			this.children[ctr] = this.children[ctr - 1];
		}
		//Add the node at vacant pos
		this.children[indx] = node;
		return true;
	}

	/**
	 * node is not required remove
	 * Delete the child at the given index
	 * @param idx
	 * @param node
	 * @return
	 */
	final boolean deleteChild(int indx) {
		//1,2,3,4. Say we are deleting 2		
		for (int ctr = indx + 1; ctr <= this.keyTally; ctr++) {
			this.children[ctr - 1] = this.children[ctr];
		}
		this.children[this.keyTally] = null;
		return true;
	}

	/**
	 * Move the subset of tuples into the given array from 0 
	 * and clear the moved tuples from original array
	 */
	final boolean moveSubsetOftuples(Tuple<K, E>[] consumingArr, int offset, int length) {
		for (int ctr = 0; ctr < length; ctr++) {
			consumingArr[ctr] = this.tuples[offset + ctr];
			this.tuples[offset + ctr] = null;
		}
		this.keyTally -= length;
		return true;
	}

	/**
	 * Move the subset of childs into the given array from 0
	 * and clear the moved tuples from original array
	 */
	final boolean moveSubsetOfChilds(Node<K, E> newNode, int offset, int length) {
		for (int ctr = 0; ctr < length; ctr++) {
			newNode.setChild(ctr, this.children[offset + ctr]);
			this.children[offset + ctr] = null;
		}
		//It is expected that after removal children array should have tuples of size 2t   
		return true;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	final Node<K, E> getChild(int idx, FileChannel fileChannel, Header<K, E> header) throws IOException {
		this.children[idx].read(fileChannel, header);
		return this.children[idx];
	}

	final void merge(Node<K, E> tnode) {
		int i = 0; 
		for (;i < tnode.keyTally; i++) {
			if(!tnode.isLeaf) {
				this.setChild(this.keyTally, tnode.children[i]);
			}
			this.setTuple(this.keyTally, tnode.tuples[i]);			
		}
		if(!tnode.isLeaf) {
			this.setChild(this.keyTally, tnode.children[i]);	
		}
	}

}
