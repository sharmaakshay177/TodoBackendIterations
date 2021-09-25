package com.example.app.database

import com.example.app.models.{ID, Task}
import scala.collection.mutable
import com.typesafe.scalalogging.Logger


object InMemoryDB {

  private val logger = Logger(clazz = getClass)
  private val inMemoryDb: mutable.Map[String, Task] = mutable.Map.empty[String, Task]

  def insert(id: ID, task: Task): Unit = {
    inMemoryDb += ((id.id, task))
    logger.info(s"[DataBase] Task inserted into Db with ${id.id}")
  }

  def delete(id: ID): Unit = {
    inMemoryDb -= id.id
    logger.info(s"[DataBase] Task removed into Db with ${id.id}")
  }

  def update(id: ID, message: String, description: Option[String] = None): Unit ={
    val taskId = id.id
    val task = inMemoryDb(taskId)
    val newTaskObject =
      if (description.isDefined) task.copy(message = message, description = description)
      else task.copy(message = message)
    logger.info(s"[DateBase] Task Updated: $newTaskObject")
    inMemoryDb.update(taskId, newTaskObject)
  }

  def retrieve(author: String): List[Task] ={
    logger.info(s"[DataBase] Retrieving data for user $author")
    val recordsAccordingToUser = inMemoryDb.values.filter(task => task.author.equalsIgnoreCase(author)).toList
    recordsAccordingToUser
  }
}

object Testing extends App{
  val id = ID("test-id1")
  val id2 = ID("test-id2")
  val task = Task(id, "akshay", "testing task", None, "Open")
  val task2 = Task(id2, "akshay", "testing task closed", None, "Closed")
  val newMessage = "testingTask-V2"
  InMemoryDB.insert(id, task)
  InMemoryDB.update(id, newMessage)
  InMemoryDB.insert(id2, task2)
  InMemoryDB.retrieve("akshay").foreach(println)
  InMemoryDB.delete(id2)
  InMemoryDB.retrieve("akshay").foreach(println)
}
