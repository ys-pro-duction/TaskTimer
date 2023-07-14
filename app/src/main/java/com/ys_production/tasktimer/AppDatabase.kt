package com.ys_production.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val databaseName = "TaskTimer.db"
private const val databaseVersion = 5
private const val TAG = "AppDatabase"

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, databaseName, null, databaseVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate: start")/*
        CREATE TABLE Tasks(_id INTEGER PRIMARY KEY,
        Name TEXT NOT NULL,
        Description TEXT,
        ShortOrder INTEGER);
         */
        val sql =
            """CREATE TABLE IF NOT EXISTS ${TasksContract.TABLE_NAME}(${TasksContract.Columns.ID} INTEGER PRIMARY KEY,
                 ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
                 ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
                 ${TasksContract.Columns.TASK_SHORT_ORDER} INTEGER);""".replaceIndent()
        db.execSQL(sql)
        addTimingTable(db)
        addCurrentTimingView(db)
        addDurationView(db)
        parameterView(db)
        Log.d(TAG, "onCreate: SQLiteDatabaseVersion ${db.version}")
        Log.d(TAG, "onCreate: SQLiteDatabasePath ${db.path}")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: start")
        Log.d(TAG, "onUpgrade: version OLD: $oldVersion NEW $newVersion")
        when (oldVersion) {
            1 -> {
                addTimingTable(db)
                addCurrentTimingView(db)
                addDurationView(db)
                parameterView(db)
            }

            2 -> {
                addCurrentTimingView(db)
                addDurationView(db)
                parameterView(db)
            }

            3 -> {
                addDurationView(db)
                parameterView(db)
            }
            4 ->{
                parameterView(db)
            }
            else -> {
                throw IllegalStateException("unknown version $newVersion")
            }
        }
    }

    private fun addTimingTable(db: SQLiteDatabase) {/*
        CREATE TABLE Timings(_id INTEGER PRIMARY KEY,
        TaskId INTEGER NOT NULL,
        StartTime INTEGER,
        Duration INTEGER);
         */
        val sqlTiming =
            """CREATE TABLE IF NOT EXISTS ${TimingsContract.TABLE_NAME}(${TimingsContract.Columns.ID} INTEGER PRIMARY KEY,
                 ${TimingsContract.Columns.TIMING_TASK_ID} INTEGER NOT NULL,
                 ${TimingsContract.Columns.TIMING_START_TIME} INTEGER,
                 ${TimingsContract.Columns.TIMING_DURATION} INTEGER);""".replaceIndent()
        db.execSQL(sqlTiming)/*
        CREATE TRIGGER Remove_task
        AFTER DELETE ON Tasks
        FOR EACH ROW
        BEGIN
        DELETE FROM Timings
        WHERE TaskId = OLD._id;
        END;
         */
        val sqlTrigger = """CREATE TRIGGER Remove_task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMING_TASK_ID} = OLD.${TasksContract.Columns.ID};
            END;""".replaceIndent(" ")
        db.execSQL(sqlTrigger)
    }

    private fun addCurrentTimingView(db: SQLiteDatabase) {/*
        CREATE VIEW vwCurrentTiming
        AS SELECT Timings.TaskId,
        Timings._id,
        Timings.StartTime,
        Tasks.Name
        FROM Timings
        JOIN Tasks
        ON Timings.TaskId = Tasks._id
        WHERE Timings.Duration = 0
        ORDER BY Timings.StartTime DESC
        /* vwCurrentTiming(TaskId,_id,StartTime,Name) */;
         */
        val sSQLTimingView = """CREATE VIEW ${CurrentTimingsContract.TABLE_NAME}
            AS SELECT ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
            FROM ${TimingsContract.TABLE_NAME}
            JOIN ${TasksContract.TABLE_NAME}
            ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
            WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} = 0
            ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME} DESC;
        """.replaceIndent("")
        Log.d(TAG, "addCurrentTimingView: $sSQLTimingView")
        db.execSQL(sSQLTimingView)
    }

    private fun addDurationView(db: SQLiteDatabase) {/*
         * CREATE VIEW vwTaskDuration AS SELECT
         * Tasks.Name, Tasks.Description, Timings.StartTime,
         * DATE(Timings.StartTime, 'unixepoch','localtime') AS StartDate,
         * SUM(Timings.Duration) AS Duration
         * FROM Tasks INNER JOIN Timings ON Tasks._id = Timings.TaskId
         * GROUP BY Tasks._id, StartDate
        /* vwTaskDuration(Name,Description,StartTime,StartDate,Duration) */;
         */
        val sSQL = """
            CREATE VIEW ${DurationContract.TABLE_NAME} AS SELECT
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_DESCRIPTION},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            DATE(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            'unixepoch','localtime') AS ${DurationContract.Columns.START_DATE},
            SUM(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION}) 
            AS ${DurationContract.Columns.DURATION} 
            FROM ${TasksContract.TABLE_NAME} INNER JOIN ${TimingsContract.TABLE_NAME}
            ON  ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID} =  
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} 
            GROUP BY ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}, 
            ${DurationContract.Columns.START_DATE};
        """.trimIndent().replaceIndent("")
        Log.d(TAG, "addDurationView: $sSQL")
        db.execSQL(sSQL)
    }

    private fun parameterView(db: SQLiteDatabase) {
        var sSQL = """CREATE TABLE ${ParametersContract.TABLE_NAME}
            (${ParametersContract.Column.ID} INTEGER PRIMARY KEY NOT NULL,
            ${ParametersContract.Column.VALUE} INTEGER NOT NULL);
        """.trimIndent().replaceIndent(" ")
        Log.d(TAG, "parameterView: sql : $sSQL")
        db.execSQL(sSQL)

        sSQL = "DROP VIEW IF EXISTS ${DurationContract.TABLE_NAME}"
        Log.d(TAG, "parameterView: $sSQL")
        db.execSQL(sSQL)
        /*
        CREATE VIEW vwTaskDurations AS SELECT Tasks.Name, Tasks.Description, Timings.StartTime,
        DATE(Timings.StartTime, 'unixepoch', 'localtime') AS StartDate,
        SUM(Timings.Duration) AS Duration
        FROM Tasks INNER JOIN Timings
        ON Tasks._id = Timings.TaskId
        WHERE Timings.Duration > (SELECT Parameters.Value FROM Parameters WHERE Parameters._id = 1)
        GROUP BY Tasks._id,
        StartDate;*/
        sSQL = """CREATE VIEW ${DurationContract.TABLE_NAME}
            AS SELECT ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_DESCRIPTION},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            DATE(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME}, 'unixepoch', 'localtime')
            AS ${DurationContract.Columns.START_DATE},
            SUM(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION})
            AS ${DurationContract.Columns.DURATION}
            FROM ${TasksContract.TABLE_NAME} INNER JOIN ${TimingsContract.TABLE_NAME}
            ON ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID} = 
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID}
            WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} >
            (SELECT ${ParametersContract.TABLE_NAME}.${ParametersContract.Column.VALUE}
            FROM ${ParametersContract.TABLE_NAME}
            WHERE ${ParametersContract.TABLE_NAME}.${ParametersContract.Column.ID} = ${ParametersContract.ID_SHORT_TIMING})
            GROUP BY ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID},
            ${DurationContract.Columns.START_DATE};
        """.trimMargin().replaceIndent(" ")
        Log.d(TAG, "parameterView: $sSQL")
        db.execSQL(sSQL)

        sSQL =
            """INSERT INTO ${ParametersContract.TABLE_NAME} VALUES(${ParametersContract.ID_SHORT_TIMING}, 0);""".replaceIndent(
                    " "
                )
        Log.d(TAG, "parameterView: $sSQL")
        db.execSQL(sSQL)

    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}