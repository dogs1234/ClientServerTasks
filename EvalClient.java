package cs671.eval;

import java.io.*;
import java.lang.reflect.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * EvalClient used to do work from EvalServer and send it back as an array of results
 */
public class EvalClient implements Runnable{
  public String address;
  public Socket connection;
  public ObjectInputStream in;
  public ObjectOutputStream out;
  public int port;

  private boolean isInitialized = false;

  /**
   * Constructor
   * @param a address of the host
   * @param p port number
   */
  public EvalClient( String a, int p ) {
    address = a;
    port = p;
  }

  /**
   * Initializes data structures for the client and establishes the connection to the server.
   * Sets up input and output streams. Gracefully handles any exceptions that arise when
   * attempting to connect and set up streams, printing error messages.
   */
  public void initialize() {
    isInitialized = true;
    try {
      connection = new Socket( address, port );
      out = new ObjectOutputStream( connection.getOutputStream() );
      out.flush();
      in = new ObjectInputStream( connection.getInputStream() );
    } catch (IOException e) {
      System.err.println("Can not connect to server");
    }
  }

  /**
   * Asks the server whether more work is available by sending the String "hasWork", and receiving a Boolean value of
   * either true or false. Handles ClassNotFoundException and IOException gracefully, printing an error message.
   * @return
   */
  public boolean checkWork() {
    boolean hasWork = false;
    try {
      out.writeUTF("hasWork");     // Here is where we write back to eval connection and get a exception.
      out.flush();
      hasWork = in.readBoolean();
    } catch ( IOException e) { Exception( "IOException in EvalClient.checkWork." );
    } finally {
      return hasWork;
    }
  }

  /**
   * Receives new work from the server, confirms it is a valid EvalTask, and casts it in order to return it. Handles
   * ClassNotFoundException and IOException gracefully, printing an error message.
   * @return
   */
  public EvalTask getWork() {
    try {
      EvalTask et = (EvalTask) in.readObject();
      if( EvalTask.class.equals( et.getClass() ) )
        if( et.target != null )
          return et;

    } catch (IOException e) {
      Exception( "EvalClient.getWork() io exception raised " );
    } catch (ClassNotFoundException e) {
      Exception( "EvalClient.getWork() ClassNotFoundException raised ");
    }

    return null;
  }

  /**                                                         +
   * Checks whether a specific method is valid for a given set of arguments. Because all arguments are given as objects,
   * it is necessary to check whether the argument types are primitives and then check whether the parameter is of the
   * object version appropriate for that primitive. The TYPE field will be useful for this, as it belongs to all
   * classes which corellate to a primitive type. Should handle all exceptions gracefully and return the correct value
   * still when exceptions occur.
   *
   *
   * @return
   */
  public boolean checkMethod( String name, Method m, Object[] args ) {
    LinkedList<Class<?>> yourParameterTypes = new LinkedList<Class<?>>(Arrays.asList( m.getParameterTypes() ) );
    Iterator<Class<?>> yourIt = yourParameterTypes.iterator();
    LinkedList<Object>   myArgumentObjects  = new LinkedList<Object>( Arrays.asList( args ) );
    Iterator<Object> myIt = myArgumentObjects.iterator();

    final int MATCHES_NEEDED = args.length;
    int matched = 0;

    if( !m.getName().equals( name ) )
      return false;

    if( yourParameterTypes.size() != myArgumentObjects.size() )
      return false;

    while( myIt.hasNext() ) {
      Object myObject = myIt.next();
      while( yourIt.hasNext() ) {
        Class<?> yourClass = yourIt.next();

        if( primitiveCheck( myObject.getClass(), yourClass ) ) {
          matched++;
          break;
        }
        if( yourClass.isAssignableFrom( myObject.getClass() ) ) {
          matched++;
          break;
        }
      }
    }

    if( matched == MATCHES_NEEDED ) {
      return true;
    }

    return false;
  }

  /**
   * For the method name and set of arguments args, the client attempts to find a method with that signature. If none
   * is found, it should return an error String to notify the server of the problem. If the method is found, the client
   * invokes it and returns the result. For methods with no return value, the target is returned, under the assumption
   * that the method likely altered it.
   *
   * @param c
   * @param target
   * @param name
   * @param args
   */
  public Object doTask( Class c, Object target, String name, Object[] args ) {
    for( Method m : c.getDeclaredMethods() ) {
      if( m.getName().equals( name ) ) {
        if( checkMethod( name, m, args ) ) {
          try {
            return invokeTheMethod( m, target, args );
          } catch (IllegalAccessException e) {
            Exception("IllegalAccessException on method: " + m.getName() );
          } catch (InvocationTargetException e) {
            Exception("Invocation Target Exception on method: " + m.getName() );
          }
        }
      }
    }
    // Failed to invoke a method if here
    return "Bad test was bad.";
  }

  /**
   * Main loop of the client. Uses the checkWork method to test whether there is more work to be done.
   * If there is more work, receives a serialized EvalTask from the server. Uses doTask to invoke each method and sends
   * the result or relevant error message to the server. When no work remains or the connection is closed, client exits.
   * Should handle all exceptions gracefully, printing a descriptive error message, sending an error message
   * String notifying the server of the problem, and moving on to the next method or task.
   */
  public void run() {
    if( !isInitialized )
      throw new IllegalStateException( "EXCEPTION: EvalClient run method called before initialized");

    while( checkWork() ) {
      EvalTask task = getWork();
      ArrayList<Object> workDone = new ArrayList<Object>();
      for( int i = 0; i < task.methods.length; i++ ) {
        Object taskDone = doTask( task.target.getClass(), task.target, task.methods[i], task.args[i] );
        if( taskDone != null )
          workDone.add( taskDone );
        else
          workDone.add( new String( "Error: Failed invocation"));
      }
      try {
        out.writeObject( workDone.toArray() );
        out.flush();
      } catch (IOException e) {
        Exception( "EvalClient.run()| FAILED: Write result array back to EvalConnection");
      }
    }
  }

  /**
   * Main method for running the client from command line. Creates a new client using hostname/address and port given
   * at command line. Starts the client in a new Thread.
   */
  public static void main( String args[] ) {
    EvalClient client = new EvalClient( args[0], Integer.parseInt(args[1]) );
    client.initialize();
    Thread t = new Thread( client );
    t.start();
  }

  /**
   * Prints an exception out conveniently.
   * @param s String to display with exception name.
   */
  private void Exception( String s ) {
    System.err.println( "EXCEPTION : " + s );
  }

  /**
   * Attempts to invoke a method. Returns the target if method is void
   * @param method method to invoke
   * @param target object to invoke the method on
   * @param args arguments to the method
   * @return the invoked result
   * @throws IllegalAccessException if method cant be accessed
   * @throws InvocationTargetException if the method cannot be invoked
   */
  private Object invokeTheMethod( Method method, Object target, Object[] args )
      throws IllegalAccessException, InvocationTargetException {
    method.setAccessible( true );
    if( method.getReturnType().equals( Void.TYPE ) ) {
      method.invoke( target, args );
      return target;
    } else {
      return method.invoke( target, args );
    }
  }

  /**
   * Checks to see if your class is equivalent to "Wrapper.TYPE" and
   * returns true if my class is equivelent to the corresponding Wrapper.class
   * @param mine my class
   * @param yours your class
   * @return true if your .TYPE is equiv to my Class.class
   */
  private boolean primitiveCheck( Class mine , Class yours ) {
    if (yours.equals( java.lang.Integer.TYPE ))
      return mine.equals( java.lang.Integer.class );
    if ( yours.equals( java.lang.Double.TYPE ))
      return mine.equals( java.lang.Double.class );
    if ( yours.equals( java.lang.Character.TYPE ))
      return mine.equals( java.lang.Character.class );
    if ( yours.equals( java.lang.Boolean.TYPE ))
      return mine.equals( java.lang.Boolean.class ) ;
    if ( yours.equals( java.lang.Short.TYPE ))
      return mine.equals( java.lang.Short.class );
    if ( yours.equals( java.lang.Float.TYPE ))
      return mine.equals( java.lang.Float.class );
    if ( yours.equals( java.lang.Long.TYPE ))
      return mine.equals( java.lang.Long.class );
    if ( yours.equals( java.lang.Byte.TYPE ))
      return mine.equals( java.lang.Byte.class );

    return false;
  }

}
