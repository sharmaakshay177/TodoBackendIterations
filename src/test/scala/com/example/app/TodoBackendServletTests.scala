package com.example.app

import org.scalatra.test.scalatest._

class TodoBackendServletTests extends ScalatraFunSuite {

  override def header = ???

  addServlet(classOf[TodoBackendServlet], "/*")

  test("GET / on TodoBackendServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

  test("GET / get route should return the status 200"){
    get("/get/testUser"){
      status should equal (200)
    }
  }

  test("GET / on ping route should return the status 200") {
    get("/ping") {
      status should equal (200)
    }
  }

  test(" Post / insert route should return the status 200") {
    post("/insert/test123") {
      status should equal (200)
    }
  }

  test("Post / update route should return the status 200"){
    post("/update/test123"){
      status should equal (200)
    }
  }

  test("Post / delete route should return the status 200"){
    post("/delete/test123"){
      status should equal (200)
    }
  }

  test("when a non matching route is called not found should handle and return the status 200"){
    post("/someRoute"){
      status should equal (200)
    }
  }

}
