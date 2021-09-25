package com.example.app.models

case class Task(id: ID,
                author: String,
                message: String,
                description: Option[String],
                taskStatus: String)
