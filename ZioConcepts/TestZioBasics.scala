package ZioConcepts

import zio.{Task, ZIO}

import scala.io.StdIn

// for only testing the zio concepts
//https://www.baeldung.com/scala/zio-intro
object TestZioBasics extends App {

  // this should be evaluated immediately as variable assignment is encountered
  val goShopping: Unit = println("Going to The grocery store -- from variable")
  // this should be evaluated when called for
  val goShoppingZio =
    ZIO.attempt(println("Going to The grocery store -- from zio effect"))

  /** Referential transparency
    * an expression such as 2+2 is referentially transparent if we cna always replace
    * the computation with its result in any program while still preserving its
    * runtime behaviour
    *
    * Pure Expressions - Expressions without side-effects
    * Pure Functions - function whose body are pure expressions
    */

  // Example -1
  val readLine = ZIO.attempt(StdIn.readLine())
  def printLine(line: String) = ZIO.attempt(println(line))

  val echo =
    for {
      line <- readLine
      _ <- printLine(line)
    } yield ()

  // async example
  def getUserByIdAsync(id: Int)(cb: Option[String] => Unit): Unit = ???
  // one way
  getUserByIdAsync(0) {
    case Some(value) => println(value)
    case None        => println("User not found")
  }
  // using zio
  def getUserById(id: Int): ZIO[Any, None.type, String] =
    ZIO.async { callback =>
      getUserByIdAsync(id) {
        case Some(value) => callback(ZIO.succeed(value))
        case None        => callback(ZIO.fail(None))
      }
    }
}

object Exercises {
  import scala.io.Source
  import java.io.{PrintWriter, File}
  //1. Implement a ZIO version of the function readFile by using the ZIO.effect
  // constructor.
  // previous implementation
  def readFile(file: String): String = {
    val source = scala.io.Source.fromFile(file)
    try source.getLines().mkString finally source.close()
  }
  // Zio implementation
  // ZIO[Any, Throwable, A]
  // Succeed with an `A`, may fail with `Throwable`, no requirements.
  // ZIO[Any, Throwable, String]
  def readFileZio(file: String): Task[String] ={
    ZIO.attempt{
      val source = Source.fromFile(file)
      val string = source.getLines().mkString
      source.close()
      string
    }
  }

  //2. Implement a ZIO version of the function readFile by using the ZIO.effect
  // constructor.
  def writeFile(file: String, text: String): Unit = {
    import java.io._
    val pw = new PrintWriter(new File(file))
    try pw.write(text) finally pw.close()
  }

  // ZIO[Any, Throwable, Unit]
  def writeFileZio(file: String, text: String): Task[Unit] = {
    ZIO.attempt {
      val pw = new PrintWriter(new File(file))
      pw.write(text)
      pw.close()
    }
  }

  //3. Using the flatMap method of ZIO effects, together with the readFileZio
  // and writeFileZio functions that you wrote, implement a ZIO version of
  // the function copyFile.
  def copyFile(source: String, dest: String): Unit = {
    val contents = readFile(source)
    writeFile(dest, contents)
  }

  def copyFileZio(source: String, dest: String): Task[Unit] = {
    readFileZio(source).flatMap(line => writeFileZio(dest, line))
  }

  //4. Rewrite the following ZIO code that uses flatMap into a for comprehension.
  def rewrite: Task[Unit] = {
    def printLine(line: String): Task[Unit] = ZIO.attempt(println(line))
    val readLine = ZIO.attempt(scala.io.StdIn.readLine())

    for {
      _ <- printLine("What is your Name?")
      name <- readLine
      _ <- printLine(s"Hello $name!")
    } yield ()
  }

  //5. Rewrite the following ZIO code that uses flatMap into a for comprehension.
  def rewrite2: Task[Unit] = {
    val random = ZIO.attempt(scala.util.Random.nextInt(3) + 1)
    def printLine(line: String): Task[Unit] = ZIO.effect(println(line))
    val readLine = ZIO.attempt(scala.io.StdIn.readLine())

    for {
      rand <- random
      _ <- printLine("Guess a number from 1 to 3:")
      _ <- readLine.map{in =>
        if (rand.toString == in) printLine("You guessed right!")
        else printLine(s"You guessed wrong, the number was $rand")
      }
    } yield()
  }

  //6. Implement the zipWith function in terms of the toy model of a ZIO
  // effect. The function should return an effect that sequentially composes
  // the specified effects, merging their results with the specified user-defined
  // function.
  final case class ZIO[-R, +E, +A](run: R => Either[E, A])

  def zipWith[R, E, A, B, C](self: ZIO[R, E, A],
                             that: ZIO[R, E, B])(f: (A, B) => C): ZIO[R, E, C] = {
    val r1 = self.run
    val r2 = that.run
    ZIO[R, E, f(r1, r2)]
  }

  //7. Implement the collectAll function in terms of the toy model of a ZIO
  // effect. The function should return an effect that sequentially collects the
  // results of the specified collection of effects.
  def collectAll[R, E, A]( in: Iterable[ZIO[R, E, A]] ): ZIO[R, E, List[A]] = {
    val res = in.map(some => some.run).map(someA => List(someA)).flatten
    ZIO[R, E, res]
  }

}
