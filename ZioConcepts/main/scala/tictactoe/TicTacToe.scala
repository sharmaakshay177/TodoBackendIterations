package tictactoe

import tictactoe.controller.ControllerLive
import tictactoe.domain.State
import tictactoe.gameLogic.GameLogicLive
import tictactoe.mode.confirm.ConfirmModeLive
import tictactoe.mode.game.GameModeLive
import tictactoe.mode.menu.MenuModeLive
import tictactoe.opponentAi.OpponentAiLive
import tictactoe.parser.confirm.ConfirmCommandParserLive
import tictactoe.parser.game.GameCommandParserLive
import tictactoe.parser.menu.MenuCommandParserLive
import tictactoe.runLoop.{RunLoop, RunLoopLive}
import tictactoe.terminal.TerminalLive
import tictactoe.view.confirm.ConfirmViewLive
import tictactoe.view.game.GameViewLive
import tictactoe.view.menu.MenuViewLive
import zio.magic._
import zio.{ExitCode, Has, URIO, ZEnv, ZIO}

object TicTacToe extends App {

  val program: URIO[Has[RunLoop], Unit] = {
    def loop(state: State): URIO[Has[RunLoop], Unit] =
      RunLoop
        .step(state)
        .flatMap(loop)
        .ignore

    loop(State.initial)
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    program
      .injectCustom(
        ControllerLive.layer,
        GameLogicLive.layer,
        ConfirmModeLive.layer,
        GameModeLive.layer,
        MenuModeLive.layer,
        OpponentAiLive.layer,
        ConfirmCommandParserLive.layer,
        GameCommandParserLive.layer,
        MenuCommandParserLive.layer,
        RunLoopLive.layer,
        TerminalLive.layer,
        ConfirmViewLive.layer,
        GameViewLive.layer,
        MenuViewLive.layer
      )
      .exitCode
}
