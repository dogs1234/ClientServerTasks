package cs671.eval;

public class AssignableTest {
  public static void main(String[] args) {
    String format = "%s is assignable from %s: %s\n";
    System.out.printf(
        format,
        "SuperClass",
        "SubClass",
        SuperClass.class.isAssignableFrom(SubClass.class)
    );

    System.out.printf(
        format,
        "SubClass",
        "SuperClass",
        SubClass.class.isAssignableFrom(SuperClass.class)
    );

    System.out.printf(
        format,
        "int",
        "Integer",
        int.class.isAssignableFrom(Integer.class)
    );

    System.out.printf(
        format,
        "Integer",
        "int",
        Integer.class.isAssignableFrom(int.class)
    );
  }

  class SuperClass {
  }

  class SubClass extends SuperClass {
  }
}
