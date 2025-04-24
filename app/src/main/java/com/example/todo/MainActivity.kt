package com.example.todo

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.models.TaskCreate
import com.example.todo.models.TaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<String>
    private val tasks = mutableListOf<TaskResponse>()
    private lateinit var list: ListView
    private lateinit var txt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI элементов
        initViews()

        // Настройка адаптера
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        // Загрузка задач
        fetchTasks()

        // Настройка обработчиков событий
        setupListeners()
    }

    private fun initViews() {
        list = findViewById(R.id.listOfView)
        txt = findViewById(R.id.inputTxt)
    }

    private fun setupListeners() {
        val buttonIn = findViewById<Button>(R.id.buttonIn)
        val buttonRemove = findViewById<Button>(R.id.buttonRemove)

        buttonIn.setOnClickListener {
            val text = txt.text.toString().trim()
            if (text.isNotEmpty()) {
                createTask(text)
                txt.text.clear()
            } else {
                showToast("Вы не ввели ничего в поле!")
            }
        }

        buttonRemove.setOnClickListener {
            val selectedPosition = list.checkedItemPosition
            if (selectedPosition != ListView.INVALID_POSITION) {
                val task = tasks[selectedPosition]
                deleteTask(task.id)
                list.clearChoices()
            } else {
                showToast("Выберите элемент для удаления")
            }
        }

        list.setOnItemLongClickListener { _, _, position, _ ->
            showEditDialog(position)
            true
        }
    }
    private fun fetchTasks() {
        RetrofitInstance.api.getAllTasks().enqueue(object : Callback<List<TaskResponse>> {
            override fun onResponse(call: Call<List<TaskResponse>>, response: Response<List<TaskResponse>>) {
                if (response.isSuccessful) {
                    tasks.clear()
                    response.body()?.let { tasks.addAll(it) }
                    refreshUI()
                } else {
                    showToast("Ошибка загрузки задач")
                }
            }

            override fun onFailure(call: Call<List<TaskResponse>>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun createTask(taskText: String) {
        val task = TaskCreate(Task = taskText)
        RetrofitInstance.api.createTask(task).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        tasks.add(it)
                        refreshUI()
                    }
                } else {
                    showToast("Ошибка создания задачи")
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun deleteTask(taskId: Int) {
        RetrofitInstance.api.deleteTask(taskId).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    tasks.removeAll { it.id == taskId }
                    refreshUI()
                } else {
                    showToast("Ошибка удаления задачи")
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun updateTask(taskId: Int, newTaskText: String) {
        val task = TaskCreate(Task = newTaskText)
        RetrofitInstance.api.updateTask(taskId, task).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { updatedTask ->
                        val index = tasks.indexOfFirst { it.id == taskId }
                        if (index != -1) {
                            tasks[index] = updatedTask
                            refreshUI()
                        }
                    }
                } else {
                    showToast("Ошибка обновления задачи")
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun refreshUI() {
        val taskNames = tasks.map { it.Task ?: "" }
        adapter.clear()
        adapter.addAll(taskNames)
        adapter.notifyDataSetChanged()
    }

    private fun showEditDialog(position: Int) {
        val task = tasks[position]
        val editText = EditText(this).apply {
            setText(task.Task)
        }

        AlertDialog.Builder(this)
            .setTitle("Редактирование задачи")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                val newTaskText = editText.text.toString().trim()
                if (newTaskText.isNotEmpty()) {
                    updateTask(task.id, newTaskText)
                } else {
                    showToast("Новый текст не может быть пустым")
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}