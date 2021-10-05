package com.example.app.Helpers

import akka.actor.{Actor, PoisonPill}
import com.example.app.Helpers.EncoderAndDecoders.convertListOfTaskToJsonResponse
import com.example.app.database.InMemoryDB.{delete, insert, retrieve, update}
import com.example.app.models.Task
import com.typesafe.scalalogging.Logger
import io.circe.Json

class TaskActor extends Actor{
  private val logger = Logger(clazz = getClass)

  override def receive: Receive ={
    case InsertMessage(task) =>
      logger.info(s"Inserting task to Database :$task")
      insert(task.id, task)
    case UpdateMessage(id, updateModel) =>
      logger.info(s"Updating the task $id with given update $updateModel")
      update(id, updateModel.message, updateModel.description)
    case DeleteMessage(id) => delete(id)
    case STOP => self ! PoisonPill
  }

}

class GetActor extends Actor{
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
