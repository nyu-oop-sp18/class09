public class TraitsInJava {
  
  public interface Coffee {
    // getter for val basePrice
    double basePrice();

    // implementation of Coffee.price to be used for calls to super.price in mixin
    static double price$(Coffee c) {
      return c.basePrice();
    }
    
    // default implementation of method price for Coffee
    default double price() {
      return price$(this);
    }
  }
  
  public interface Sugar extends Coffee {
    // Sugar's super.price to be determined during mixin
    double Sugar$$super$price();

    // implementation of Sugar.price to be used for calls to super.price in mixin
    static double price$(Sugar s) {
      return s.Sugar$$super$price() + 0.5; 
    }

    // default implementation of method price for Sweetened
    default double price() {
      return price$(this);
    }
  }
  
  public interface Milk extends Coffee {
    // Milk's super.price to be determined during mixin
    double Milk$$super$price();

    // implementation of Milk.price to be used for calls to super.price in mixin
    static double price$(Milk m) {
      return m.Milk$$super$price() + 0.5;
    }

    // default implementation of method price for Milky
    default double price() {
      return price$(this);
    }
  }
  
  /* Scala: 
   *  class CoffeeWithSugarMilk extends Coffee with Sugar with Milk { 
   *    val basePrice = 1.0 
   *  }
   */
  public static class CoffeeWithSugarMilk implements Sugar, Milk {
    // val basePrice = 1.0
    final double basePrice = 1.0;

    // implementation of getter method for val basePrice
    public double basePrice() {
      return basePrice;
    }
    
    // Milk's super.price is resolved to Sweetened.price
    public double Milk$$super$price() {
      return Sugar.price$(this);
    }
    
    // Sugar's super.price is resolved to Coffee.price
    public double Sugar$$super$price() {
      return Coffee.price$(this);
    }

    // Milky is last trait that is mixed in, so inherit its implementation of price.
    public double price() {
      return Milk.price$(this);
    }
    
  }
  
  public static void main(String[] args) {
    Coffee c = new CoffeeWithSugarMilk();
    
    System.out.println("A coffee with sugar and milk costs $" + c.price());
  }
}
