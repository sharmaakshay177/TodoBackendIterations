package tictactoe.opponentAi

import tictactoe.domain.{AppError, FullBoardError, Piece}
import tictactoe.domain.Board.Field
import zio.{Has, IO, URLayer}
import zio.random.Random

final case class OpponentAiLive(random: Random.Service) extends OpponentAi {
  override def randomMove(board: Map[Field, Piece]): IO[AppError, Field] = {
    val unoccupied = (Field.all.toSet -- board.keySet).toList.sortBy(_.value)
    unoccupied.size match {
      case 0 => IO.fail(FullBoardError)
      case n => random.nextIntBounded(n).map(unoccupied(_))
    }
  }
}
object OpponentAiLive {
  val layer: URLayer[Has[Random.Service], Has[OpponentAi]] = (OpponentAiLive(_)).toLayer
}
