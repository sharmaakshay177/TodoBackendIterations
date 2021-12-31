package tictactoe.mode

import tictactoe.domain.{ConfirmCommand, ConfirmFooterMessage, State}
import tictactoe.parser.confirm.ConfirmCommandParser
import tictactoe.view.confirm.ConfirmView
import zio.{Has, UIO, URIO, URLayer, ZIO, ZLayer}

package object confirm {

  type ConfirmMode = Has[ConfirmMode.Service]

  object ConfirmMode {

    trait Service {
      def process(input: String, state: State.Confirm): UIO[State]
      def render(state: State.Confirm): UIO[String]
    }

    val live: URLayer[ConfirmCommandParser with ConfirmView, ConfirmMode] = ZLayer.fromFunction { env =>
      val confirmCommandParserService = env.get[ConfirmCommandParser.Service]
      val confirmViewService          = env.get[ConfirmView.Service]

      new Service {
        override def process(input: String, state: State.Confirm): UIO[State] =
          confirmCommandParserService
            .parse(input)
            .map {
              case ConfirmCommand.Yes => state.confirmed
              case ConfirmCommand.No  => state.declined
            }
            .orElse(ZIO.succeed(state.copy(footerMessage = ConfirmFooterMessage.InvalidCommand)))

        override def render(state: State.Confirm): UIO[String] =
          for {
            header  <- confirmViewService.header(state.action)
            content <- confirmViewService.content
            footer  <- confirmViewService.footer(state.footerMessage)
          } yield List(header, content, footer).mkString("\n\n")
      }
    }

  }

  def process(input: String, state: State.Confirm): URIO[ConfirmMode, State] =
    ZIO.accessM(_.get.process(input, state))

  def render(state: State.Confirm): URIO[ConfirmMode, String] =
    ZIO.accessM(_.get.render(state))

}
