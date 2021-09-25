package com.example.app.Helpers

import com.example.app.Helpers.EncoderAndDecoders.{convertListOfTaskToJsonResponse, convertTaskToJson, getTaskFromJson, getUpdateTaskFromJson}
import com.example.app.models.{ID, Task, UpdateModel}
import com.typesafe.scalalogging.Logger
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper


class TodoBackendServletHelperSpec extends AnyFlatSpecLike {

  private val logger = Logger(clazz = getClass)

  private val TaskJsonStringWithAllFields =
  """{
    |    "id": "test-id-123",
    |    "author": "akshay",
    |    "message": "this is testing task to parse",
    |    "description": "this is task description",
    |    "taskStatus": "OPEN"
    |}""".stripMargin

  private val TaskJsonStringWithAllFieldsWithDescriptionPlaceHolder =
    """{
      |    "id": "test-id-123",
      |    "author": "akshay",
      |    "message": "this is testing task to parse",
      |    "description": "",
      |    "taskStatus": "OPEN"
      |}""".stripMargin

  private val TaskUpdateJsonStringAllFields =
    """{
      |    "message": "this is testing task to parse from update",
      |    "description": "this is task description from update"
      |}""".stripMargin

  private val TaskUpdateJsonStringAllFieldsWithDescriptionPlaceHolder =
    """{
      |    "message": "this is testing task to parse from update",
      |    "description": ""
      |}""".stripMargin


  "Json Decoder" should "be able to return the Task object extracted from json in correct format" in {
    val expectedTask: Task = Task(ID("test-id-123"),
      "akshay",
      "this is testing task to parse",
      Some("this is task description"),
      "OPEN")

    val expectedTaskWithoutPlaceHolderValue = Task(
      ID("test-id-123"),
      "akshay",
      "this is testing task to parse",
      None,
      "OPEN"
    )

    val actualTaskReceived = getTaskFromJson(TaskJsonStringWithAllFields)
    val actualTaskReceivedWithPlaceHolder = getTaskFromJson(TaskJsonStringWithAllFieldsWithDescriptionPlaceHolder)

    actualTaskReceived match {
      case Right(taskReceived) =>
        logger.info(s"Task received after json parsing $actualTaskReceived")
        taskReceived shouldBe expectedTask
    }

    actualTaskReceivedWithPlaceHolder match {
      case Right(taskReceived) =>
        logger.info(s"Task received after json parsing $actualTaskReceivedWithPlaceHolder")
        taskReceived shouldBe expectedTaskWithoutPlaceHolderValue
    }
  }

  "Json Decoder" should "be able to parse update message json as well" in {
    val expectedTask = UpdateModel("this is testing task to parse from update",
                                   Some("this is task description from update"))

    val expectedPlaceHolderTask = UpdateModel("this is testing task to parse from update", None)

    val actualExpectedTask = getUpdateTaskFromJson(TaskUpdateJsonStringAllFields)
    val actualExpectedTaskWithPlaceHolder = getUpdateTaskFromJson(TaskUpdateJsonStringAllFieldsWithDescriptionPlaceHolder)

    actualExpectedTask match {
      case Right(taskReceived) =>
        logger.info(s"Update Task Received $taskReceived")
        taskReceived shouldBe expectedTask
    }

    actualExpectedTaskWithPlaceHolder match {
      case Right(taskReceived) =>
        logger.info(s"Update Task Received $taskReceived")
        taskReceived shouldBe expectedPlaceHolderTask
    }

  }

  "Json Encoder" should "be able to convert a task to its corresponding json" in {
    val expectedJsonValue = """{"id":"test-id-123","author":"akshay","message":"this is testing task to parse","description":"this is task description","taskStatus":"OPEN"}""".stripMargin
    val task = Task(
      ID("test-id-123"),
      "akshay",
      "this is testing task to parse",
      Some("this is task description"),
      "OPEN")

    val taskConvertedToJson = convertTaskToJson(task)
    logger.info(s"json Received ${taskConvertedToJson.noSpaces}")
    taskConvertedToJson.noSpaces shouldBe expectedJsonValue

    val expectedJsonValueWithoutTaskDescription = """{"id":"test-id-123","author":"akshay","message":"this is testing task to parse","description":"","taskStatus":"OPEN"}""".stripMargin
    val taskWithoutDescription = Task(
      ID("test-id-123"),
      "akshay",
      "this is testing task to parse",
      None,
      "OPEN")

    val convertedToJsonForObjectWithoutTaskDescription = convertTaskToJson(taskWithoutDescription)
    logger.info(s"Json Received ${convertedToJsonForObjectWithoutTaskDescription.noSpaces}")
    convertedToJsonForObjectWithoutTaskDescription.noSpaces shouldBe expectedJsonValueWithoutTaskDescription

  }

  "Json Encoder" should "give be able to final json converted from task retrieved according to user" in{
    val expectedJsonValue = """{"tasks":[{"id":"test-id-123","author":"akshay","message":"this is testing task to parse","description":"this is task description","taskStatus":"OPEN"}]}""".stripMargin
    val task = Task(
      ID("test-id-123"),
      "akshay",
      "this is testing task to parse",
      Some("this is task description"),
      "OPEN")

    val finalDataListConverted = convertListOfTaskToJsonResponse(List(task))
    logger.info(s"Final Json received ${finalDataListConverted.noSpaces}")
    finalDataListConverted.noSpaces shouldBe expectedJsonValue
  }

}
