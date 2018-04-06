object DependencyInjection extends App {
  
  trait BeverageProduct {
    this: CondimentProvider =>
      val basePrice: Double
      val baseIngredients: List[String]

      /* other methods related to products */
  }

  trait EspressoProduct extends BeverageProduct {
    this: CondimentProvider =>
      override val basePrice = 3.00
      override val baseIngredients = List("espresso")
  }
  
  trait Condiment {
    def price: Double = 0.0

    def ingredients: List[String] = Nil
  }
  
  trait Sweetener extends Condiment {
    override def ingredients = "sugar" :: super.ingredients
  }
  
  trait MilkFroth extends Condiment {
    override def ingredients = "milk" :: super.ingredients
  }
  
  trait CondimentProvider {
    this: BeverageProduct =>
      val condiments: Condiment
    
      def price: Double = basePrice + condiments.price
      
      def ingredients: String = {
        val ingr = baseIngredients ++ condiments.ingredients
        ingr reduce (_ + " and " + _)
      }
  }
  
  trait SweetFrothProvider extends CondimentProvider {
    this: BeverageProduct =>
    val condiments = new Sweetener with MilkFroth
  }
  
  object cappuccino extends EspressoProduct with SweetFrothProvider {
    override def toString = "cappuccino"
  }
  
  println(s"A cappuccino consists of ${cappuccino.ingredients}, and costs $$${cappuccino.price}")
}
