# Class 9

## Scala Traits

*Traits* are Scala's attempt to find a middle ground between the
simplicity but limited expressiveness of interface inheritance and the
expressive power but semantic complexity of multiple class
inheritance.

In many ways, Scala traits can be thought of as providing the
combination of features that are provided by interfaces and abstract
classes in Java. In particular, like interfaces in Java, traits can be
mixed into other classes and traits so that the resulting types
inherit the methods defined in the trait. Moreover, a trait can
provide default implementation of its methods, which can then be
overridden by the inheriting class. For instance, here is how we
express our `Duck` example using Scala traits:

```scala
trait Duck {
  def quack: Unit
    
  def push: Unit = quack
}
  
trait Flyable {
  def fly: Unit
}
  
class Mallard extends Duck with Flyable {
  override def quack: Unit = println("Quack!")

  override def fly: Unit = println("Heading south!")
    
  override def push: Unit = { quack; fly }
}
  
class RubberDuck extends Duck {
  override def quack: Unit = println("Squeak!")
}
```

Traits also share some of the same limitations with Java interfaces. In
particular, traits do not have parameter lists and therefore have no
implicit or explicit constructors. 

However, in general, traits are much more flexible than interfaces. In
particular, traits can have instance fields just like abstract
classes. For instance, here is a variant of our `Duck` example in which
the `Duck` trait uses an instance field to provide a flexible default
implementation for the `quack` method:

```scala
trait Duck {
  val quackSound: String
    
  def quack: Unit = println(s"$quackSound!")
}
  
class Mallard extends Duck {
  val quackSound = "Quack"
}
  
class RubberDuck extends Duck {
  val quackSound = "Squeak"
}
```

We can also have an instance field `x` declared in multiple unrelated
traits and mix these traits into a single class. Similar to virtual
inheritance in C++, we will end up with only one `x` field in the
resulting class. However, the final class must initialize `x` with a
value that is a common subtype of the types of `x` in the mixed in
traits. Here is an example:

```scala
class A {
  override def toString: String = "A"
}

trait B extends A {
  override def toString: String = "B"
}

trait T1 {
  val x: A

  override def toString: String = x.toString
}

trait T2 {
  val x: B
}

class D extends T1 with T2 {
  val x = new B {}
}
```

Calling `prinln(new D)` will now print `B`. Note that the types
imposed on the shared field `x` by each trait (here `A` and `B`) do
not have to be related by subtyping as in the example above. What is
important is that the type of the final value used to initialize the
field in `D` is a subtype of all these types. This works because `val`
fields are typed covariantly and hence when we initialize the field
with a value of a subtype of the types specified in the traits `T1`
and `T2`, we are guarantee to satisfy all the assumptions on the value
imposed by `T1` and `T2`. Here is an example demonstrating this more
general case:

```scala
class A {
  override def toString: String = "A"
}

trait B {
  override def toString: String = "B"
}

trait T1 {
  val x: A

  override def toString: String = x.toString
}

trait T2 {
  val x: B
}

class D extends T1 with T2 {
  val x = new A and B
}

object Traits extends App {
  println(new D)
}
```

Note that `A` and `B` are not related by subtyping. Though, we
initialize `x` in `D` using an instance of the class that we obtain by
mixing `B` into `A`. This class is a subtype of both `A` and `B`. The
resulting code compiles and will print `B` when it is executed.

### Stackable Modifications

One of the key features that make Scala's mix-in composition with
traits superior to Java's interface inheritance and C++'s virtual
class inheritance is the ability to mix multiple traits that override
the same method.

```scala
trait T {
  def m(): Unit
}

trait T1 extends T {
  override def m(): Unit = println("T1")
}

trait T2 extends T {
  override def m(): Unit = println("T2")
}

object obj extends T1 with T2
```

The conflict between the different overridden versions of `m()` is
resolved by *linearizing* the traits in the mix-in chain. That is,
consider a general chain of mix-ins:

```scala
class C extends Base with T1 with T2 ... with TN
```

where `Base` is a class or trait and `T1`, ..., `TN` are traits.

The compiler will construct the final class `C` by starting from
`Base` and first extending it with `T1`. The methods provided by
`Base` may be overridden by new versions in `T1`. The compiler
continues in this fashion, extending the resulting class `Base with
T1` with `T2` and so forth until `TN`.

In our example above, calling `obj.m()` will thus call `T2.m` and not
`T1.m` since `T2` is the last trait to be mixed-in and therefore its
`m` methods overrides the prior versions in the chain.

What makes this form of composition interesting is that any reference
to `super` in the body of one of the mixed in traits `T` will be
resolved to the class obtained by mixing in all of `T`'s predecessors
in the chain. That is, `super` in a trait `T` is bound only at the
point when `T` is mixed in. This feature is extremely useful for
implementing *stackable modifications*.

As an example, let's consider a simple trait `Coffee` that provides
a method `price` to obtain a price quote for a coffee drink and that
overrides the `toString` method to provide a description of the coffee:

```scala
trait Coffee {
  val basePrice: Double
    
  def price: Double = basePrice
  
  override def toString: String = "coffee"
}
```

Now, suppose that we want to be able to describe coffee-based drinks
that contain milk, and charge customers for the extra ingredient. We
can simply define another trait that extends `Coffee` and adapts the
price and ingredient description appropriately.

```scala
trait Milk extends Coffee {
  override def price: Double = super.price + 0.5

  override def toString: String = super.toString + " with milk"
}
```

Note that both `price` and `toString` delegate the call to
`super` and simply add the surcharge for milk to the obtained price,
respectively add "milk" as an additional ingredient to the description
produced by `toString`.

Here is a similar trait that modifies `Coffee` to account for the
addition of sugar (which is free of charge):

```scala
trait Sugar extends Coffee {
  override def toString: String = super.toString + " with sugar"
}
```

Now consider the following code, which creates an instance of the
class obtained by mixing the traits `Sugar` and
`Milk` into `Coffee`:

```scala
val c: Coffee = new Coffee with Sugar with Milk { val basePrice = 1.0 }
  
println(s"A ${c.toString} costs $$${c.price}.")
```

This code will print

```
A coffee with sugar with milk costs $2.0.
```

In particular, the call `c.toString` will be dispatched to
`Milk.toString` because `Milk` is the last trait in the mixin
chain. `Milk.toString` will then call `Sugar.toString` because
`Milk.super` is bound to `Sugar`. The call to `Sugar.toString` will
then in turn call `Coffee.toString` because `Sugar.super` is bound to
`Coffee`. 

Note that the order of the mixin composition matters due to the way
that the composition is linearized. For example, if we swap the order
of `Sugar` and `Milk` as in

```scala
val c: Coffee = new Coffee with Milk with Sugar { val basePrice = 1.0 }
  
println(s"A ${c} costs $$${c.price}.")
```

we get

```
A Coffee with milk with sugar costs $2.0.
```

Such stackable modifications are quite useful if you have many
different small adaptations of the default behavior of a class that
you want to combine in different ways in different contexts.

### Implementing Scala Traits in Java

We might ask ourselves how we could go about simulating the behavior
of traits in Java where we only have interfaces at our disposal, with
all its limitations. Since Scala compiles to Java bytecode, which also
provides only classes and interfaces, we can simply disassemble the
bytecode generated for our previous examples and see how the compiler
does it.

To see how this works, we will use a slightly simplified example:

```scala
trait Coffee {
  val basePrice: Double
  
  def price: Double = basePrice
}

trait Milk extends Coffee {
  override def price = super.price + 0.5
}

trait Sugar extends Coffee {
  override def price = super.price + 0.2
}

class CoffeeWithSugarMilk extends Coffee with Sugar with Milk { 
  val basePrice = 1.0 
}
```

The trait `Coffee` is compiled to the following interface:

```java
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
```

First, since Java interfaces cannot have instance fields, the field
`basePrice` has been removed. Instead, the compiler has added a getter
method, also called `basePrice`, that can be used to access the
field. Every read access to `basePrice` in the original code of the
trait has been replaced by calls to the getter method.

Second, the compiler has added a static method `price$` that contains
the implementation of the original `price` method in the trait
`Coffee`. Note that `price$` takes a `Coffee` as argument, which
stands for the implicit `this` receiver of the original `price`
method. The method `price$` will be used to implement calls to
`super.price` in the cases where `super` is bound to `Coffee` in a
mixin chain.

Finally, we have the actual instance method `price` as in the original
trait, which simply delegates to `price$`.

The translation of the traits `Milk` and `Sugar` look quite
similar, so we only show the translated code for `Sugar` here:

```java
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
```

We obtain an interface `Sugar` that extends the interface `Coffee`. As
in the translation of `Coffee`, we obtain a static method `price$` for
the implementation of `price` provided by the trait `Sugar`. The call
`super.price` in the body of the original `price` has been translated
to a call to the auxiliary method `Sugar$$super$price` on the explicit
receiver `s`. The method `Sugar$$super$price` is a placeholder for the
actual `super.price` that is determined when `Sugar` is mixed into
another class. The interface simply declares this method as an
abstract instance member.

Now let's consider the following class that mixes `Sugar` and `Milk`
into `Coffee`:

```scala
class CoffeeWithSugarMilk extends Coffee with Sugar with Milk { 
  val basePrice = 1.0 
}
```

The Java translation of this class will look as follows:

```java
public static class CoffeeWithSugarMilk implements Sugar, Milk {
  // val basePrice = 1.0
  final double basePrice = 1.0;

  // implementation of getter method for val basePrice
  public double basePrice() {
    return basePrice;
  }
    
  // Milk's super.price is resolved to Sugar.price$
  public double Milk$$super$price() {
    return Sugar.price$(this);
  }
    
  // Sugar's super.price is resolved to Coffee.price$
  public double Sugar$$super$price() {
    return Coffee.price$(this);
  }

  // Milk is last trait that is mixed in, so inherit its implementation of price.
  public double price() {
    return Milk.price$(this);
  }
}
```

First, translated class now declares the instance field `basePrice` that it
inherited from the trait `Coffee` and uses this field to implement the
corresponding getter method. Since `Sugar` is mixed into `Coffee`, the
call to `super.price` in `Sugar.price` should be resolved to
`Coffee.price`. Thus, the class implements `Sugar$$super$price` by
delegating to `Coffee.price$`. Similarly, `Milk$$super$price` resolves
to `Sugar.price$`.

We can now create an instance of the resulting class and call its
`price` method:

```java
CoffeeWithSugarMilk c = new CoffeeWithSugarMilk();
c.price() // returns 1.7
```

The observed behavior is consistent with the expected behavior of the
stacked traits.

### Self-Types and Dependency Injection

Large software systems are often organized into interdependent
components that provide different services and functionality. It
desirable to design the overall system in such a way that components
can be reused in other contexts or replaced by alternative
implementations without much effort. This is particular helpful for
testing an individual component when other components it depends on
are also still under development.

A common solution to this problem in OOP is a design pattern referred
to as
[dependency injection](https://en.wikipedia.org/wiki/Dependency_injection). In
the OOP setting, software components correspond to classes. The
instance `b` of a component `B` on which another component `A` depends
is injected into the instance `a` of `A` by passing a reference to `b`
when `a` is created. This can be implemented via auxiliary constructor
arguments in `A`, or dedicated setter methods.

Many languages do not provide inbuilt mechanisms for expressing
dependencies and implementing the *wiring* between components
succinctly. Therefore, one often resorts to using specialized
frameworks such as [Spring](https://spring.io/) for Java that reduce
some of the boilerplate code that is needed to implement this design pattern.

Scala provides a language feature referred to as *self-types* that
together with mixin composition of traits can be used for a
light-weight implementation of dependency injection.

A self type of a class or trait `C` expresses that the instances of
`C` depend on another class `D`. Self-types can be expressed using the
following syntax:

```scala
trait C {
  self: D => 
  // implementation of C
  ...
}
```

Note that the scope of the identifier `self` is the body of trait `C`
and is always an alias for `this`. You may use any other valid Scala
identifier instead of `self` in the self-type declaration, including
`this` itself.

The effect of the self-type annotation is that we can now access the
members of class `D` within the body of `C` as if they were members of
`C`.

The compiler will statically enforce that before an instance of some
class that extends `C` can be created, we have to mixin `D` into that
class (or some other subtype of `D`) to satisfy `C`'s dependency:

```scala
val c = new C with D { ... }
```

Note that the above self-type for `C` is different from a subtyping
constraint expressed using `extends`:

```scala
trait C extends D {
  ...
}
```

In particular, we can use self-types to express mutual dependencies
between the two classes `C` and `D`:

```scala
trait D {
  self: C => 
  // implementation of D
  ...
}
```
Situations where such mutually dependent components arise are not
uncommon
(the
[original paper](http://lampwww.epfl.ch/~odersky/papers/ScalableComponent.pdf) that
introduced self-types provides an example of such a situation that is
motivated by the implementation of the Scala compiler itself).

Also, self-types do not impose a specific order in which `C` and `D`
are mixed together:

```scala
val c = new D with C { ... }
```

Let us look at a more concrete example of how this all works. Let's
start from our stackable modifications example in the previous
section. As part of this example, we declared the following trait

```scala
trait Sugar extends Coffee {
  override def toString: String = super.toString + " with sugar"
}
```

This is actually not a great design. Remember that class
inheritance should be used for expressing an "is a" relationship
between the subclass and its superclass. This is clearly not the case
here: a condiment such as `Sugar` is certainly not a beverage like
`Coffee`.

Let's improve our design by creating two separate components for
expressing the relationship between beverages and condiments: a
`BeverageProduct` that abstracts from beverage products such as
`Coffee` and a `CondimentProvider` that serves as a container for the
specific condiments "decorating" a particular
beverage. `BeverageProduct` captures basic properties of the beverage,
such as its base price, its base ingredients and so
forth. `CondimentProvider` keeps track of the surcharges associated
with the added condiments and the additional ingredients contained in
them. It also provides functionality for computing the final price and
ingredient list of a beverage with condiments.

We use self-types to separate the two components into their own
traits. Here is how `BeverageProduct` looks like:


```scala
trait BeverageProduct {
  this: CondimentProvider =>

  val basePrice: Double
  val baseIngredients: List[String]

  /* methods related to products */
  ...
}
```

And here is a concrete implementation of a `BeverageProduct`:

```scala
trait EspressoProduct extends BeverageProduct {
  this: CondimentProvider =>

  override val basePrice = 3.00
  override val baseIngredients = List("espresso")
}
```

Next, let's define a trait for condiments:

```scala
trait Condiment {
  def price: Double = 0.0

  def ingredients: List[String] = Nil
}
```

Note that `Condiment` is no longer a subtype of `BeverageProduct` like
e.g. `Sugar` was a subtype of `Coffee` in our previous design.

As in our earlier example, we can still implement specific condiments
as stackable components:

```scala
trait Sweetener extends Condiment {
  override def ingredients = "sugar" :: super.ingredients
}
  
trait MilkFroth extends Condiment {
  override def ingredients = "milk" :: super.ingredients
}
```

Finally, here is our implementation of `CondimentProvider`:

```scala
trait CondimentProvider {
  this: BeverageProduct =>
  
  val condiments: Condiment
    
  def price: Double = basePrice + condiments.price
      
  def ingredients: String = {
    val ingr = baseIngredients ++ condiments.ingredients
    ingr reduce (_ + " and " + _)
  }
}
```

Note how the implementation of `price` uses `basePrice` which is
provided by through the self-type `BeverageProduct` to calculate the
total price of the beverage.

Here is a concrete subclass of `CondimentProvider` that provides
`MilkFroth` and `Sweetener`:

```scala
trait SweetFrothProvider extends CondimentProvider {
  this: BeverageProduct =>
  val condiments = new Sweetener with MilkFroth
}
```

Using mixin composition, we can now wire the components together to
implement specific beverage products. For instance, a cappuccino can
be obtained by mixing `EspressoProduct` with `SweetFrothProvider`:

```scala
object cappuccino extends EspressoProduct with SweetFrothProvider {
  override def toString = "cappuccino"
}
```

Executing

```scala
println(s"A cappuccino consists of ${cappuccino.ingredients}, and costs $$${cappuccino.price}")
```

would then print:

```
A cappuccino consists of espresso and milk and sugar, and costs $3.0
```

#### The Cake Pattern

The above implementation of dependency injection using self-types is a
simplified variant of the more
general
[Cake Pattern](http://jonasboner.com/real-world-scala-dependency-injection-di/).
