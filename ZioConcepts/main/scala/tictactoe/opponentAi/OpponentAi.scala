package tictactoe.opponentAi

import tictactoe.domain.Board.Field
import tictactoe.domain.{AppError, Piece}
import zio.{Has, IO, ZIO}

trait OpponentAi {
  def randomMove(board: Map[Field, Piece]): IO[AppError, Field]
}
object OpponentAi {
  def randomMove(board: Map[Field, Piece]): ZIO[Has[OpponentAi], AppError, Field] =
    ZIO.serviceWith[OpponentAi](_.randomMove(board))
}