package com.example.todo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TASK = """task"""
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun addTask(task: String) {
        writableDatabase.execSQL("""
            INSERT INTO $TABLE_TASKS ($COLUMN_TASK) VALUES (?)
        """, arrayOf(task))
    }

    fun getAllTasks(): List<String> {
        val tasks = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_TASKS", null)
        while (cursor.moveToNext()) {
            tasks.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK)))
        }
        cursor.close()
        return tasks
    }

    fun removeTask(task: String) {
        writableDatabase.delete(TABLE_TASKS, "$COLUMN_TASK = ?", arrayOf(task))
    }

    fun updateTask(oldTask: String, newTask: String) {
        writableDatabase.execSQL("""
            UPDATE $TABLE_TASKS SET $COLUMN_TASK = ? WHERE $COLUMN_TASK = ?
        """, arrayOf(newTask, oldTask))
    }
}