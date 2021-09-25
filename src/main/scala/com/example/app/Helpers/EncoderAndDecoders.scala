package com.example.app.Helpers

import com.example.app.Constants.MessageFieldsName.{AuthorName, Description, Message, TaskId, TaskResponseHeader, TaskStatus}
import com.example.app.models.{ID, Task, UpdateModel}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Error, Json}

object EncoderAndDecoders {

  private implicit val updateTaskDecoder: Decoder[UpdateModel] = Decoder.instance{
    item =>
      for{
        message <- item.get[String](Message)
        description <- item.get[String](Description)
      } yield UpdateModel(message, if(description.nonEmpty) Some(description) else None)
  }

  private implicit val taskDecoder: Decoder[Task] = Decoder.instance{
    item =>
      for{
        taskId <- item.get[String](TaskId)
        authorName <- item.get[String](AuthorName)
        message <- item.get[String](Message)
        description <- item.get[String](Description)
        taskStatus <- item.get[String](TaskStatus)
      } yield Task(ID(taskId), authorName, message, if(description.nonEmpty) Some(description) else None, taskStatus)
  }


  private implicit val TaskEncoder: Encoder[Task] = new Encoder[Task] {
    override def apply(a: Task): Json = Json.obj(
      TaskId -> Json.fromString(a.id.id),
      AuthorName -> Json.fromString(a.author),
      Message -> Json.fromString(a.message),
      Description -> Json.fromString(if(a.description.isDefined) a.description.get else ""),
      TaskStatus -> Json.fromString(a.taskStatus)
    )
  }

  private implicit val ListOfTaskEncoder: Encoder[List[Task]] = new Encoder[List[Task]] {
    override def apply(a: List[Task]): Json ={
      val taskToJson: List[Json] = a.map(task => task.asJson)
      Json.obj(
        TaskResponseHeader -> Json.fromValues(taskToJson)
      )
    }
  }

  // Decoder Methods
  def getTaskFromJson(jsonReceived: String): Either[Error, Task] = decode[Task](jsonReceived)
  def getUpdateTaskFromJson(jsonReceived: String): Either[Error, UpdateModel] = decode[UpdateModel](jsonReceived)
  // Encoder Methods
  def convertTaskToJson(task: Task): Json = task.asJson
  def convertListOfTaskToJsonResponse(taskList: List[Task]): Json = taskList.asJson
}
