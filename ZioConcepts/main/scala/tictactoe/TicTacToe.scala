package tictactoe

import tictactoe.controller.Controller
import tictactoe.domain.{ AppError, State }
import tictactoe.gameLogic.GameLogic
import tictactoe.mode.confirm.ConfirmMode
import tictactoe.mode.game.GameMode
import tictactoe.mode.menu.MenuMode
import tictactoe.opponentAi.OpponentAi
import tictactoe.parser.confirm.ConfirmCommandParser
import tictactoe.parser.game.GameCommandParser
import tictactoe.parser.menu.MenuCommandParser
import tictactoe.runLoop.RunLoop
import tictactoe.terminal.Terminal
import tictactoe.view.confirm.ConfirmView
import tictactoe.view.game.GameView
import tictactoe.view.menu.MenuView
import zio.magic._
import zio.random.Random
import zio.console.Console
import zio.{ ExitCode, Has, ULayer, URIO, ZEnv, ZIO }
import zio.Runtime

object TicTacToeRunning {
  def run(args: Array[String]): Unit =
    Runtime.default.unsafeRun(TicTacToe.run)
}

object TicTacToe {

  def run: URIO[zio.ZEnv, ExitCode] = program.provideLayer(prepareEnvironment).exitCode

  val program: ZIO[RunLoop, AppError, Unit] = {
    def loop(state: State): ZIO[RunLoop, AppError, Unit] =
      RunLoop.step(state).flatMap(loop).ignore
    loop(State.initial)
  }

  private val prepareEnvironment: ULayer[RunLoop] = {
    val opponentAiNoDeps: ULayer[OpponentAi]                                                 = Random.live >>> OpponentAi.live
    val confirmModeDeps: ULayer[ConfirmCommandParser with ConfirmView]                       =
      ConfirmCommandParser.live ++ ConfirmView.live
    val menuModeDeps: ULayer[MenuCommandParser with MenuView]                                =
      MenuCommandParser.live ++ MenuView.live
    val gameModeDeps: ULayer[GameCommandParser with GameView with GameLogic with OpponentAi] =
      GameCommandParser.live ++ GameView.live ++ GameLogic.live ++ opponentAiNoDeps
    val confirmModeNoDeps: ULayer[ConfirmMode]                                               = confirmModeDeps >>> ConfirmMode.live
    val menuModeNoDeps: ULayer[MenuMode]                                                     = menuModeDeps >>> MenuMode.live
    val gameModeNoDeps: ULayer[GameMode]                                                     = gameModeDeps >>> GameMode.live
    val controllerDeps: ULayer[ConfirmMode with GameMode with MenuMode]                      =
      confirmModeNoDeps ++ gameModeNoDeps ++ menuModeNoDeps
    val controllerNoDeps: ULayer[Controller]                                                 = controllerDeps >>> Controller.live
    val terminalNoDeps: ULayer[Terminal]                                                     = Console.live >>> Terminal.live
    val runLoopNoDeps                                                                        = (controllerNoDeps ++ terminalNoDeps) >>> RunLoop.live
    runLoopNoDeps
  }
}
