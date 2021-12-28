package tictactoe.controller

import tictactoe.domain.{AppError, IllegalStateError, State}
import tictactoe.mode.confirm.ConfirmMode
import zio.{Function3ToLayerSyntax, Has, IO, UIO, URLayer, ZIO}
import tictactoe.mode.game.GameMode
import tictactoe.mode.menu.MenuMode

final case class ControllerLive(confirmMode: ConfirmMode,
                                gameMode: GameMode,
                                menuMode: MenuMode) extends Controller {
  override def process(input: String, state: State): IO[AppError, State] =
    state match {
      case s: State.Confirm => confirmMode.process(input, s)
      case s: State.Menu => menuMode.process(input, s)
      case s: State.Game => gameMode.process(input, s)
      case State.Shutdown => ZIO.fail(IllegalStateError)
    }

  def render(state: State): UIO[String] =
    state match {
      case s: State.Confirm => confirmMode.render(s)
      case s: State.Game => gameMode.render(s)
      case s: State.Menu => menuMode.render(s)
      case State.Shutdown => UIO.succeed("Shutting Down...")
    }
}

object ControllerLive {
  val layer: URLayer[Has[ConfirmMode] with Has[GameMode] with Has[MenuMode], Has[Controller]] =
    (ControllerLive(_, _, _)).toLayer
}
