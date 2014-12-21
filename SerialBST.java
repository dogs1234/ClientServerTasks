package cs671.eval;

import java.io.*;
import java.util.*;

/**
 * A serializable BST implementation with an inner node class.
 * Lots of code was reused from a 416 lab I had completed.
 */
public class SerialBST implements Serializable {
  public transient Node root;
  public transient int size;

  /**
   * Constructor, sets root to null and size to 0 for default empty list.
   */
  public SerialBST() {
    root = null;
    size = 0;
  }

  /**
   * Adds a node to the tree
   * @param key comparable key
   * @param val value of the node
   */
  public void add( Comparable key, Object val){
    Node newNode = new Node( key, val );
    size++;
    /* Tree is empty, make new node root of tree */
    if( root == null ) {
      root = newNode;
      return;
    }
    Node curNode = root;
    while( true ) {
       if( key.compareTo( curNode.key ) <= 0 ) {
        if( curNode.left == null ) {
          curNode.left = newNode;
          break;
        } else {
          curNode = curNode.left;
        }
      }
      else if( key.compareTo( curNode.key ) > 0 ) {
        if( curNode.right == null ) {
          curNode.right = newNode;
          break;
        } else {
          curNode = curNode.right;
        }
      }
    }
  }

  /**
   * The size of the tree
   * @return
   */
  public int size() {
    return size;
  }

  /**
   * Balances the tree
   */
  public void balance() {
    ArrayList<Node> balancing = new ArrayList<Node>();
    toArrayList( root, balancing );
    root = null;
    size = 0;
    addArrayList(  0, balancing.size() - 1, balancing );
  }

  /**
   * Removes the node that matches the key and returns the value inside that node
   * @param key comparable
   * @return value inside the removed node
   */
  public Object remove( Comparable key ){
    if( root == null )
      return null;
    Node cur = root;
    Node par = null;

    try{
      for( int i = 0; i < size; i++ ) {
        int cmp = key.compareTo( cur.key );
//        System.err.println("Arg Key: " + key );
//        System.err.println("Node Key: " + cur.key );
        if( cmp < 0 ) {
          par = cur;
          cur = cur.left;
        }
        else if( cmp > 0 ) {
          par = cur;
          cur = cur.right;
        }
        else {
          /* found it */
          size--;
          break;
        }
      }
    } catch( NullPointerException e ) {
      throw new NoSuchElementException( "Not in tree " );
    }

    Object removedVal = cur.val;
    //removeNode( cur, par );
    if ( root.key.compareTo( cur.key ) == 0 ) // n is the root
      removeRoot( par );
    else if ( par.left != null && par.left.key.compareTo( cur.key ) == 0 )
      removeLeft( par, cur );
    else
      removeRight( par, cur );
    return removedVal;
  }



  /**
   * Finds and node and returns the value in it.
   * @param key comparable
   * @return value
   */
  public Object find( Comparable key ){
    Node found = findH( key, root );
    if( found == null )
      throw new NoSuchElementException( "Node with key not found");
    return found.val;
  }



  /**
   * Returns true if the tree is empty
   * @return
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Returns the tree in inOrder fashion
   * @return array of nodes in inOrder
   */
  public Object[] inOrder() {
    LinkedList<Object> list = new LinkedList<Object>();
    inHelper( root, list );
    return list.toArray();
  }


  /**
   * Gets the tree in postOrder
   * @return returns an array of objects in post order
   */
  public Object[] postOrder() {
    LinkedList<Object> list = new LinkedList<Object>();
    postHelper( root, list );
    return list.toArray();
  }

  /**
   * Gets the tree in pre Order
   * @return array of nodes in preorder
   */
  public Object[] preOrder() {
    LinkedList<Object> list = new LinkedList<Object>();
    preHelper( root, list );
    return list.toArray();
  }

  /**
   * toString method
   * @return preorder string representation of the tree
   */
  public String toString() {
    LinkedList<Node> nodes = new LinkedList<Node>();
    toStringHelper( root, nodes );
    StringBuilder sb = new StringBuilder();
    for( Node n : nodes )
      sb.append( "[" + n.key + ", " + n.val + "]" );

    return sb.toString();
  }

  /**
   * Read object from stream
   * @param in input stream
   */
  private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int n = in.readInt();
    for ( int i=0; i<n; i++) {
      Object key = in.readObject();
      Object val = in.readObject();
      add( (Comparable) key, val );
    }
  }

  /**
   * Writes the tree to a output stream
   *
   */
  private void writeObject( ObjectOutputStream out ) throws IOException {
    out.defaultWriteObject();
    out.writeInt( size );
    LinkedList<Node> list = new LinkedList<Node>();
    writer( root, list );
    for( Node n : list ) {
      out.writeObject( n.key );
      out.writeObject( n.val );
    }
  }

  /**
   * Inner class for a node in a binary search tree.
   */
  public static class Node {
    public Comparable key;
    public Object val;
    public Node left;
    public Node right;

    /**
     * Inner class constructor for a node with comparable object and a value ( or data ).
     * @param k is comparable key
     * @param v is the value, or data
     */
    public Node( Comparable k, Object v ) {
      key = k;
      val = v;
      left = null;
      right = null;
    }
  }

  /**
   * Helper for toString
   * @param node node
   * @param list list
   */
  private void toStringHelper( Node node, LinkedList<Node> list ) {
    if( node == null )
      return;
    list.add(node);
    toStringHelper(node.left, list);
    toStringHelper(node.right, list);
  }

  /**
   * balance helper
   * @param node node
   * @param balancing arraylist for balancing
   */
  private void toArrayList( Node node, ArrayList<Node> balancing ) {
    if ( node == null )
      return;
    toArrayList( node.left, balancing );
    balancing.add( node );
    toArrayList(node.right, balancing);
  }

  /**
   * Helper function for writeObject
   * @param node current node
   * @param listOfNodes all nodes so far
   */
  private void writer( Node node, LinkedList<Node> listOfNodes ) {
    if( node == null )
      return;
    listOfNodes.add( node );
    writer( node.left, listOfNodes );
    writer( node.right, listOfNodes );
  }

  private void inHelper( Node node, LinkedList<Object> list ) {
    if( node == null )
      return;
    inHelper( node.left, list );
    list.add(node.val);
    inHelper( node.right, list );
  }

  /**
   * Post order helper
   * @param node node
   * @param list list
   */
  private void postHelper( Node node, LinkedList<Object> list ) {
    if( node == null )
      return;
    postHelper( node.left, list  );
    postHelper( node.right, list  );
    list.add(node.val);
  }

  /**
   * Helper for preOrder
   * @param node node
   * @param list list
   */
  private void preHelper( Node node, LinkedList<Object> list ) {
    if( node == null )
      return;
    list.add(node.val);
    preHelper( node.left, list  );
    preHelper( node.right, list  );
  }

  /**
   * helper for finding the node
   * @param key comparable
   * @param n node
   * @return node
   */
  private Node findH( Comparable key, Node n ) {
    if( n == null )
      return null;
    int cmp = key.compareTo( n.key );
    if( cmp < 0 )
      return findH( key, n.left );
    else if( cmp > 0 )
      return findH( key, n.right );
    else
      return n;
  }

  /**
   * Helper for balancing
   */
  private void addArrayList( int start, int end, ArrayList<Node> balancing ) {
    int middleIndex = (end + start) / 2;
    if( start <= end ) {
      add( balancing.get(middleIndex).key, balancing.get(middleIndex).val );
      addArrayList( start, middleIndex-1, balancing );
      addArrayList( middleIndex+1, end, balancing );
    }
  }


  /**
   * Remove the root node
   */
  private void removeRoot( Node par ) {
    Node n = root;
    if( n.left == null ) {
      root = n.right;
    }
    else if( n.right == null ) {
      root = n.left;
    }
    else {
      addToFarRight( n.left, n.right );
      root = n.left;
    }
  }

  /**
   * Remove a node that is the left child of its parent
   */
  private void removeLeft( Node parent, Node n ) {
    if( n.left == null ) {
      parent.left = n.right;
    }
    else {
      parent.left = n.left;
      addToFarRight( parent.left, n.right );
    }
  }

  /**
   * Remove a node that is the right child of its parent
   */
  private void removeRight( Node parent, Node n ) {
    if( n.right == null ) {
      parent.right = n.left;
    }
    else {
      parent.right = n.right;
      addToFarLeft(parent.right, n.left);
    }
  }

  /**
   * add the subtree Node as the right most descendant of the 1st argument
   */
  private void addToFarRight( Node n, Node subtree ) {
    while( n.right != null )
      n = n.right;
    n.right = subtree;
  }

  /**
   * add the subtree Node as the left most descendant of the 1st argument
   */
  private void addToFarLeft( Node n, Node subtree ) {
    while( n.left != null )
      n = n.left;
    n.left = subtree;

  }


}
