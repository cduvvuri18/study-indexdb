package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;
import java.util.Optional;

/**
 * 
 * @author Chaitanya DS
 * 17-Nov-2017
 */
final class Insert<K extends Comparable<K>, E> {
	private final BTree<K, E> btree;
	
	private Insert(BTree<K, E> btree) {
		this.btree = btree;
	}

	static<K extends Comparable<K>, E> Insert<K, E> getNewInstance(BTree<K, E> btree) {
		return new Insert<K, E>(btree);
	}
	
	//Reference - Adam drozdek, CLRS
	final boolean insert(Tuple<K, E> tuple) throws IOException {
		//Find the leaf node or update the node if the key already exists
		Optional<Node<K, E>> container = findTheLeafToInstOrUpdateNodeIfExists(btree.rootNode, tuple);
		
		//container will not have node if the key already exists
		if(!container.isPresent()) {
			return true;
		}

		Node<K, E> node = container.get();
		
		//New child to the upper half. This node is the resultant of the lower node split.
		Node<K, E> newChildResultOfLowerNodeSplit = null;

		while (true) {
			//Find the appropriate position to insert the element
			int pos = node.findPos(tuple);

			//If the node is not full, insert the element and write to disk
			if (node.keyTally < btree.header.order) {
				//Add the element
				node.setTuple(pos, tuple);
				
				//If the node is not leaf - split occurred in the bottom up traversal
				if (!node.isLeaf)
					node.setChild(pos + 1, newChildResultOfLowerNodeSplit);

				node.write(btree.fileChannel, btree.header);
				return true;
			} else {
				//split the node
				//node1 = node, node2 is new
				Node<K, E> newNode = btree.allocateNode();

				//Move the high elements into the new node. 
				//i.e.All the elements from above minOrder to order
				//node.moveSubsetOfTuples(newNode.tuples,  minOrder, order - minOrder);
				node.moveSubsetOftuples(newNode.tuples, btree.header.minOrder,
						btree.header.order - btree.header.minOrder);

				newNode.keyTally = btree.header.order - btree.header.minOrder;

				//If node is leaf then newNode will be leaf
				newNode.isLeaf = node.isLeaf;

				//initialize middle key
				Tuple<K, E> midTuple = getMidTuple(node, newNode, tuple, pos, newChildResultOfLowerNodeSplit);

				//Assign the parent to new node
				newNode.parent = node.parent;

				if (node == btree.rootNode) {
					Node<K, E> newRoot = btree.allocateNode();
					//Root is the result of split. It cann't be leaf 
					newRoot.isLeaf = false;
					
					//Set the parent
					node.parent = newRoot;
					newNode.parent = newRoot;

					//Root node is new. Element will be at the 0th position
					newRoot.setTuple(0, midTuple);

					//write root to the disk
					newRoot.setChild(0, node);
					newRoot.setChild(1, newNode);

					//Exchange fpos new,old for root
					long temp = newRoot.fpos;
					newRoot.fpos = node.fpos;
					node.fpos = temp;

					//Write to the disk. Analyze the data corruption part later.
					newRoot.isLoaded = true;
					newNode.isLoaded = true;
					newRoot.write(btree.fileChannel, btree.header);
					node.write(btree.fileChannel, btree.header);
					newNode.write(btree.fileChannel, btree.header);

					//set the newRoot
					btree.rootNode = newRoot;
					return true;
				} else {

					//Input to the upper node
					tuple = midTuple;
					newChildResultOfLowerNodeSplit = newNode;

					//Write to the disk. Analyze the data corruption part later.
					newNode.isLoaded = true;
					node.write(btree.fileChannel, btree.header);
					newNode.write(btree.fileChannel, btree.header);

					node = node.parent;
				}
			}
		}

		//return false;
	}


	/*
	 * -Set the new element
	 * -If the branch condition in the below API is satisfied put the element in the higher half else in the lower half
	 * -Pull up the middle key from the half which satisfied minDegree condition and in which we inserted the new entry.
	 * -pos Effective position before split
	 * */
	private final Tuple<K, E> getMidTuple(Node<K, E> node, Node<K, E> newNode, Tuple<K, E> tuple, int pos,
			Node<K, E> newChildResultOfLowerNodeSplit) {
		Tuple<K, E> midTuple = null;

		//Middle element in the higher half if branch is taken
		if (pos >= btree.header.minOrder) {
			int npos = pos - (newNode.keyTally - 1);
			newNode.setTuple(npos, tuple);
			midTuple = newNode.tuples[0];
			newNode.deleteTuple(0);

			//Align the children
			if (!newNode.isLeaf) {
				node.moveSubsetOfChilds(newNode, btree.header.minOrder + 1, btree.header.order - btree.header.minOrder);
				newNode.setChild(npos, newChildResultOfLowerNodeSplit);
			}

			return midTuple;
		}

		//Middle element in the lower half
		node.setTuple(pos, tuple);
		midTuple = node.tuples[node.keyTally - 1];
		node.deleteTuple(node.keyTally - 1);

		//Align the children
		if (!node.isLeaf) {
			node.moveSubsetOfChilds(newNode, btree.header.minOrder,
					/*child count*/(btree.header.order + 1) - btree.header.minOrder);
			node.setChild(pos + 1, newChildResultOfLowerNodeSplit);
		}

		return midTuple;
	}

	//Find the leaf node, util method for insert
	private final Optional<Node<K, E>> findTheLeafToInstOrUpdateNodeIfExists(Node<K, E> root, Tuple<K, E> tuple) throws IOException {
		Node<K, E> node = root;

		while (true) {
			int i = 0;

			while (i < node.keyTally && tuple.key.compareTo(node.tuples[i].key) > 0) {
				i = i + 1;
			}

			if(i < node.keyTally && tuple.key.compareTo(node.tuples[i].key) == 0) {
				node.overrideTuple(i, tuple);
				node.write(btree.fileChannel, btree.header);
				return Optional.empty();
			}
			
			if (node.isLeaf) {
				return Optional.of(node);
			}

			node.children[i].read(btree.fileChannel, btree.header);

			node = node.children[i];
		}
	}
}
