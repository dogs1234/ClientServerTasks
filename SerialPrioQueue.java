package cs671.eval;

import java.io.*;
import java.util.*;
import java.io.Serializable;

/**
 * Priority Queue implementation, modified from the sample code given on Piazza.
 *
 */
public class SerialPrioQueue implements Serializable {
  private transient Node[] elements;
  private transient int lastIndex;
  private transient int maxIndex;
  private final transient int MAXSIZE = 20000;

  public transient Node head;
  public transient int size ;

  /**
   * Constructor
   */
  public SerialPrioQueue() {
    elements = new Node[MAXSIZE];
    lastIndex = -1;
    maxIndex = MAXSIZE - 1;
    size = 0;
  }

  /**
   * Returns true/false depending on if queue is empty
   * @return true if Queue is empty
   */
  public boolean isEmpty() {
    return (lastIndex == -1);
  }

  /**
   * Add a node to the heap.
   * @param key priority
   * @param val value
   */
  public void add( Comparable key, Object val ) {
    Node node = new Node( key, val );
    if (lastIndex == maxIndex) {
      // Used when reading in, set up new structure
      elements = new Node[20000];
      lastIndex = -1;
      maxIndex = MAXSIZE - 1;
      size = 0;
    }
    size = size + 1;
    lastIndex = lastIndex + 1;
    reheapUp( node );
    head = elements[0];
  }

  /**
   * Removes a node from the heap
   * @return the value of the node removed
   */
  public Object remove() {
    Node hold;
    Node toMove;
    if (lastIndex == -1) {
      return null;
    }
    hold = elements[0];
    toMove = elements[lastIndex];
    size = size - 1;
    lastIndex = lastIndex - 1;
    reheapDown(toMove);
    head = elements[0];
    return hold.val;
  }

  /**
   * toString, outputting the priority and the val of each node.
   * @return the string output
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    LinkedList<Node> nodes = new LinkedList<Node>();
    toStringHelper( 0 , nodes );
    for( Node n : nodes ) {
      sb.append( "[" + n.priority + ", " + n.val + "]");
    }
    return sb.toString();
  }

  /**
   * The size of the priority queue
   * @return the size
   */
  public int size() {
    return size;
  }

  /**
   * Returns the priority queue in preOrder
   * @return array of node vals in pre order
   */
  public Object[] preOrder() {
    LinkedList<Object> list = new LinkedList<Object>();
    preHelper( 0, list );
    return list.toArray();
  }

  /**
   * Reads the object in from an in stream
   * @param in in stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int n = in.readInt();
    for ( int i = 0; i<n; i++) {
      Object prio = in.readObject();
      Object val = in.readObject();
      add( (Comparable) prio, val );
    }
  }

  /**
   * Writes the object to an outstream
   * @param out outstream
   * @throws IOException
   */
  private void writeObject( ObjectOutputStream out ) throws IOException {
    out.defaultWriteObject();
    out.writeInt(size);
    for ( int i = 0; i < size; i++  ) {
      out.writeObject( elements[i].priority );
      out.writeObject( elements[i].val );
    }
    out.flush();
  }

  /**
   * Inner node class
   */
  public static class Node {
    public Node left;
    public Node right;
    public Comparable priority;
    public Object val;

    public Node( Comparable k, Object v ) {
      priority = k;
      val = v;
      left = null;
      right = null;
    }
  }

  /**
   * If Priority Queue is full
   * @return true if full
   */
  private boolean isFull() {
    return (lastIndex == maxIndex);
  }

  /**
   * Re heaps the binary heap with a newly added node, finds the proper place for it.
   * @param node  the node to add to the binary heap.
   */
  private void reheapUp( Node node ) {
    int hole = lastIndex;
    while ((hole > 0) && ( node.priority.compareTo(elements[(hole - 1) / 2].priority ) > 0))  {
      elements[hole] = elements[(hole - 1) / 2];
      hole = (hole - 1) / 2;
    }
    elements[hole] = node;
  }

  /**
   * Recursive helper for preOrder
   * @param i
   * @param list
   */
  private void preHelper( int i, LinkedList<Object> list ) {
    if( elements[i] == null )
      return;
    list.add(elements[i].val);
    preHelper( (i*2+1), list  );
    preHelper( (i*2+2), list  );
  }

  /**
   * Recursive helper function for the toString method
   * @param i current node index
   * @param nodes list of nodes to add to
   */
  private void toStringHelper( int i, LinkedList<Node> nodes ) {
    if( elements[i] == null )
      return;
    nodes.add(elements[i] );
    toStringHelper((i * 2 + 1), nodes);
    toStringHelper((i * 2 + 2), nodes);
  }

  /**
   * The original toString method
   * @return String output of the priority queue
   */
  private String oldToString() {
    String theHeap = new String("the heap is:\n");
    for (int index = 0; index <= lastIndex; index++)
      theHeap = theHeap + index + ". " + elements[index].priority + ", " + elements[index].val + "\n";
    return theHeap;
  }

  /**
   * Finds the newest hole
   * @param hole hole position
   * @param node node
   * @return hole
   */
  private int newHole(int hole, Node node ) {
    int left = (hole * 2) + 1;
    int right = (hole * 2) + 2;

    if (left > lastIndex)
      return hole;
    else if (left == lastIndex)
      if (node.priority.compareTo(elements[left].priority) < 0)
        return left;
      else
        return hole;
    else if (elements[left].priority.compareTo(elements[right].priority) < 0)
      if (elements[right].priority.compareTo(node.priority) <= 0)
        return hole;
      else
        return right;
    else if (elements[left].priority.compareTo( node.priority ) <= 0)
      return hole;
    else
      return left;
  }

  /**
   * Reheaps the binary heap down
   * @param node the node to move
   */
  private void reheapDown(Node node ) {
    int hole = 0;
    int newhole;
    newhole = newHole(hole, node);   // find next hole
    while (newhole != hole) {
      elements[hole] = elements[newhole];  // move element up
      hole = newhole;                      // move hole down
      newhole = newHole(hole, node);       // find next hole
    }
    elements[hole] = node;           // fill in the final hole
  }

  /*
  public static void main ( String args[] ) {
    System.err.println( "... PrioQueue Test Main ...");

    SerialPrioQueue pq1 = new SerialPrioQueue();

    pq1.add( "A", "A" );
    pq1.add( "B", "B" );
    pq1.add( "C", "C" );
    pq1.add( "W", "W" );
    pq1.add( "M", "M" );
    pq1.add( "E", "E" );
    pq1.add( "G", "G" );
    pq1.add( "K", "K" );
    pq1.add( "Q", "Q" );

    System.err.println( pq1 );
    System.err.println( "Before serialize root is : " + pq1.head.val + " and size is " + pq1.size );
    System.err.println( pq1 );

    try {
      OutputStream file = new FileOutputStream( "pqSerialize.txt");
      ObjectOutputStream out = new ObjectOutputStream( file );
      out.writeObject( pq1 );
      out.close();
      file.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }


    SerialPrioQueue inPQ = null;

    try {
      FileInputStream fileIn = new FileInputStream( "pqSerialize.txt");
      ObjectInputStream in = new ObjectInputStream( fileIn );
      inPQ = (SerialPrioQueue) in.readObject();
      in.close();
      fileIn.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (ClassNotFoundException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    System.err.println( "And back in :  ");
    inPQ.toString();
    System.err.println( inPQ.head.val );
    System.err.println( inPQ.size );
    System.err.println( inPQ );
  }
  */
}