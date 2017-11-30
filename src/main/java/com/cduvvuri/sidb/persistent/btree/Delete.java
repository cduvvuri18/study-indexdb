package com.cduvvuri.sidb.persistent.btree;

import java.io.IOException;

/**
 * 
 * @author Chaitanya DS
 * 27-Nov-2017
 */
final class Delete<K extends Comparable<K>, E> {
	private BTree<K, E> btree;

	static <K extends Comparable<K>, E> Delete<K, E> getNewInstance(BTree<K, E> btree) {
		return new Delete<K, E>(btree);
	}

	Delete(BTree<K, E> btree) {
		this.btree = btree;
	}

	//Reference Adam drozdek
	//https://www.cs.usfca.edu/~galles/visualization/BTree.html
	//TODO Lots of redundant code. Have to optimize
	void delete(K k) throws IOException {
		//Look up for the node
		TupleMetadata<K, E> tm = btree.search.search(k);

		//target node
		Node<K, E> tnode = tm.node;

		//return if node is null		
		if (tnode == null) {
			return;
		}

		if (!tnode.isLeaf) {
			TupleMetadata<K, E> stm = btree.successor.get(k);
			//Deep copy the successor into the node that contains the tuple which has the key
			tm.node.overrideTuple(tm.pos, stm.node.tuples[stm.pos]);

			//Persist the updated tuple. 
			//TODO - Analyze if we can move the below line into setTuple API 
			tm.node.write(btree.fileChannel, btree.header);

			stm.node.deleteTuple(stm.pos);

			//target node will be successor node
			tnode = stm.node;
		} else {
			tm.node.deleteTuple(tm.pos);
		}

		while (true) {
			//if the node is root and which do not have any more keys, all clear
			if(tnode.keyTally == 0 && tnode == btree.rootNode) {
				//persist
				tnode.write(btree.fileChannel, btree.header);
				return;
			}
			
			//if the node does not underflow
			if (tnode.keyTally >= btree.header.minOrder) {
				//persist
				tnode.write(btree.fileChannel, btree.header);
				return;
			}

			Node<K, E> tnodeParent = tnode.parent;

			//Look up for fill data in the younger and older sibling
			//TODO We can avoid the below traversal by maintaining the node pos w.r.t parent in TupleMetadata
			int tnodepos = 0;
			for (; tnodepos < tnodeParent.keyTally; tnodepos++) {
				if (tnodeParent.children[tnodepos].fpos == tnode.fpos) {
					break;
				}
			}

			int lpos = tnodepos - 1;
			int rpos = tnodepos + 1;

			Node<K, E> lnode = null;
			Node<K, E> rnode = null;

			//Edge conditions
			if(lpos >= 0) {				
				lnode = tnodeParent.getChild(lpos, btree.fileChannel, btree.header);
			}

			if(rpos <= tnodeParent.keyTally) {
				rnode = tnodeParent.getChild(rpos, btree.fileChannel, btree.header);	
			}

			//If the left node has enough elements to shuffle
			if (lnode != null && lnode.keyTally > btree.header.minOrder) {
				Tuple<K, E> pTuple = tnodeParent.tuples[lpos];
				//Copy from parent to target node
				tnode.setTuple(0, pTuple);
				
				if(!lnode.isLeaf) {
					tnode.setChild(0, lnode.getChild(lnode.keyTally, btree.fileChannel, btree.header));
					lnode.deleteChild(lnode.keyTally);
				}
				
				//Move element from left sibling to parent node
				Tuple<K, E> lTuple = lnode.tuples[lnode.keyTally - 1];
				lnode.deleteTuple(lnode.keyTally - 1);
				
				tnodeParent.overrideTuple(lpos, lTuple);

				//persist
				tnode.write(btree.fileChannel, btree.header);
				lnode.write(btree.fileChannel, btree.header);
				tnodeParent.write(btree.fileChannel, btree.header);
				return;
			}

			//If the right node has enough elements to shuffle
			if (rnode != null && rnode.keyTally > btree.header.minOrder) {
				//Get tuple from parent
				Tuple<K, E> pTuple = tnodeParent.tuples[tnodepos];

				//Copy from parent to target node
				tnode.setTuple(tnode.keyTally, pTuple);
				
				if(!rnode.isLeaf) {
					tnode.setChild(tnode.keyTally, rnode.getChild(0, btree.fileChannel, btree.header));
					rnode.deleteChild(0);
				}
				
				//Move element from right sibling to parent node
				tnodeParent.overrideTuple(tnodepos, rnode.tuples[0]);
				
				//delete
				rnode.deleteTuple(0);

				//persist
				tnode.write(btree.fileChannel, btree.header);
				rnode.write(btree.fileChannel, btree.header);
				tnodeParent.write(btree.fileChannel, btree.header);
				
				return;
			}

			//If the parent is root node
			if (tnode.parent == btree.rootNode) {
				if (btree.rootNode.keyTally == 1) {
					//Merge node, its sibling, and the parent to form a new root
					if (lnode != null) {
						Tuple<K, E> pTuple = tnodeParent.tuples[lpos];
						lnode.setTuple(lnode.keyTally, pTuple);
						lnode.merge(tnode);
						tnodeParent.deleteTuple(lpos);

						//lnode is the new root
						lnode.fpos = btree.rootNode.fpos;
						btree.rootNode = lnode;
						btree.rootNode.parent = null;
						
						//persist
						btree.rootNode.write(btree.fileChannel, btree.header);
						
					} else if(rnode != null) {
						Tuple<K, E> pTuple = tnodeParent.tuples[tnodepos];
						tnode.setTuple(tnode.keyTally, pTuple);
						tnode.merge(rnode);
						tnodeParent.deleteTuple(tnodepos);
						
						//tnode is the new root
						tnode.fpos = btree.rootNode.fpos;
						btree.rootNode = tnode;
						btree.rootNode.parent = null;
						
						//persist
						btree.rootNode.write(btree.fileChannel, btree.header);
					}					
				} else {
					if (lnode != null) {
						Tuple<K, E> pTuple = tnodeParent.tuples[lpos];
						lnode.setTuple(lnode.keyTally, pTuple);
						lnode.merge(tnode);
						tnodeParent.deleteChild(tnodepos);
						tnodeParent.deleteTuple(lpos);						
						
						//persist
						tnodeParent.write(btree.fileChannel, btree.header);
						lnode.write(btree.fileChannel, btree.header);
					} else if(rnode != null) {
						Tuple<K, E> pTuple = tnodeParent.tuples[tnodepos];
						tnode.setTuple(tnode.keyTally, pTuple);
						tnode.merge(rnode);
						tnodeParent.deleteChild(rpos);
						tnodeParent.deleteTuple(tnodepos);
						
						//persist
						tnodeParent.write(btree.fileChannel, btree.header);
						tnode.write(btree.fileChannel, btree.header);
					}					
				}
				return;
			}
			
			if (lnode != null) {
				Tuple<K, E> pTuple = tnodeParent.tuples[lpos];
				lnode.setTuple(lnode.keyTally, pTuple);
				lnode.merge(tnode);
				tnodeParent.deleteChild(tnodepos);
				tnodeParent.deleteTuple(lpos);
				
				//persist
				tnodeParent.write(btree.fileChannel, btree.header);
				lnode.write(btree.fileChannel, btree.header);

			} else if(rnode != null) {
				Tuple<K, E> pTuple = tnodeParent.tuples[tnodepos];
				tnode.setTuple(tnode.keyTally, pTuple);
				tnode.merge(rnode);
				tnodeParent.deleteChild(rpos);
				tnodeParent.deleteTuple(tnodepos);
				
				//persist
				tnodeParent.write(btree.fileChannel, btree.header);
				tnode.write(btree.fileChannel, btree.header);

			}
			
			tnode = tnode.parent;
		}
	}
}
