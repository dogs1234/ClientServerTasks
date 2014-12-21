package cs671.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Initializer class used in EvalServer main. This class deals with the loading data
 * from files, initializing the data properly using reflection, and creating
 * a list of EvalTasks to return.
 */
class Initializer {
  LinkedList<EvalTask> evalTasks;
  LinkedList<String> lines;
  LinkedList<String> keys;
  LinkedList<String> data;
  ArrayList<String> methods;
  String dataStructure = "" ; String typeOfKey = "" ; String typeOfData = "";
  String[] methArray;
  Object[][] argsArray;
  Object dsInstance;
  Map<String,Class> prims;

  /**
   * Set up a new Initializer, instantiate all lists needed.
   */
  Initializer() {
    evalTasks = new LinkedList<EvalTask>();
    keys = new LinkedList<String>();
    data = new LinkedList<String>();
    methods = new ArrayList<String>();
    lines = new LinkedList<String>();
    prims = new HashMap<String, Class>();
  }

  /**
   * Clears the previous lists and strings for a new EvalTask to be read in.
   */
  void cleanUp( ) {
    keys.clear();
    data.clear();
    methods.clear();
    dataStructure = "";
    typeOfKey = null;
    typeOfData = null;
  }

  /**
   * Loads all String lines from the given file into an array list to be used
   * by the initializer to create each new eval taks.
   * @param fileName The file name
   * @throws FileNotFoundException if the file is not found
   */
  void loadLines( String fileName ) throws FileNotFoundException {
    Scanner scan = new Scanner( new File(fileName));
    String nextLine;
    while( scan.hasNext() ) {
      nextLine = scan.nextLine().trim();
      if( !nextLine.equals(""))
        lines.add( nextLine );
    }
  }

  /**
   * Loads all of the lines needed for the next eval taks to be created.
   */
  void loadNextDataStructure() {
    boolean newFlag = false; boolean firstPass = true;
    String curLine;
    while( !lines.isEmpty() ) {
      curLine = lines.pollFirst();
      if( curLine.contains( "SerialBST" ) || curLine.contains( "SerialList") || curLine.contains( "SerialPrioQueue") ) {
        if( !firstPass ) {
          lines.addFirst( curLine );
          return; // Jump back out into main loop
        }
        firstPass = false;
        newFlag = true;
        Scanner lineReader = new Scanner( curLine );
        dataStructure  = lineReader.next();
        typeOfKey      = lineReader.next();
        typeOfData     = lineReader.next();
      }
      else if( newFlag ) {
        newFlag = false;
        Scanner lineReader = new Scanner( curLine );
        lineReader.useDelimiter("&");
        while( lineReader.hasNext() ) {
          keys.add(lineReader.next());
          data.add(lineReader.next());
        }
      }
      else {
        methods.add( curLine );
      }
    }
  }

  /**
   * Sets up all of the methods and Object arguments needed for the next Eval Task.
   * @throws ClassNotFoundException If the class is not found
   * @throws NoSuchMethodException If their is no such method
   * @throws IllegalAccessException If we dont have proper access
   * @throws InvocationTargetException If the method can not be instantiated
   * @throws InstantiationException If it cant be instantiated
   */
  void setUpMethArgs() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
                              InvocationTargetException, InstantiationException {

    methArray = new String[methods.size()];
    argsArray = new Object[methods.size()][0];
    StringTokenizer tok;
    //PrimitiveUtils primUtils = new PrimitiveUtils();
    for( int r = 0; r < methArray.length; r++ ) {
      // Add Method to method array
      tok = new StringTokenizer( methods.get(r));
      methArray[r] = tok.nextToken();
      int colIndex = 0;
      argsArray[r] = new Object[tok.countTokens()];
      while( tok.hasMoreTokens() ) {
        String curFull = tok.nextToken();
        String parts[] = curFull.split( "&");
        String typeString = parts[0];
        String argString = parts[1];
        Class typeClass;
        // Primitive Checks
        //if( primUtils.isPrimitive( typeString ) )
        //  typeClass = primUtils.primToWrapper( typeString );
        //else

        typeClass = Class.forName( "java.lang." + typeString );

        Constructor typeConstructor = typeClass.getDeclaredConstructor( String.class );
        Object paraObject = typeConstructor.newInstance( argString );
        argsArray[r][colIndex] = paraObject;
        colIndex += 1;
      }
    }
  }

  /**
   * Instantiatese the actual datastructures and sets them for the target of the eval task
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   */
  void instantiateDataStructure() throws ClassNotFoundException, IllegalAccessException,
                                         InstantiationException, NoSuchMethodException,
                                         InvocationTargetException {

    Class structure = Class.forName( "cs671.eval." + dataStructure );
    Class keyClass  = Class.forName( "java.lang." + typeOfKey );
    Class valClass  = Class.forName( "java.lang." + typeOfData );
    dsInstance = structure.newInstance();

    // Get String Constructors for Key Class and Value Class specified
    Constructor keyConstructor = keyClass.getDeclaredConstructor( String.class );
    Constructor valConstructor = valClass.getDeclaredConstructor( String.class );

    // Set up data structure
    Method addMethod = structure.getMethod( "add", Comparable.class, Object.class );
    addMethod.setAccessible(true);
    while( !keys.isEmpty() ) {
      Object o = addMethod.invoke( dsInstance, keyConstructor.newInstance( keys.pollFirst() ) ,
          valConstructor.newInstance( data.pollFirst() ) );
    }
  }

  /**
   * Creates a new evalTask with the current information in the lists.
   */
  void createEvalTask() {
    EvalTask newEvalTask = new EvalTask( dsInstance, methArray, argsArray );
    evalTasks.add( newEvalTask );
    cleanUp();
  }

  /**
   * Checks of the lines is empty from the read file
   * @return true if empty
   */
  boolean linesIsEmpty() {
    return lines.isEmpty();
  }
}

