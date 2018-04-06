trait Coffee {
  val basePrice: Double
    
  def price: Double = basePrice
  
  override def toString: String = "coffee"
}

trait Sugar extends Coffee {
  override def toString: String = super.toString + " with sugar"
}

trait Milk extends Coffee {
  override def price: Double = super.price + 0.5

  override def toString: String = super.toString + " with milk"
}


object StackableMods extends App {
  val c = new Coffee with Milk with Sugar { val basePrice = 2.0 }
  
  println(s"A ${c} costs $$${c.price}.")
}
