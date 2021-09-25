package com.example.app.Helpers

import com.example.app.Helpers.EncoderAndDecoders.{convertListOfTaskToJsonResponse, getTaskFromJson, getUpdateTaskFromJson}
import com.example.app.models.{ID, Task, UpdateModel}
import io.circe._
import com.example.app.database.InMemoryDB.{delete, insert, retrieve, update}
import com.typesafe.scalalogging.Logger
import io.circe.Json

object TodoBackendServletHelper {
  private val logger = Logger(clazz = getClass)

  // retriever helper
  def retrieverHelper(authorName: String): Json = {
    logger.info(s"Retrieving data for $authorName")
    val listOfTaskByUser: List[Task] = retrieve(authorName)
    val jsonResponse = convertListOfTaskToJsonResponse(listOfTaskByUser)
    jsonResponse
  }

  // delete helper
  def deleteHelper(id: ID): Unit = delete(id)

  // insert helper
  def insertHelper(id: ID, requestBody: String): Unit = {
    logger.info(s"Json message received $requestBody")
    val taskToInsert: Either[Error, Task] = getTaskFromJson(requestBody)
    taskToInsert match {
      case Right(task) =>
        logger.info(s"Json to Task conversion $task")
        insert(id, task)
      case Left(parserError) => logger.error(parserError.getMessage)
    }
  }

  // update helper
  def updateHelper(id: ID, requestBody: String): Unit ={
    logger.info(s"Json message received for update request $requestBody")
    val taskDetailsToUpdate: Either[Error, UpdateModel] = getUpdateTaskFromJson(requestBody)
    taskDetailsToUpdate match {
      case Right(updateTask) =>
        logger.info(s"Update Json to task conversion $updateTask")
        update(id, updateTask.message, updateTask.description)
      case Left(parserError) => logger.error(parserError.getMessage)
    }
  }

}
