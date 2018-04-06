trait A extends AnyRef {
  override def toString: String = "A"
}

trait B {
  override def toString: String = super.toString + "B"
}

trait C extends A with B

trait T1 {
  val x: A

  override def toString: String = x.toString
}

trait T2 {
  val x: B
}

class D extends T1 with T2 {
  val x = new A with B {}
}

object Traits extends App {
  println(new D)
}