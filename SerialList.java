package cs671.eval;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * A serializable linked-list implementation, using key and value pairs.
 *
 * @author Kyle Vickers
 */
public class SerialList implements Serializable {
  public transient Node head;
  public transient Node tail;
  public transient int size;

  /**
   * Empty Constructor
   */
  public SerialList() {
    head = null;
    tail = null;
    size = 0;
  }

  public SerialList( Node h, Node t, int s ) {
    head = h;
    tail = t;
    size = 0;
  }

  /**
   * Adds node with given key and value to the head of the list.
   * @param key a comparable key object
   * @param val the data for the node
   */
  public void add( Comparable key, Object val ) {
    Node newNode = new Node( key, val );
    size++;
    if( head == null ) {
      head = newNode;
      head.next = tail;
    } else {
      newNode.next = head;
      head = newNode;
    }
  }

  /**
   * Retrieves the data of the element at the given index.
   * @param index the index of the object
   * @return the object at the given index of the list
   */
  public Object get( int index ){
    if( index < 0 || index > size - 1 )
      throw new IndexOutOfBoundsException( "index < 0 or index is greater than size - 1 ");

    Node cur = head;
    for( int i = 0; i < size; i++ )
      if( i == index )
        return cur.val;
      else
        cur = cur.next;
    return cur.val;
  }

  /**
   * Retrieves the data of the first element with the given key.
   * @param key returns the item matching the key
   * @return the object found matching the key
   */
  public Object get( Comparable key ){
    Node cur = head;
    while( cur != null ) {
      if( cur.key.compareTo( key ) == 0 )
        return cur.val;
      else
        cur = cur.next;
    }
    throw new NoSuchElementException( "No element found with key : " + key );
  }

  /**
   * Removes the first element with the given index and returns the value from that node.
   * @param index the index of the item which needs to be deleted
   * @return the value that was in the removed node
   */
  public Object remove(int index){
    if( index < 0 || index > size - 1 )
      throw new IndexOutOfBoundsException( "index < 0 or index is greater than size - 1 ");

    Node cur = head;
    Node prev = null;
    Object ret = null;

    for( int i = 0; i < size; i++ ) {
      if( i == index ) {
        if( prev == null )
          head = head.next;
        else if( cur.next != null )
          prev.next = cur.next;
        else if( cur.next == null )
          prev.next = null;
        ret = cur.val;
        cur = null;
        size--;
        return ret;
      }
      prev = cur;
      cur = cur.next;
    }

    return null;
  }

  public Object remove(java.lang.Comparable key) {
    Node cur = head;
    Node prev = null;
    Object ret = null;

    for( int i = 0; i < size; i++ ) {
      if( key.compareTo( cur.key ) == 0 ) {
        if( prev == null )
          head = head.next;
        else if( cur.next != null )
          prev.next = cur.next;
        else if( cur.next == null )
          prev.next = null;
        ret = cur.val;
        cur = null;
        size--;
        return ret;
      }
      prev = cur;
      cur = cur.next;
    }

    throw new NoSuchElementException( "Key was not found in the list for removal : " + key );
  }

  /**
   * Sorts the linked list using a bubble sort
   */
  public void sort() {
    if( size == 0 || size == 1 )
      return;
    boolean swap = true;
    while (swap) {
      swap = false;
      Node cur = head;
      Node next = cur.next;
      Node prev = null;
      while ( next != null ) {
        if (cur.key.compareTo( next.key ) > 0) {
          swap = true;
          if (cur == head) {
            head = next;
            Node temp = next.next;
            next.next = cur;
            cur.next = temp;
            cur = head;
          } else {
            prev.next = cur.next;
            cur.next = next.next;
            next.next = cur;
            cur = next;
          }
        }
        prev = cur;
        cur = cur.next;
        next = cur.next;
      }
    }
  }

  /**
   * If the list is empty. Checks the size field integer, if it is 0 then the list is empty
   * @return true if list is of size 0
   */
  public boolean isEmpty(){
    return size == 0;
  }

  /**
   * The size of the list
   * @return The size of the list, which is the field value <code>size</code>
   */
  public int size(){
    return size;
  }

  /**
   * Returns a string representation of the list, with key and value pairs. Uses
   * string builder for string construction for when large lists are used.
   * @return the string representation of the list in the form ( 'key', 'val' )
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Node cur = head;
    while( cur != null ) {
      sb.append( "( " + cur.key + ", " + cur.val + "), ");
      cur = cur.next;
    }
    return sb.toString();
  }

  /**
   * Reads in the object from a serialized state. Reads it in efficiently.
   * @param in the object input stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int n = in.readInt();
    for ( int i=0; i<n; i++) {
      Object key = in.readObject();
      Object val = in.readObject();
      addLast( (Comparable) key, val );
    }
  }

  /**
   * Writes the object to the output stream
   * @param out the output stream.
   * @throws IOException
   */
  private void writeObject( ObjectOutputStream out ) throws IOException {
    out.defaultWriteObject(); // not strictly necessary
    out.writeInt(size);
    for (Node c = head; c != null; c = c.next) {
      out.writeObject( c.key );
      out.writeObject( c.val );
    }
    out.flush();
  }

  /**
   * Inner node class to store data.
   * Remember : Static classes cannot reference things from its outer class. So it can can only invoke
   * static methods or access static fields of an instance of the outer class.
   */
  public static class Node {
    public Comparable key;  // Key for the node
    public Object val;      // Data
    public Node next;       // Next node

    /**
     * Node constructor
     * @param k comparable key
     * @param v
     */
    public Node( Comparable k, Object v ) {
      key = k;
      val = v;
    }
  }

  /**
   * Adds a node to the end of the list. This is used for the serialization aspect.
   * @param key
   * @param val
   */
  private void addLast( Comparable key, Object val ) {
    if( head == null ) {
      head = new Node( key, val );
      tail = head;
    }
    else {
      tail.next = new Node( key, val);
      tail = tail.next;
    }
    size++;
  }

}
