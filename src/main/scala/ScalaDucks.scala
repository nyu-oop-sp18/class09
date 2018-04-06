class ScalaDucks {
  trait Duck {
    val quackSound: String
    
    def quack: Unit = println(s"$quackSound!")
    
    def push: Unit = quack
  }
  
  trait Flyable {
    def fly: Unit
  }
  
  class Mallard extends Duck with Flyable {
    val quackSound = "Quack"
    
    override def fly: Unit = println("Heading south!")
    
    override def push: Unit = { quack; fly }
  }
  
  class RubberDuck extends Duck {
    val quackSound = "Squeak"
  }
}
