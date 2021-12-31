package tictactoe

import zio.console.Console
import zio.{Has, IO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.IOException

package object terminal {

  type Terminal = Has[Terminal.Service]

  object Terminal {
    // service definition
    trait Service {
      val getUserInput: Task[String]
      def display(frame: String): Task[Unit]
    }
    val ansiClearScreen = "\u001b[H\u001b[2J"

    val live: URLayer[Console, Terminal] = ZLayer.fromService { consoleService =>
      new Service {
        override val getUserInput: Task[String]         = consoleService.getStrLn.orDie
        override def display(frame: String): Task[Unit] =
          for {
            _ <- consoleService.putStr(ansiClearScreen)
            _ <- consoleService.putStrLn(frame)
          } yield ()
      }
    }

    // accessors
    val getUserInput: ZIO[Terminal, Throwable, String]         = ZIO.accessM(_.get.getUserInput)
    def display(frame: String): ZIO[Terminal, Throwable, Unit] = ZIO.accessM(_.get.display(frame))
  }
}
