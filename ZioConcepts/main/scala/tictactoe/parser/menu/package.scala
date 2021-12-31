package tictactoe.parser

import tictactoe.domain.{AppError, MenuCommand, ParseError}
import zio.{Has, IO, ULayer, ZIO, ZLayer}

package object menu {

  type MenuCommandParser = Has[MenuCommandParser.Service]

  object MenuCommandParser{
    trait Service{
      def parse(input: String): IO[AppError, MenuCommand]
    }

    def live: ULayer[MenuCommandParser] = ZLayer.succeed {
      new Service {
        override def parse(input: String): IO[AppError, MenuCommand] ={
          input match {
            case "new game" => ZIO.succeed(MenuCommand.NewGame)
            case "resume"   => ZIO.succeed(MenuCommand.Resume)
            case "quit"     => ZIO.succeed(MenuCommand.Quit)
            case _          => ZIO.fail(ParseError)
          }
        }
      }
    }

  } // object closing

  // accessor methods here
  def parse(input: String): ZIO[MenuCommandParser, AppError, MenuCommand] =
    ZIO.accessM(_.get.parse(input))
}
