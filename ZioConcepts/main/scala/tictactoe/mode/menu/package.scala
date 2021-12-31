package tictactoe.mode

import tictactoe.domain.{ConfirmAction, ConfirmFooterMessage, GameFooterMessage, GameResult, MenuCommand, MenuFooterMessage, Piece, Player, State}
import tictactoe.parser.menu.MenuCommandParser
import tictactoe.view.menu.MenuView
import zio.{Has, UIO, URIO, URLayer, ZIO, ZLayer}

package object menu {

  type MenuMode = Has[MenuMode.Service]

  object MenuMode {

    trait Service {
      def process(input: String, state: State.Menu): UIO[State]
      def render(state: State.Menu): UIO[String]
    }

    val live: URLayer[MenuCommandParser with MenuView, MenuMode] = ZLayer.fromFunction{ env =>
      val menuCommandParserService = env.get[MenuCommandParser.Service]
      val menuViewService          = env.get[MenuView.Service]

      new Service {
        override def process(input: String, state: State.Menu): UIO[State] =
          menuCommandParserService
            .parse(input)
            .map {
              case MenuCommand.NewGame =>
                val newGameState =
                  State.Game(
                    Map.empty,
                    Player.Human,
                    Player.Ai,
                    Piece.Cross,
                    GameResult.Ongoing,
                    GameFooterMessage.Empty
                  )
                state.game match {
                  case Some(_) =>
                    State.Confirm(ConfirmAction.NewGame, newGameState, state, ConfirmFooterMessage.Empty)
                  case None    => newGameState
                }
              case MenuCommand.Resume  =>
                state.game match {
                  case Some(gameState) => gameState
                  case None            => state.copy(footerMessage = MenuFooterMessage.InvalidCommand)
                }
              case MenuCommand.Quit    =>
                state.game match {
                  case Some(_) =>
                    State.Confirm(ConfirmAction.Quit, State.Shutdown, state, ConfirmFooterMessage.Empty)
                  case None    => State.Shutdown
                }
            }
            .orElse(UIO.succeed(state.copy(footerMessage = MenuFooterMessage.InvalidCommand)))

        override def render(state: State.Menu): UIO[String] =
          for {
            header  <- menuViewService.header
            content <- menuViewService.content(state.game.nonEmpty)
            footer  <- menuViewService.footer(state.footerMessage)
          } yield List(header, content, footer).mkString("\n\n")
      }

    }

  }

  def process(input: String, state: State.Menu): URIO[MenuMode, State] =
    ZIO.accessM(_.get.process(input, state))

  def render(state: State.Menu): URIO[MenuMode, String] = ZIO.accessM(_.get.render(state))

}
