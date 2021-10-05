package com.example.app.Helpers

import com.example.app.models.{ID, Task, UpdateModel}
case object STOP

case class InsertMessage(task: Task)
case class UpdateMessage(id: ID, updateModel: UpdateModel)
case class DeleteMessage(taskId: ID)
case class RetrieveMessage(authorName: String)
