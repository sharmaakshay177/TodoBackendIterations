package com.example.app

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.example.app.Helpers._
import com.example.app.Transformer.{TransformJson, TransformerActor}
import com.example.app.models.{ID, Task, UpdateModel}
import com.typesafe.scalalogging.Logger
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.json4s.{DefaultFormats, Formats}

import org.scalatra._

import com.roundeights.hasher.Digest.digest2string
import com.roundeights.hasher.Implicits._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._


case class Session(id: String)

  /**
   * implementation steps
   * get session-id and user details who started the session (done with login)
   * create a get actor for the entire session which will fetch the task for that user (done)
   *
   * -- task actor creation will be created when create actor is called --
   * -- actor will hold operations crete, update, delete --
   * -- actor deletion will be taken care when the delete is called for the task --
   * -- when the user session is closed all the actors associated with the session is terminated --
   *
   * add logout as well to remove that session actor (done)
   * */
class TodoBackendServlet extends ScalatraServlet {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = Logger(clazz = getClass)

  implicit val timeout: Timeout = Timeout(2.seconds)
  private val system: ActorSystem = ActorSystem("TodoBackendActors")
  private val transformerActor = system.actorOf(Props[TransformerActor], "TransformerActorCreated")

  // stores for sessions and task lists
  private val sessionStore: mutable.Map[String, ActorRef] = mutable.Map.empty[String, ActorRef] // session id to getActorForThatSession
  private val taskToActorStore: mutable.Map[String, ActorRef] = mutable.Map.empty[String, ActorRef] // task to corresponding actor
  private val sessionToTasksStore: mutable.Map[String, List[String]] = mutable.Map.empty[String, List[String]] // session to corresponding tasks

  private def getSessionRetrieveActor(sessionID: String): ActorRef ={
    val path: String = "GetActorForSession".concat(sessionID)
    val getActor = system.actorOf(Props[GetActor], path)
    logger.info(s"Session Actor created path: ${getActor.path}")
    getActor
  }

  private def getActorForaTask(sessionID: String, taskId: String): ActorRef ={
    val path: String = "TaskActor".concat(sessionID).concat(taskId)
    val taskActor = system.actorOf(Props[TaskActor], path)
    logger.info(s"Task actor created path: ${taskActor.path}")
    taskActor
  }

  private def generateSessionId(username: String): String ={
    val sessionIdForUser: String = digest2string(username.sha256)
    sessionIdForUser
  }

  before(){
    contentType = "json"
  }

  get("/ping") {
    logger.info(s"Request for ${request.getRequestURL}")
    logger.info("Request received for ping")
    val Response = "Response: Pong".asJson
    response.setHeader("Response", "Pong")
    Response
  }

  post("/login/:username"){
    // verify credential
    // logic to be added later
    // create a session for the user
    val userName: String = params("username")
    val sessionID = generateSessionId(userName)
    // store the session id as main key to store in memory data
    servletContext.setAttribute(userName, Session(sessionID))
    // create actor and store it for get
    val getActorForThisSession = getSessionRetrieveActor(sessionID)
    sessionStore += ((sessionID, getActorForThisSession))
    // redirect to get route with the passed username
    redirect(s"/get/$userName")
  }

  get("/logout/:username"){
    logger.info(s"Request for ${request.getRequestURL}")
    val userName: String = params("username")
    logger.info(s"Request received for logout by user: $userName")

    val sessionIdForUser = servletContext.getAttribute(userName).asInstanceOf[Session]
    val sessionId = sessionIdForUser.id
    logger.info(s"Session id retrieved for $userName with Id :$sessionId")
    // delete get actor for that session
    val retrieveActorForUser: Option[ActorRef] = sessionStore.get(sessionId)
    retrieveActorForUser match {
      case Some(actor) =>
        logger.info(s"Removing Get Actor Path: ${actor.path}")
        actor ! PoisonPill
    }
    // delete task actors as well for the session
    val taskListForSession = sessionToTasksStore.get(sessionId)
    taskListForSession match {
      case Some(list) => list.foreach{
        task =>
          logger.info(s"Task ID retrieved to remove actor for :$task")
          val taskActor = taskToActorStore.get(task)
          taskActor match {
            case Some(actor) =>
              logger.info(s"Removing Task Actor Path: ${actor.path}")
              actor ! PoisonPill
          }
      }
    }
    sessionToTasksStore.remove(sessionId)
    // remove the entry from session store
    sessionStore.remove(sessionId)
    // remove from servlet context as well
    servletContext.removeAttribute(sessionId)
    logger.info(s"Session Removed :$sessionId")

    val responseMessage = "User Logged Out Successfully".asJson
    responseMessage
  }

  get("/get/:authorName"){
    logger.info(s"Request for ${request.getRequestURL}")
    val authorName: String = params("authorName")
    logger.info(s"Request received for get with author name $authorName")
    if (servletContext.contains(authorName)){
      logger.info(s"Session author name found")
      // get session id against this user
      val sessionIdForUser = servletContext.getAttribute(authorName).asInstanceOf[Session]
      logger.info(s"Session id retrieved :${sessionIdForUser.id}")
      // get the get actor for this user and serve the request
      val retrieveActorForUser: Option[ActorRef] = sessionStore.get(sessionIdForUser.id)
      val actorForUser = retrieveActorForUser match {
        case Some(actor) =>
          logger.info(s"Actor Retrieved path :${actor.path}")
          actor
      }
      val response = ask(actorForUser, RetrieveMessage(authorName)).mapTo[Json]
      val jsonToSend = Await.result(response, timeout.duration)
      jsonToSend
    }
    else{
      logger.error(s"Session is not registered for user: $authorName")
      response.addHeader("Error", "Session not registered. Please login !!!")
    }
  }

  post("/insert/:username/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val username: String = params("username")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Insert with ID: $taskIdFromParams")
    val requestBody = request.body
    // get session id for the user name
    if (servletContext.contains(username)){ // to check if session is registered
      logger.info(s"Session user name found")
      val sessionIdForUser = servletContext.getAttribute(username).asInstanceOf[Session]
      val taskActor = getActorForaTask(sessionIdForUser.id, taskIdFromParams)

      // add it to taskToActorStore
      taskToActorStore += ((taskIdFromParams, taskActor))
      logger.info(s"SessionToTaskStore Status Initial: ${sessionToTasksStore.values.toList}")
      // get list of task corresponding to session and add this task
      val listOfTaskForSession = sessionToTasksStore.get(sessionIdForUser.id)
      val newList: List[String] = listOfTaskForSession match {
        case Some(list) => list.toBuffer.addOne(taskIdFromParams).toList
        case None => List(taskIdFromParams)
      }
      sessionToTasksStore.update(sessionIdForUser.id, newList)
      logger.info(s"SessionToTaskStore Status Updated: ${sessionToTasksStore.values.toList}")

      logger.info(s"Actor created for task with path ${taskActor.path}")
      val taskFromJson = ask(transformerActor, TransformJson("insert",requestBody)).mapTo[Task]
      val task: Task = Await.result(taskFromJson, Timeout(1.milli).duration)
      taskActor ! InsertMessage(task)
      response.addHeader("Status", "Data Inserted Successfully")
    }
    else {
      logger.error(s"Session is not registered for user: $username")
      response.addHeader("Error", "Session not registered. Please login !!!")
    }
  }

  post("/update/:username/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    val username: String = params("username")
    logger.info(s"Request Received for Update with ID: $taskIdFromParams")
    if(servletContext.contains(username)){
      logger.info(s"Session user name found")
      val requestBody = request.body
      val updateModelFromJson = ask(transformerActor, TransformJson("update",requestBody)).mapTo[UpdateModel]
      val updateModel = Await.result(updateModelFromJson, Timeout(1.milli).duration)
      // get actor for this task
      val taskActor = taskToActorStore.get(taskIdFromParams)
      taskActor match {
        case Some(actor) =>
          logger.info(s"Actor retrieved with path: ${actor.path}")
          actor ! UpdateMessage(ID(taskIdFromParams), updateModel)
        case None => logger.error(s"No Actor found for task: $taskIdFromParams")
      }
      response.addHeader("Status", "Task Updated Successfully")
    } else {
      logger.error(s"Session is not registered for user: $username")
      response.addHeader("Error", "Session not registered. Please login !!!")
    }

  }

  post("/delete/:username/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val username: String = params("username")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Delete with ID: $taskIdFromParams")
    // get actor for passed task-id
    if (servletContext.contains(username)){
      logger.info(s"Session user name found")
      val sessionIdForUser = servletContext.getAttribute(username).asInstanceOf[Session]

      val listOfTaskForSession = sessionToTasksStore.get(sessionIdForUser.id)
      logger.info(s"SessionToTaskStore Status Initial: ${sessionToTasksStore.values.toList}")

      val newList: List[String] = listOfTaskForSession match {
        case Some(list) =>
          list.filter(task => task != taskIdFromParams)
        case None => List.empty
      }
      sessionToTasksStore.update(sessionIdForUser.id, newList)
      logger.info(s"SessionToTaskStore Status Updated After Delete: ${sessionToTasksStore.values.toList}")

      val taskActor = taskToActorStore.get(taskIdFromParams)
      taskActor match {
        case Some(actor) =>
          logger.info(s"Actor retrieved with path: ${actor.path}")
          actor ! DeleteMessage(ID(taskIdFromParams))
          actor ! PoisonPill
        case None =>
          logger.error(s"No actor found for task: $taskIdFromParams")
          response.addHeader("Error", "Task Actor Not Found check for session validity!!!")
      }
      response.addHeader("Status", "Task Deleted Successfully")
    } else {
      logger.error(s"Session is not registered for user: $username")
      response.addHeader("Error", "Session not registered. Please login !!!")
    }
  }

  notFound{
    logger.info(s"Logger form Not Found")
    logger.info(s" URL request for ${request.getRequestURL}")
    logger.info(s" Query string for ${request.getQueryString}")
    <h1>Not found. Bummer!!!!!</h1>
  }

}
