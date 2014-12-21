package cs671.eval;

import java.io.Serializable;

/**
 * A container for a "task" to be passed from an EvalServer to an EvalClient. Stores the object to be used,
 * names of parameters, and arguments to pass.
 */
public class EvalTask implements Serializable {
  public Object target; // DataStructure
  public String[] methods; // Names of methods to be called
  public Object[][] args; // Arguments to be passed to methods

  /**
   * EvalTask Constructor
   * @param t target
   * @param m method
   * @param a arguments
   */
  public EvalTask( Object t, String[] m, Object[][] a ) {
    target = t;
    methods = m;
    args = a;
  }

}
