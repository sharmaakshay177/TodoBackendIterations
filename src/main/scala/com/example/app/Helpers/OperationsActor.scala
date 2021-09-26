package com.example.app.Helpers

import akka.actor.{Actor, PoisonPill}
import com.example.app.Helpers.EncoderAndDecoders.{convertListOfTaskToJsonResponse, getTaskFromJson, getUpdateTaskFromJson}
import com.example.app.database.InMemoryDB.{delete, insert, retrieve, update}
import com.example.app.models.{ID, Task, UpdateModel}
import com.typesafe.scalalogging.Logger
import io.circe.{Error, Json}

case object STOP

case class InsertMessage(taskID: ID, jsonBody: String)
case class UpdateMessage(taskId: ID, jsonBody: String)
case class DeleteMessage(taskId: ID)
case class RetrieveMessage(authorName: String)

class InsertActor extends Actor{
  private val logger = Logger(clazz = getClass)

  override def receive: Receive = {
    case InsertMessage(id, jsonBody) =>
      val taskFromJson: Either[Error, Task] = getTaskFromJson(jsonBody)
      taskFromJson match {
        case Right(task) =>
          logger.info(s"Json to Task conversion $task")
          insert(id, task)
        case Left(parserError) =>
          logger.error(parserError.getMessage)
      }
    case STOP => self ! PoisonPill
  }
}

class UpdateActor extends Actor{
  private val logger = Logger(clazz = getClass)

  override def receive: Receive ={
    case UpdateMessage(id, jsonBody) =>
      val updateTaskFromJson: Either[Error, UpdateModel] = getUpdateTaskFromJson(jsonBody)
      updateTaskFromJson match {
        case Right(updateTask) =>
          logger.info(s"Update Json to task conversion $updateTask")
          update(id, updateTask.message, updateTask.description)
        case Left(parserError) =>
          logger.error(parserError.getMessage)
      }
    case STOP => self ! PoisonPill
  }
}

class DeleteActor extends Actor{

  override def receive: Receive ={
    case DeleteMessage(id) =>
      delete(id)
    case STOP => self ! PoisonPill
  }
}

class RetrieveActor extends Actor{
  private val logger = Logger(clazz = getClass)

  override def receive: Receive ={
    case RetrieveMessage(name) =>
      logger.info(s"Retrieving data for $name")
      val listOfTaskForUser: List[Task] = retrieve(name)
      val response: Json = convertListOfTaskToJsonResponse(listOfTaskForUser)
      sender() ! response
    case STOP => self ! PoisonPill
  }
}