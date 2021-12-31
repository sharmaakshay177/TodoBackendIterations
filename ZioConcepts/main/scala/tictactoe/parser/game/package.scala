package tictactoe.parser

import atto.Atto._
import atto.Parser
import tictactoe.domain.Board.Field
import tictactoe.domain.{AppError, GameCommand, ParseError}
import zio.{Has, IO, ULayer, ZIO, ZLayer}

package object game {

  type GameCommandParser = Has[GameCommandParser.Service]

  object GameCommandParser{
    trait Service {
      def parse(input: String): IO[AppError, GameCommand]
    }

    val live: ULayer[GameCommandParser] = ZLayer.succeed{
      new Service {
        override def parse(input: String): IO[AppError, GameCommand] =
          ZIO.fromOption(command.parse(input).done.option).orElseFail(ParseError)

        private lazy val command: Parser[GameCommand] = choice(menu, put)
        private lazy val menu: Parser[GameCommand] =
          (string("menu") <~ endOfInput) >| GameCommand.Menu
        private lazy val put: Parser[GameCommand] =
          (int <~ endOfInput).flatMap { value =>
            Field
              .make(value)
              .fold(err[GameCommand](s"Invalid field value: $value"))(field => ok(field).map(GameCommand.Put))
          }
      }
    }
    // accessor
    def parse(input: String): ZIO[GameCommandParser, AppError, GameCommand] = {
      // has.get method is providing the module capabilities as it have a Has type alias
      ZIO.accessM(_.get.parse(input))
    }
  }
}
