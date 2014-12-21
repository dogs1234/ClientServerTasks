package cs671.eval;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class EvalServer implements Runnable {
  public final int port;                  // port number to listen on
  public ServerSocket serv;               // server socket to lisetn on for new clients
  public LinkedList<EvalTask> work;       // Tasks remaining to be distributed to clients
  public ArrayList<Object> results;       // List of results from client computations
  public ArrayList<Thread> clients;       // Threads for each client connection
  private boolean isInitialized = false;
  private Object workLock = new Object();
  private Object resultsLock = new Object();


  /**
   * EvalServer Constructor, saves port number.
   * @param p
   */
  public EvalServer( int p ) {
    port = p;
  }

  /**
   * Initializes data structures for the server and opens the socket. Sets the timeout for listening per attempt to 10
   * seconds. Gracefully handles any exceptions that arise when attempting to start the server socket,
   * printing error messages.
   */
  public void initialize() {
    isInitialized = true;
    work = new LinkedList<EvalTask>();
    results = new ArrayList<Object>();
    clients = new ArrayList<Thread>();
    try {
      serv = new ServerSocket( port );
//      serv.setSoTimeout( 10000 );
      serv.setSoTimeout( 1000 );
    } catch (IOException e) {
      System.err.println( "Exception : EvalServer.initialize() : could not initialize on port " + port );
    }
  }

  /**
   * Main loop of the server. While there is work to be done or clients are still connected, continues to listen for
   * new clients. Will need to listen only for 10 seconds at a time so it can check for whether it should keep running.
   * When a new client connects, creates a new EvalConnection and starts a new thread to run it. Adds the thread to the
   * clients list and continues to listen for new clients.
   */
  public void run() {

    if( !isInitialized )
      throw new IllegalStateException("EvalServer is not initialized");
    if( !hasWork() )
      return;

    Socket clientSocket = null;
    EvalConnection evalConnection;

    do {
      try {
        clientSocket = serv.accept();
        evalConnection = new EvalConnection( clientSocket );
        evalConnection.initialize();
        Thread t = new Thread( evalConnection );
        t.start();
        clients.add( t );
      } catch (IOException e) {
        Exception( "IOException, in EvalServer.run()." );
        break;
      }
    } while( hasWork() && clientsActive() );

    try {
      serv.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    
  }

  /**
   * Checks whether any clients are still connected and running by checking the status of the threads
   * running their EvalConnection.
   * @return
   */
  public boolean clientsActive( ) {
    for( Thread t : clients ) {
      if( t.isAlive())
        return true;
    }
    return false;
  }

  /**
   * Adds new EvalTask objects to the work list. Can be called by multiple threads.
   * @param t
   */
  public void addWork( EvalTask t ) {
    synchronized ( this ) {
      if( !isInitialized )
        throw new IllegalStateException( "Add work called before initialize in EvalServer ");
      work.push( t );
    }
  }


  /**
   * Adds a result to the server's result list
   */
  public synchronized void addResultToServer( Object o ) {
    synchronized ( this ){
      results.add(o);
    }
  }

  /**
   * Gets a EvalTask object from the work list. Can be called by multiple threads.
   * @return
   */
  public EvalTask getWork() {
    synchronized ( this ) {
      if( !isInitialized )
        throw new IllegalStateException("EvalServer.getWork called with empty work list or not initialized in EvalServer");
      if( work.isEmpty() ) {
        System.err.println( "\t\t\tresults.size: " + results.size() );
        System.err.println( "\t\t\twork.size: " + work.size() );
        throw new IllegalStateException( "EvalServer.getWork is empty ");
      }
      return work.pop();
    }
  }

  /**
   * Checks whether the work list is empty. Can be called by multiple threads.
   * @return
   */
  public boolean hasWork() {
    synchronized ( this ) {
      if( !isInitialized )
        throw new IllegalStateException( "hasWork called before initialize in EvalServer");
      if( work.isEmpty() ) {
        return false;
      }
      return true;
    }
  }

  private static void Exception( String s ) { System.err.println( "Exception : " + s ); }

  /**
   * Main method for running the server from command line. Creates a new server using the port and input file given at
   * command line. Reads the data structures and methods from the input file and adds them to the work list. Starts the
   * server in a new Thread.
   *
   *  Note that for memory efficiency, it would be best to make sure that objects with the same data are not stored as
   *  copies, but rather references to one copy.
   */
  public static void main( String[] args ) {
    EvalServer server    = new EvalServer( Integer.valueOf(args[0]) );
    Initializer factory  = new Initializer();
    server.initialize();

    try {
      factory.loadLines( args[1]);
      do {
        factory.loadNextDataStructure();
        factory.setUpMethArgs();
        factory.instantiateDataStructure();
        factory.createEvalTask();
      } while( !factory.linesIsEmpty() );
    } catch (ClassNotFoundException e){
      Exception("ClassNotFoundE "); e.printStackTrace( new PrintStream(System.err));
    } catch (NoSuchMethodException e){
      Exception("NoSuchMethod "); e.printStackTrace( new PrintStream(System.err));
    } catch (InvocationTargetException e){
      Exception("InvocationTarget "); e.printStackTrace( new PrintStream(System.err));
    } catch (InstantiationException e){
      Exception("Instantiation "); e.printStackTrace( new PrintStream(System.err));
    } catch (IllegalAccessException e){
      Exception("IllegalAccess "); e.printStackTrace( new PrintStream(System.err));
    } catch (FileNotFoundException e) {
      Exception("FileNotFoundException "); e.printStackTrace(); }


    for( EvalTask t : factory.evalTasks )
      server.addWork( t );

    new Thread ( server, "Server" ).start();
  }



  /**
   * Inner class to handle communication with one specific client. Communicates to clients whether work is remaining,
   * sends serialized EvalTask objects to be worked on, and receives results until a new String object containing only
   * "hasWork" is sent.
   */
  public class EvalConnection implements Runnable {
    public ObjectInputStream in;
    public ObjectOutputStream out;
    private boolean isInitialized = false;
    private boolean isRunning = false;
    public Socket connection;

    /**
     * Constructor
     * @param s
     */
    public EvalConnection( Socket s ) {
      connection = s;
    }

    /**
     * Initializes input and output streams. Gracefully handles any exceptions that arise when attempting to connect
     * and set up streams, printing error messages.
     */
    public void initialize() {
      System.err.println("EvalServer.initialize() called.");
      isInitialized = true;
      isRunning = false;
      try {
        in = new ObjectInputStream( connection.getInputStream() );
        out = new ObjectOutputStream( connection.getOutputStream() );
        out.flush();
      } catch ( IOException e ) { e.printStackTrace(); }
    }

    /**
     * Adds an object to the results list in thread-safe manner.
     */
    public void addResult( Object o ) {
//      synchronized ( results ) {
//        if( !isInitialized )
//          throw new IllegalStateException( "EvalConnection, add result called before isInitialized.");
//        results.add( o );
//      }
      addResultToServer( o );
    }

    /**
     * Main loop of the connection. First, attempts to receive the String "hasWork" from the client and sends true or
     * false based on whether the server has remaining work to send or not. Gets an EvalTask from the server and sends
     * a serialized version to the client over the socket. Then waits for result data to be sent and adds it to the
     * results list. Continues listening for results until the String "hasWork" is sent again. If no work remains or the
     * client disconnects, the method exits.
     */
    public void run() {
      System.err.println( "EvalServer run called.");
      if( !isInitialized )
        throw new IllegalStateException( "EvalConnection, run called before initialized.");
      isRunning = true;
      String message = null;

      while( connection.isConnected() ) {
        try {
          if( in.readUTF().equals( "hasWork" )) {
            boolean hw = hasWork();
            out.writeBoolean( hw );
            out.flush();
            if( !hw ) {
              System.err.println( "       BREAKING OUT OF LOOP   !!!         ");
              break;
            }

            EvalTask task = getWork();
            out.writeObject( task );
            out.flush();


            // Waits for result data and add it to the results list
            Object[] result = (Object[]) in.readObject();
            for( Object o : result )
              addResult(o);
          }
        } catch (IOException e) {
          Exception( "EvalConnect.run(): Failed to read message in from input stream. Breaking out of loop.");
          break;
        } catch (ClassNotFoundException e) {
          Exception( "EvalConnection.run() : " );
        } catch (IllegalStateException e ) {
          Exception ("getWork() when there is no work.");
        }
      }
      System.err.println( "EVAL CLIENT IS EXITING RUN LOOP.");
    }
  }
}
