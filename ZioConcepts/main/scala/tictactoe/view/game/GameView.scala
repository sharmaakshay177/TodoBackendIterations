package tictactoe.view.game

import tictactoe.domain.Board.Field
import tictactoe.domain.{GameFooterMessage, GameResult, Piece, Player}
import zio.{Has, UIO, URIO, ZIO}

trait GameView {
  def header(result: GameResult, turn: Piece, player: Player): UIO[String]
  def content(board: Map[Field, Piece], result: GameResult): UIO[String]
  def footer(message: GameFooterMessage): UIO[String]
}
object GameView {
  def header(result: GameResult, turn: Piece, player: Player): URIO[Has[GameView], String] =
    ZIO.serviceWith[GameView](_.header(result, turn, player))

  def content(board: Map[Field, Piece], result: GameResult): URIO[Has[GameView], String] =
    ZIO.serviceWith[GameView](_.content(board, result))

  def footer(message: GameFooterMessage): URIO[Has[GameView], String] = ZIO.serviceWith[GameView](_.footer(message))
}