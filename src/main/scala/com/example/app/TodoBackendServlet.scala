package com.example.app

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.example.app.Helpers.{DeleteActor, DeleteMessage, InsertActor, InsertMessage, RetrieveActor, RetrieveMessage, STOP, UpdateActor, UpdateMessage}
import com.example.app.Helpers.TodoBackendServletHelper.{deleteHelper, insertHelper, retrieverHelper, updateHelper}
import com.example.app.models.ID
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import com.typesafe.scalalogging.Logger
import io.circe.Json

import scala.concurrent.Await
import scala.concurrent.duration._

class TodoBackendServlet extends ScalatraServlet {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = Logger(clazz = getClass)

  implicit val timeout: Timeout = Timeout(2.seconds)
  private val system: ActorSystem = ActorSystem("TodoBackendActors")

  private def getActorsWithUniquePath(route: String): ActorRef ={
    val maxLimit = 100000000
    var countRet = 0
    var countUpdate = 0
    var countDel = 0
    var countIns = 0
    route match {
      case "get" =>
        countRet = countRet + 1
        system.actorOf(Props[RetrieveActor], s"RetrieveActorCreated$countRet")
      case "update" =>
        countUpdate = countUpdate + 1
        system.actorOf(Props[UpdateActor], s"UpdateActorCreated$countUpdate")
      case "delete" =>
        countDel = countDel + 1
        system.actorOf(Props[DeleteActor], s"DeleteActorCreated$countDel")
      case "insert" =>
        countIns = countIns + 1
        system.actorOf(Props[InsertActor], s"InsertActorCreated$countIns")
    }
  }

  before(){
    contentType = "json"
  }

  get("/ping") {
    logger.info(s"Request for ${request.getRequestURL}")
    logger.info("Request received for ping")
    val Response = "Response: Pong"
    response.setHeader("Response", "Pong")
    Response
  }

  get("/get/:authorName"){
    logger.info(s"Request for ${request.getRequestURL}")
    val authorName: String = params("authorName")
    logger.info(s"Request received for get with author name $authorName")
    val getActor = getActorsWithUniquePath("get")
    logger.info(s"Actor created with path ${getActor.path}")
    val response = ask(getActor, RetrieveMessage(authorName)).mapTo[Json]
    val jsonToSend = Await.result(response, timeout.duration)
    //val responseToSend: Json = retrieverHelper(authorName)
    getActor ! STOP
    jsonToSend
  }

  post("/insert/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Insert with ID: $taskIdFromParams")
    val requestBody = request.body
    val insertActor = getActorsWithUniquePath("insert")
    logger.info(s"Actor created with path ${insertActor.path}")
    insertActor ! InsertMessage(ID(taskIdFromParams), requestBody)
    //insertHelper(ID(taskIdFromParams), requestBody)
    insertActor ! STOP
    response.addHeader("Status", "Data Inserted Successfully")
  }

  post("/update/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Update with ID: $taskIdFromParams")
    val requestBody = request.body
    val updateActor = getActorsWithUniquePath("update")
    logger.info(s"Actor created with path ${updateActor.path}")
    updateActor ! UpdateMessage(ID(taskIdFromParams), requestBody)
    //updateHelper(ID(taskIdFromParams), requestBody)
    updateActor ! STOP
    response.addHeader("Status", "Task Updated Successfully")
  }

  //todo: refactor it delete method instead of post
  post("/delete/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Delete with ID: $taskIdFromParams")
    val deleteActor = getActorsWithUniquePath("delete")
    logger.info(s"Actor created with path ${deleteActor.path}")
    deleteActor ! DeleteMessage(ID(taskIdFromParams))
    deleteActor ! STOP
    //deleteHelper(ID(taskIdFromParams))
    response.addHeader("Status", "Task Deleted Successfully")
  }

  notFound{
    logger.info(s"Logger form Not Found")
    logger.info(s" URL request for ${request.getRequestURL}")
    logger.info(s" Query string for ${request.getQueryString}")
    <h1>Not found. Bummer!!!!!</h1>
  }

}
