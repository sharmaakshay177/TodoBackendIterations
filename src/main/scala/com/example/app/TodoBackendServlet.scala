package com.example.app

import com.example.app.Helpers.TodoBackendServletHelper.{deleteHelper, insertHelper, retrieverHelper, updateHelper}
import com.example.app.models.ID
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import com.typesafe.scalalogging.Logger
import io.circe.Json


class TodoBackendServlet extends ScalatraServlet {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = Logger(clazz = getClass)

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
    val responseToSend: Json = retrieverHelper(authorName)
    responseToSend
  }

  post("/insert/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Insert with ID: $taskIdFromParams")
    val requestBody = request.body
    insertHelper(ID(taskIdFromParams), requestBody)
    response.addHeader("Status", "Data Inserted Successfully")
  }

  post("/update/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Update with ID: $taskIdFromParams")
    val requestBody = request.body
    updateHelper(ID(taskIdFromParams), requestBody)
    response.addHeader("Status", "Task Updated Successfully")
  }

  //todo: refactor it delete method instead of post
  post("delete/:taskId"){
    logger.info(s"Request for ${request.getRequestURL}")
    val taskIdFromParams: String = params("taskId")
    logger.info(s"Request Received for Delete with ID: $taskIdFromParams")
    deleteHelper(ID(taskIdFromParams))
    response.addHeader("Status", "Task Deleted Successfully")
  }

  notFound{
    logger.info(s"Logger form Not Found")
    logger.info(s" URL request for ${request.getRequestURL}")
    logger.info(s" Query string for ${request.getQueryString}")
    <h1>Not found. Bummer!!!!!</h1>
  }

}
