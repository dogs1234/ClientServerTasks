package cs671.eval;

import org.omg.CORBA.OBJECT_NOT_EXIST;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public final class DNF {


  private static File flowFile = new File( "DNF-Flow-File-1.txt" );
  private static File dataFile = new File( "DNF-Data-File-1.txt" );
  private static File bothFile = new File( "DNF-Both-File-1.txt" );
  private static OutputStreamWriter dataOut = new OutputStreamWriter( System.err );
  private static OutputStreamWriter flowOut = new OutputStreamWriter( System.err );
  private static boolean dataOutOn = false;
  private static boolean flowOutOn = false;
  private static int f = 0;
  private static int d = 0;
  private static String indent = "\t";
  private static int flowLevel;
  private static int teeter = 0;

  private static int methodsStarted = 0;
  private static int methodsClosed = 0;
  static StringBuffer sbFlow = new StringBuffer();
  static StringBuffer sbData = new StringBuffer();
  private static LinkedList<String> slab1 = new LinkedList<String>();

  public static synchronized void METHOD_START( String name, Class caller, Object ... par ) {
    if( flowOutOn )
      try {
        sbFlow.append( indent + "    =.=.=.=.=.=["+f+"]=[ " + caller.getSimpleName() + "." + name + " ]=.=.=.=.=.=.    \n" );
        int headerLength = sbFlow.length();
        for( Object o : par ) {
          sbFlow.append( indent + "\t\t==   "+o.getClass().getSimpleName()+" "+o+ "\n" );
        }
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++; methodsStarted++;
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public static synchronized void METHOD_END( String name, Class caller, Object ... vals ) {
    if( flowOutOn ) {
      try {
        for( Object o : vals )
          sbFlow.append( indent + "\t\t==[" + o.getClass() + " " + o + "]" );
        methodsClosed--;
        sbFlow.append( indent + "====["+methodsClosed+"]====[ENDING] "+caller.getSimpleName()+"."+name+ "=[METHOD]====\n\n" );
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static synchronized void METHOD_RET_FALSE( String name, Class caller ) {
    if( flowOutOn ) {
      try {
        methodsClosed--;
        sbFlow.append( indent + " - - METHOD IS RETURNING [FALSE] - " + "\n");
        sbFlow.append( indent + "========["+methodsClosed+"]====[FALSE] "+caller.getSimpleName()+"."+name+ " [FALSE]===[FALSE]===\n\n" );
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static synchronized void METHOD_RET_TRUE( String name, Class caller ) {
    if( flowOutOn ) {
      try {
        methodsClosed--;
        sbFlow.append( indent + "+ + + METHOD IS RETURNING + [TRUE] +" + "\n");
        sbFlow.append( indent + "========["+methodsClosed+"]====[FALSE] "+caller.getSimpleName()+"."+name+ " [FALSE]===[FALSE]===\n\n" );
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  /*
  public static synchronized void BASIC_METHOD_START( String name, Class caller, Object ... par ) {
    if( flowOutOn )
      try {
        sbFlow.append( indent + "=.=.=.=.=.=.=.=["+f+"]=[ " + caller.getSimpleName() + "." + name + " ]=.=.=.=.=.=.=.=}\n" );
        int headerLength = sbFlow.length();
        for( Object o : par ) {
          sbFlow.append( indent + "=="+o.getClass().getSimpleName()+" "+o+ "\n" );
        }
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++; methodsStarted++;
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public static synchronized void FUNCTION_END( String name, Class caller, Object ... vals ) {
    if( flowOutOn )
      try {
        for( Object o : vals )
          sbFlow.append( indent + "==[" + o.getClass() + " " + o + "]" );
        sbFlow.append( indent + "=============[ENDING] "+caller.getSimpleName()+"."+name+ " [METHOD]=============\n" );
        flowOut.write( sbFlow.toString() );
        flowOut.flush();
        f++; methodsClosed--;
      } catch (IOException e) {
        e.printStackTrace();
      }
  }
  */
  public static synchronized void METHOD_LEVEL_UP() {
      indent += "\t\t\t";
  }

  public static synchronized void METHOD_LEVEL_DOWN() {
    if( indent.length() > 3 )
      indent.substring( 0, indent.length() - 3 );
  }

  public static synchronized void DATA( String s ) {
    if( dataOutOn )
      try {
        if( s.equals( "" )) {
          dataOut.write( "\n");
        } else {
          dataOut.write( indent + "{ " + s + " }\n" );
          dataOut.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public static synchronized void DATA_COMPARE_PRINT ( Object[] arr1, String[] msgs, Object[] arr2 ) {
    if( !dataOutOn ){ System.err.println( "Data output is not on. "); return; }
    if( ( arr1.length - arr2.length - msgs.length ) != ( 0 - arr1.length ) )
      throw new IllegalArgumentException( "Arrays not the same length.");

    int longestLine = 0;
    for( int i = 0; i < arr1.length; i++ ) {
      slab1.add( " [ " + arr1[i] + " ] " + msgs[i] + " [ " + arr2[i] + " ] "  );
      longestLine = Math.max( slab1.get(i).length(), longestLine );
    }

    for( String s : slab1 ) {
      try {
        dataOut.write( indent + slab1.pollFirst() );
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static synchronized void DATA_SORTED_COMPARE_PRINT ( Object[] arr1, String[] msgs, Object[] arr2 ) {
    if( !dataOutOn ){ System.err.println( "Data output is not on. "); return; }
    if( ( arr1.length - arr2.length - msgs.length ) != ( 0 - arr1.length ) )
      throw new IllegalArgumentException( "Arrays not the same length.");

    int longestLine = 0;
    for( int i = 0; i < arr1.length; i++ ) {
      slab1.add( " [ " + arr1[i] + " ] " + msgs[i] + " [ " + arr2[i] + " ] "  );
      longestLine = Math.max( slab1.get(i).length(), longestLine );
    }

    Collections.sort( slab1 );

    for( String s : slab1 ) {
      try {
        dataOut.write( indent + slab1.pollFirst() );
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void BLEND_OUT( ) {
    try {
      dataOut = new FileWriter( bothFile );
      flowOut = new FileWriter( bothFile );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void DEFAULT_OUTPUT() {
    dataOut = new OutputStreamWriter( System.err);
    flowOut = new OutputStreamWriter( System.err);
  }
  public static void SET_DATA_FILE( String fileName ) {
    try {
      dataOut = new FileWriter( fileName, false );
    } catch ( IOException e ) { e.printStackTrace(); }
  }

  public static void CLEAR_DATA_FILE( ) {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(dataFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    writer.print("");
    writer.close();
  }

  public static void SET_FLOW_FILE( String fileName ) {
    try {
      flowOut = new FileWriter( fileName, false );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Toggles for on and off output.
   * @param b
   */
  public static void setFlow     ( boolean b )  {   flowOutOn = b;  }
  public static void forceFlow   ( boolean b )  { flowOutOn = true; }
  public static void setData     ( boolean b )  {   dataOutOn = b;  }
  public static void forceData (){ dataOutOn = true; }
  public static void FORCE_ALL  (){ dataOutOn = true; flowOutOn = true;   }
  public static void forceNone (){ dataOutOn = false; flowOutOn = false; }



  public static void main( String args[] ) {
    DNF.FORCE_ALL();
    DNF.SET_FLOW_FILE("DNF-Flow-File-1.txt");
    DNF.SET_DATA_FILE("DNF-Data-File-1.txt");
    DNF.METHOD_START("main", DNF.class, args);
    int x = 1999; String k = "z0mi3ie"; boolean t = true; EvalTask et = new EvalTask( null, null, null );
    DNF.METHOD_END( "main", DNF.class, x, k, t, et );
    DNF.METHOD_LEVEL_UP();
    DNF.METHOD_START("main", DNF.class, args);
    DNF.DATA( "Testing this data thingy awww yeah.");
    DNF.METHOD_START("main", DNF.class, args);
  }
}
