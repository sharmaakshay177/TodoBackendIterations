package tictactoe.runLoop

import tictactoe.controller.Controller
import tictactoe.domain.{AppError, State}
import tictactoe.terminal.Terminal
import zio._

final case class RunLoopLive(controller: Controller, terminal: Terminal) extends RunLoop {
  override def step(state: State): IO[AppError, State] =
    for {
      _         <- controller.render(state).flatMap(terminal.display)
      input     <- if (state == State.Shutdown) UIO.succeed("") else terminal.getUserInput
      nextState <- controller.process(input, state)
    } yield nextState
}
object RunLoopLive {
  val layer: URLayer[Has[Controller] with Has[Terminal], Has[RunLoop]] = (RunLoopLive(_, _)).toLayer
}
