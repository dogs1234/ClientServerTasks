package cs671.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/** Debugger Class for misc items **/

public class Debug {

  static boolean toggle = false;

  public static void on() {
    toggle = true;
  }

  public static void off() {
    toggle = false;
  }

  public static void bug( String s ) {
    if( toggle )
      System.err.println( s );
  }

  public static void fun( String s ) {
    if( toggle )
      System.err.println( " --- Function " + s + " called ---");
  }

  public static void con( String s ) {
    if( toggle )
      System.err.println( " +++ Constructor " + s + " called +++");
  }

  public static void print2dArray( int[][] arr, String name ) {
    if( !toggle )
      return;
    int rows = arr.length;
    int cols = arr[0].length;
    System.err.println( "~~~~ Debug ~ Print 2D Array ~~~~");
    System.err.println( "Name: " + name );
    System.err.println( "Rows( arr.length ): " + rows );
    System.err.println( "Cols( arr[0].length ): " + cols );
    System.err.println( "Coordinate format : [r,c] <val> ");
    for( int r = 0; r < rows; r++ ){
      for( int c = 0; c < cols; c++ ) {
        if( toggle )
          System.err.print( "[" + r + "," + c + "] " + arr[r][c] + " ");
      }
      System.err.print( "\n");
    }
  }

  public static void reflectionTester( Class a, Class b ) {
    try {
      File file = new File( "reflectionTester.txt");
      FileWriter out = new FileWriter( file );
      LinkedList<String> aStuff = new LinkedList<String>();
      LinkedList<String> bStuff = new LinkedList<String>();
      aStuff.add( "Class: " + a );
      bStuff.add( "Class: " + b );
      out.write( aStuff.pollFirst() );
      out.write( bStuff.pollFirst() );
      out.flush();


    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
