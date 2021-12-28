package tictactoe.parser.confirm

import tictactoe.domain.{AppError, ConfirmCommand, ParseError}
import zio.{Function0ToLayerSyntax, Has, IO, ULayer, ZIO}

final case class ConfirmCommandParserLive() extends ConfirmCommandParser {
  def parse(input: String): IO[AppError, ConfirmCommand] =
    input match {
      case "yes" => ZIO.succeed(ConfirmCommand.Yes)
      case "no"  => ZIO.succeed(ConfirmCommand.No)
      case _     => ZIO.fail(ParseError)
    }
}
object ConfirmCommandParserLive {
  val layer: ULayer[Has[ConfirmCommandParser]] = (ConfirmCommandParserLive.apply _).toLayer
}
