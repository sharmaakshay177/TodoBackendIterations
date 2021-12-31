package tictactoe

import tictactoe.domain.Board.Field
import tictactoe.domain.{AppError, FullBoardError, Piece}
import zio.random.Random
import zio.{Has, IO, URLayer, ZIO, ZLayer}

package object opponentAi {

  type OpponentAi = Has[OpponentAi.Service]

  object OpponentAi {

    trait Service {
      def randomMove(board: Map[Field, Piece]): IO[AppError, Field]
    }

    val live: URLayer[Random, OpponentAi] = ZLayer.fromService {
      randomService =>
      new Service {
        override def randomMove(board: Map[Field, Piece]): IO[AppError, Field] = {
          val unoccupied = (Field.all.toSet -- board.keySet).toList.sortBy(_.value)
          unoccupied.size match {
            case 0 => IO.fail(FullBoardError)
            case n => randomService.nextIntBounded(n).map(unoccupied(_))
          }
        }
      }
    }
  }

  def randomMove(board: Map[Field, Piece]): ZIO[OpponentAi, AppError, Field] =
    ZIO.accessM(_.get.randomMove(board))

}
