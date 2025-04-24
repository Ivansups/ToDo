package com.example.todo

class Models {
    data class TaskCreate(
        val Task: String
    )

    data class TaskResponse(
        val id: Int,
        val Task: String
    )
}