package com.example.todo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val list = findViewById<ListView>(R.id.listOfView)
        val txt = findViewById<TextView>(R.id.inputTxt)
        val buttonIn = findViewById<Button>(R.id.buttonIn)
        val buttonRemove = findViewById<Button>(R.id.buttonRemove)

        dbHelper = TaskDatabaseHelper(this)
        val todo = dbHelper.getAllTasks().toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todo)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE


        buttonIn.setOnClickListener {
            val text = txt.text.toString().trim()
            if (text.isNotEmpty()) {
                dbHelper.addTask(text)
                refreshUI()
                txt.text = ""
            } else {
                Toast.makeText(this, "Вы не ввели ничего в поле!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonRemove.setOnClickListener {
            val selectedPosition = list.checkedItemPosition
            if (selectedPosition != ListView.INVALID_POSITION) {
                val task = todo[selectedPosition]
                dbHelper.removeTask(task)
                refreshUI()
                list.clearChoices()
            } else {
                Toast.makeText(this, "Выберите элемент для удаления", Toast.LENGTH_SHORT).show()
            }
        }

        list.setOnItemLongClickListener { _, view, position, _ ->
            showEditDialog(todo, adapter, position, view)
            true
        }
    }

    private fun refreshUI() {
        val updatedTasks = dbHelper.getAllTasks()
        adapter.clear()
        adapter.addAll(updatedTasks)
        adapter.notifyDataSetChanged()
    }

    private fun showEditDialog(
        todo: MutableList<String>,
        adapter: ArrayAdapter<String>,
        position: Int,
        view: View
    ) {
        val task = todo[position]
        val editText = EditText(this)
        editText.setText(task)

        AlertDialog.Builder(this)
            .setTitle("Редактирование задачи")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                val newTask = editText.text.toString().trim()
                if (newTask.isNotEmpty()) {
                    dbHelper.updateTask(task, newTask)
                    refreshUI()
                    Toast.makeText(this, "Задача успешно изменена", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Новый текст не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}