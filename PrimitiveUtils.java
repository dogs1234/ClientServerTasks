package cs671.eval;

import java.util.HashMap;
import java.util.Map;

class PrimitiveUtils {
  Map<Class,Class> pt; Map<Class,Class> tp;
  Map<String,Class> prims; Map<Class,Class> wraps;
  PrimitiveUtils( ) {
    pt = new HashMap<Class,Class>();
    tp = new HashMap<Class,Class>();
    prims = new HashMap<String,Class>();
    wraps = new HashMap<Class,Class>();
    setUpMaps();
  }

  boolean isPrimitive( String s ) {
    return prims.containsKey( s );
  }

  Class primToWrapper( String s ) {
    return ( prims.get( s ) );
  }

  Class primToType( Class c ) {
    return pt.get( c ).getClass();
  }

  Class typeToPrim( Class c ) {
    return tp.get( c ).getClass();
  }

  private void setUpMaps() {
    pt.put(int.class,       Integer.TYPE);
    pt.put(long.class,      Long.TYPE);
    pt.put(double.class,    Double.TYPE);
    pt.put(float.class,     Float.TYPE);
    pt.put(boolean.class,   Boolean.TYPE);
    pt.put(char.class,      Character.TYPE);
    pt.put(byte.class,      Byte.TYPE);
    pt.put(void.class,      Void.TYPE);
    pt.put(short.class,     Short.TYPE);
    tp.put( Integer.TYPE,   int.class);
    tp.put( Long.TYPE,      long.class );
    tp.put( Double.TYPE,    double.class );
    tp.put( Float.TYPE,     float.class );
    tp.put( Boolean.TYPE,   boolean.class );
    tp.put( Character.TYPE, char.class );
    tp.put( Byte.TYPE,      byte.class );
    tp.put( Void.TYPE,      void.class );
    tp.put( Short.TYPE,     short.class );
    prims.put("boolean",    Boolean.class);
    prims.put("char",       Character.class);
    prims.put("byte",       Byte.class);
    prims.put("void",       Void.class);
    prims.put("short",      Short.class);
    prims.put("int",        Integer.class);
    prims.put("long",       Long.class);
    prims.put("float",      Float.class);
    prims.put("double",     Double.class);
    wraps.put( Boolean.class, boolean.class );
    wraps.put( Character.class, char.class  );
    wraps.put( Byte.class, byte.class    );
    wraps.put( Void.class, void.class     );
    wraps.put( Short.class, short.class  );
    wraps.put( Integer.class, int.class  );
    wraps.put( Long.class, long.class     );
    wraps.put( Float.class, float.class  );
    wraps.put( Double.class, double.class );
  }

  public static void main( String args[] ) {
    PrimitiveUtils utils = new PrimitiveUtils();
    if( utils.primToType( int.class ) == utils.typeToPrim( Integer.TYPE ))
      Debug.bug( "They are equal!" );
  }
}
