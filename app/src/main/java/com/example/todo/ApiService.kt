package com.example.todo

import com.example.todo.models.TaskCreate
import com.example.todo.models.TaskResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("tasks/get_all_tasks")
    fun getAllTasks(): Call<List<TaskResponse>>

    @POST("tasks/create_task")
    fun createTask(@Body task: TaskCreate): Call<TaskResponse>

    @PUT("tasks/update_task_by_id/{task_id}")
    fun updateTask(
        @Path("task_id") taskId: Int,
        @Body task: TaskCreate
    ): Call<TaskResponse>

    @DELETE("tasks/delete_task_by_id/{task_id}")
    fun deleteTask(@Path("task_id") taskId: Int): Call<TaskResponse>
}