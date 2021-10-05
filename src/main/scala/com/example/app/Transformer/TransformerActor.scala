package com.example.app.Transformer

import akka.actor.Actor
import com.example.app.Constants.Operations.{INSERT, UPDATE}
import com.example.app.Helpers.EncoderAndDecoders.{getTaskFromJson, getUpdateTaskFromJson}
import com.example.app.models.{Task, UpdateModel}
import com.typesafe.scalalogging.Logger
import io.circe.Error

case class TransformJson(operation: String, jsonMessage: String)

class TransformerActor extends Actor{
  private val logger = Logger(clazz = getClass)

  override def receive: Receive ={

    case TransformJson(operation, jsonMessage) if operation == INSERT =>
      logger.info(s"Insert json Received: $jsonMessage")
      val convertedToTask: Either[Error, Task] = getTaskFromJson(jsonMessage)
      convertedToTask match {
        case Right(task) =>
          logger.info(s"Json to Task conversion $task")
          sender() ! task
        case Left(parserError) => logger.error(s"[Parser Error] ${parserError.getMessage}")
      }
    case TransformJson(operation, jsonMessage) if operation == UPDATE =>
      logger.info(s"Insert json Received: $jsonMessage")
      val convertedToUpdateModel: Either[Error, UpdateModel] = getUpdateTaskFromJson(jsonMessage)
      convertedToUpdateModel match {
        case Right(updatedTask) =>
          logger.info(s"Json to UpdatedModel conversion $updatedTask")
          sender() ! updatedTask
        case Left(parserError) => logger.error(s"[Parser Error] ${parserError.getMessage}")
      }
  }
}
