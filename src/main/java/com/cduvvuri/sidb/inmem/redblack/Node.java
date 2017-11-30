package com.cduvvuri.sidb.inmem.redblack;

/**
 * 
 * @author Chaitanya DS
 * 21-Nov-2017
 */
final class Node<K extends Comparable<K>, E> {
	//data
	K k;
	E e;

	//left
	Node<K, E> l;
	//right
	Node<K, E> r;
	//parent
	Node<K, E> p;

	//default color
	Color color = null;

	boolean isNill;

	final class Next {
		Node<K, E> node;
		NextType type;

		Next(Node<K, E> node, NextType type) {
			this.node = node;
			this.type = type;
		}
	}

	enum NextType {
		L, R
	}

	enum RotateType {
		LL, RR, LR, RL
	}

	enum Color {
		RED, BLACK, DOUBLEBLACK
	}

	Node(K k, E e) {
		this.k = k;
		this.e = e;
		
		//TODO - Space optimization
		//We may not need to create nils for all the leaf nodes.
		this.l = new Node<K, E>();
		this.l.p = this;
		this.r = new Node<K, E>();
		this.r.p = this;
	}

	public Node() {
		this.isNill = true;
		this.color = Color.BLACK;
	}

	/**
	 * 
	 * @param k
	 * @return 'null' If the keys are equal; else left or right node
	 */
	final Next next(K k) {
		if (k == null) {
			return null;
		}

		// > 0 = returns right node
		// < 0 = returns left node
		return this.k.compareTo(k) > 0 ? new Next(this.l, NextType.L)
				: this.k.compareTo(k) < 0 ? new Next(this.r, NextType.R) : null;
	}

	//If both left and right are null.
	final boolean isLeaf() {
		return this.l.isNill && this.r.isNill;
	}

	//If both left and right are null.
	final boolean isInternalNode() {
		return !(this.l.isNill && this.r.isNill);
	}

	/**
	 * Set the next node
	 * @param ne
	 */
	final void setNext(Node<K, E> ne) {
		if (ne == null) {
			return;
		}

		if (this.k.compareTo(ne.k) > 0) {
			this.l = ne;
			ne.p = this;
		}

		if (this.k.compareTo(ne.k) < 0) {
			this.r = ne;
			ne.p = this;
		}
	}

	final Color getSiblingColor(Node<K, E> node) {
		if (this.l == node)
			return this.r.color;
		else
			return this.l.color;
	}

	//set the color of sibling node of input
	final void setSiblingColor(Node<K, E> node, Color color) {
		if (this.l == node)
			this.r.color = color;
		else
			this.l.color = color;
	}

	//If the current node has only one child then return that child else null
	final Next getOnlyChild() {
		if (this.isLeaf()) {
			return null;
		}
		if (this.r.isNill) {
			return new Next(this.l, NextType.L);
		}
		if (this.l.isNill) {
			return new Next(this.r, NextType.R);
		}
		return null;
	}

	final boolean isBlack() {
		if (this.color == Color.BLACK) {
			return true;
		}
		return false;
	}

	final boolean isRed() {
		if (this.color == Color.RED) {
			return true;
		}
		return false;
	}

	final boolean isDoubleBlack() {
		if (this.color == Color.DOUBLEBLACK) {
			return true;
		}
		return false;
	}

	final void setLeft(Node<K, E> node) {
		this.l = node;
		this.l.p = this;
	}

	final void setRight(Node<K, E> node) {
		this.r = node;
		this.r.p = this;
	}

	final Next getSibling(Node<K, E> parent) {
		if (parent.l == this)
			return new Next(parent.r, NextType.R);
		else if (parent.r == this)
			return new Next(parent.l, NextType.L);
		return null;
	}
}
