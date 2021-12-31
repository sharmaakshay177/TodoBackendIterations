package tictactoe.parser

import tictactoe.domain.{AppError, ConfirmCommand, ParseError}
import zio.{Has, IO, ULayer, ZIO, ZLayer}

package object confirm {

  type ConfirmCommandParser = Has[ConfirmCommandParser.Service]

  object ConfirmCommandParser {

    trait Service {
      def parse(input: String): IO[AppError, ConfirmCommand]
    }

    def live: ULayer[ConfirmCommandParser] = ZLayer.succeed {
      new Service {
        override def parse(input: String): IO[AppError, ConfirmCommand] = {
          input match {
            case "yes" => ZIO.succeed(ConfirmCommand.Yes)
            case "no" => ZIO.succeed(ConfirmCommand.No)
            case _ => ZIO.fail(ParseError)
          }
        }
      }
    }
  } // object closing

  // accessors
  def parse(input: String): ZIO[ConfirmCommandParser, AppError, ConfirmCommand] =
    ZIO.accessM(_.get.parse(input))
}
