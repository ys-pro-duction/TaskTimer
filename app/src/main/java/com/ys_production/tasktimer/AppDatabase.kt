package com.ys_production.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val databaseName = "TaskTimer.db"
private const val databaseVersion = 1
private const val TAG = "AppDatabase"
class AppDatabase private constructor(context: Context):
    SQLiteOpenHelper(context, databaseName,null, databaseVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate: start")

        val sql = """CREATE TABLE IF NOT EXISTS ${TasksContract.TABLE_NAME}(${TasksContract.Columns.ID} INTEGER PRIMARY KEY,
                 ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
                 ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
                 ${TasksContract.Columns.TASK_SHORT_ORDER} INTEGER);""".replaceIndent()
        db.execSQL(sql)
        Log.d(TAG, "onCreate: SQLiteDatabaseVersion ${db.version}")
        Log.d(TAG, "onCreate: SQLiteDatabasePath ${db.path}")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: start")
        Log.d(TAG, "onUpgrade: version OLD: $oldVersion NEW $newVersion")
        when(oldVersion){
            1 -> {
                val cursor = db.rawQuery("SELECT * FROM ${TasksContract.TABLE_NAME}",null)
                cursor.use {
                    val stringBuilder = StringBuilder()
                    while (it.moveToNext()){
                        stringBuilder
                            .append("ID: ${it.getInt(0)}\n")
                            .append("NAME: ${it.getString(1)}\n")
                            .append("DESCRIPTION: ${it.getString(2)}\n")
                            .append("SHORTORDER ${it.getInt(3)}\n\n")
                    }
                    Log.d(TAG, "onUpgrade: DATA::::::::::::::::::::::::::::::\n $stringBuilder")
                }
                cursor.close()
            }
            else ->{
                throw IllegalStateException("unknown version $newVersion")
            }
        }

    }
    companion object: SingletonHolder<AppDatabase,Context>(::AppDatabase)
}