package tictactoe

import tictactoe.controller.Controller
import tictactoe.domain.{AppError, State}
import tictactoe.terminal.Terminal
import zio.{Has, IO, UIO, URLayer, ZIO, ZLayer}

package object runLoop {

  type RunLoop = Has[RunLoop.Service]

  object RunLoop {

    trait Service {
      def step(state: State): IO[AppError, State]
    }

    val live: URLayer[Controller with Terminal, RunLoop] = ZLayer.fromFunction { env =>
      val controllerService = env.get[Controller.Service]
      val terminalService   = env.get[Terminal.Service]

      new Service {
        override def step(state: State): IO[AppError, State] =
          for {
            _         <- controllerService.render(state).flatMap(terminalService.display)
            input     <- if (state == State.Shutdown) ZIO.succeed("") else terminalService.getUserInput
            nextState <- controllerService.process(input, state)
          } yield nextState
      }
    }

  }

  def step(state: State): ZIO[RunLoop, AppError, State] =
    ZIO.accessM(_.get.step(state))
}
