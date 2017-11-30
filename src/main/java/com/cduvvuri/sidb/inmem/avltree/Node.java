package com.cduvvuri.sidb.inmem.avltree;

/**
 * Node maintains the reference to left and right
 * @author Chaitanya DS
 * 17-Nov-2017
 */
final class Node<K extends Comparable<K>, E> {
	Node<K, E> l;
	Node<K, E> r;

	K k;
	E e;

	int h;

	final class Next {
		Node<K, E> node;
		NavigateType type;
		
		Next(Node<K, E> node, NavigateType type) {
			this.node = node;
			this.type = type;
		}
	}
	
	//L-left,R - right,E - equal
	enum NavigateType {
		L, R, E
	}
	
	Node(K k, E e) {
		this.k = k;
		this.e = e;
	}

	//Height of the tree from the left - Height of the tree from the right 
	final int degree() {
		return (l == null ? 0 : l.h + 1) - (r == null ? 0 : r.h + 1);//Didn't like this line
	}

	//degree shud be -1,0,1
	final boolean isViolation() {
		int degree = degree();

		if (degree < -1 || degree > 1) {
			return true;
		}

		return false;
	}

	//set the entry at the right place
	final void set(Node<K, E> entry) {
		//Update the entity If keys are equal
		if (entry.k.compareTo(this.k) == 0) {
			this.e = entry.e;
		} else if (entry.k.compareTo(this.k) < 0) {
			this.l = entry;
		} else {
			this.r = entry;
		}

		updateHeight();
	}

	//update the height of the node
	//I think I complicated this method a bit
	final void updateHeight() {
		if(l == null && r == null) {
			h = 0;
			return;
		}
		
		int lh = l == null ? 0 : l.h;
		int rh = r == null ? 0 : r.h;

		h = lh > rh ? lh : rh;
		++h;
	}

	//TODO Little confused If I shud return the same node or not If keys are equal
	//But at the end I liked the concept of having node.next API 
	//which is helping me to read my own code as and when I come back to review or fix something 
	final Next next(K key) {
		//If the keys are equal
		if (key.compareTo(this.k) == 0) {
			return new Next(this, NavigateType.E);
		} else if (key.compareTo(this.k) < 0) {
			return new Next(this.l, NavigateType.L);			
		} else {
			return new Next(this.r, NavigateType.R);			
		}
	}


	//TODO Little confused If I shud return the same node or not If keys are equal
	//But at the end I liked the concept of having node.next API 
	//which is helping me to read my own code as and when I come back to review or fix something 
	final Next next2(K key) {
		//If the keys are equal
		if (key.compareTo(this.k) < 0) {
			return new Next(this.l, NavigateType.L);			
		} else if(key.compareTo(this.k) > 0) {
			return new Next(this.r, NavigateType.R);			
		}
		
		return null;
	}
	
	//TODO Handle the case If the key are equal; Though the scenario should not occur
	final NavigateType navigateType(K k) {		
		if (k.compareTo(this.k) < 0) {
			return NavigateType.L;
		} else {
			return NavigateType.R;
		}
	}

	//Set the left element
	final void setRight(Node<K, E> right) {
		this.r = right;		
		updateHeight();
	}
	
	//Set the right element
	final void setLeft(Node<K, E> left) {
		this.l = left;
		updateHeight();
	}
	
	final boolean isLeaf() {
		return l == null && r == null;
	}
	
	final boolean hasOnlyOneChild() {
		return (l == null && r != null) || (l != null && r == null); 
	}
	
	final Node<K, E> getLargerHeightNext() {
		return (this.l.h >= this.r.h) ? this.l : this.r;
	}
}
