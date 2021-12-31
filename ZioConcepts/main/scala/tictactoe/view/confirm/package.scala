package tictactoe.view

import tictactoe.domain.{ConfirmAction, ConfirmFooterMessage}
import zio.{Has, UIO, ULayer, URIO, ZIO, ZLayer}

package object confirm {
  type ConfirmView = Has[ConfirmView.Service]

  object ConfirmView{
    trait Service {
      def header(action: ConfirmAction): UIO[String]
      def content: UIO[String]
      def footer(message: ConfirmFooterMessage): UIO[String]
    }

   val live: ULayer[ConfirmView] = ZLayer.succeed{
     new Service {
       override def header(action: ConfirmAction): UIO[String] = {
         UIO.succeed(action).map {
           case ConfirmAction.NewGame =>
             """[New game]
               |
               |This will discard current game progress.""".stripMargin
           case ConfirmAction.Quit =>
             """[Quit]
               |
               |This will discard current game progress.""".stripMargin
         }
       }

       override def content: UIO[String] = {
         UIO.succeed(
           """Are you sure?
             |<yes> / <no>""".stripMargin
         )
       }

       override def footer(message: ConfirmFooterMessage): UIO[String] = {
         UIO.succeed(message) map {
           case ConfirmFooterMessage.Empty          => ""
           case ConfirmFooterMessage.InvalidCommand => "Invalid command. Try again."
         }
       }
     }
   }

  } // object ending

  // accessors
  def header(action: ConfirmAction): URIO[ConfirmView, String] =
    ZIO.accessM(_.get.header(action))

  def content: URIO[ConfirmView, String] =
    ZIO.accessM(_.get.content)

  def footer(message: ConfirmFooterMessage): URIO[ConfirmView, String] =
    ZIO.accessM(_.get.footer(message))
}
