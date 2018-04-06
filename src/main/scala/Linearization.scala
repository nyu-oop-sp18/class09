object Linearization extends App {
  trait T {
    def m: Unit
  }

  trait T1 extends T {
    override def m = println("T1")
  }

  trait T2 extends T {
    override def m = println("T2")
  }

  trait T3 extends T {
    override def m = println("T3")
  }
  
  object obj extends T1 with T2 with T3
  
  obj.m
}