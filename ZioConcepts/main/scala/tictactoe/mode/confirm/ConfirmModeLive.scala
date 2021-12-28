package tictactoe.mode.confirm
import tictactoe.domain.{ConfirmCommand, ConfirmFooterMessage, State}
import tictactoe.parser.confirm.ConfirmCommandParser
import tictactoe.view.confirm.ConfirmView
import zio.{Function2ToLayerSyntax, Has, UIO, URLayer, ZIO}

final case class ConfirmModeLive(confirmCommandParser: ConfirmCommandParser, confirmView: ConfirmView)
  extends ConfirmMode {
  def process(input: String, state: State.Confirm): UIO[State] =
    confirmCommandParser
      .parse(input)
      .map {
        case ConfirmCommand.Yes => state.confirmed
        case ConfirmCommand.No  => state.declined
      }
      .orElse(ZIO.succeed(state.copy(footerMessage = ConfirmFooterMessage.InvalidCommand)))
  def render(state: State.Confirm): UIO[String] =
    for {
      header  <- confirmView.header(state.action)
      content <- confirmView.content
      footer  <- confirmView.footer(state.footerMessage)
    } yield List(header, content, footer).mkString("\n\n")
}
object ConfirmModeLive {
  val layer: URLayer[Has[ConfirmCommandParser] with Has[ConfirmView], Has[ConfirmMode]] =
    (ConfirmModeLive(_, _)).toLayer
}
