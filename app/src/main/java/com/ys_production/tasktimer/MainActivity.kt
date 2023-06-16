package com.ys_production.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import com.ys_production.tasktimer.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit{
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var mTowPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mTowPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val fragment = supportFragmentManager.findFragmentById(R.id.task_detail_container)
        if (fragment != null) {
            showEditPane()
        } else {
            findViewById<View>(R.id.task_detail_container).visibility =
                if (mTowPane) View.INVISIBLE else View.GONE
            findViewById<View>(R.id.mainFragment).visibility = View.VISIBLE
        }
        val appDatabase = AppDatabase.getInstance(this)
        val db = appDatabase.readableDatabase

    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_detail_container)
        if (fragment == null || mTowPane) super.onBackPressed()
        else {
            goBack(fragment)
        }
    }

    private fun goBack(fragment: Fragment) {
        removeEditPane(fragment)
        findViewById<View>(R.id.task_detail_container).visibility = View.GONE
        findViewById<View>(R.id.mainFragment).visibility = View.VISIBLE
    }

    private fun showEditPane() {
        findViewById<View>(R.id.task_detail_container).visibility = View.VISIBLE
        findViewById<View>(R.id.mainFragment).visibility = if (mTowPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane: start")
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        findViewById<View>(R.id.task_detail_container).visibility =
            if (mTowPane) View.INVISIBLE else View.GONE
        findViewById<View>(R.id.mainFragment).visibility = View.VISIBLE
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: start")
        val fragment = supportFragmentManager.findFragmentById(R.id.task_detail_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_task -> taskEditRequest(null)
            R.id.action_settings -> true
            android.R.id.home -> {
                supportFragmentManager.findFragmentById(R.id.task_detail_container)
                    ?.let { goBack(it) }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: start")
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction().replace(R.id.task_detail_container, newFragment)
            .commit()
        showEditPane()
        Log.d(TAG, "taskEditRequest: end")
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

}

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }

//        dbr.execSQL("INSERT INTO ${TasksContract.TABLE_NAME}(${TasksContract.Columns.TASK_NAME}," +
//                "${TasksContract.Columns.TASK_DESCRIPTION}) VALUES('work${(100..1000).random()}','dec${(100..1000).random()}');")
//        testInsertData(dbr)
//        testUpdateSingleData()
//        testUpdateBulkData()
//        testDeleteSingleData()
//        testDeleteBulkData()

//        binding.fab.setOnClickListener { view ->
//            val cursor = db.rawQuery("SELECT * FROM ${TasksContract.TABLE_NAME};",null)
//            cursor.use {
//                Log.d(TAG, "onCreate: cursor start")
//                while (it.moveToNext()){
//                    val id = it.getInt(0)
//                    val name = it.getString(1)
//                    val des = it.getString(2)
//                    val short = it.getInt(3)
//                    Log.d(TAG, "onCreate: Id: $id, name: $name, des: $des, shortOrder: $short")
//                }
//            }
//            cursor.close()
////            appDatabase.close()
//            Snackbar.make(view, "Executed", Snackbar.LENGTH_LONG).show()
//        }

//    private fun testDeleteBulkData(){
//        val where = "${TasksContract.Columns.TASK_SHORT_ORDER} = ?"
//        val selectionArg = arrayOf("999")
//        val item = contentResolver.delete(TasksContract.CONTENT_URI,where,selectionArg)
//        Log.d(TAG, "testDeleteBulkData: item: $item")
//    }
//    private fun testDeleteSingleData(){
//        val item = contentResolver.delete(TasksContract.getUriFromId(5),null,null)
//        Log.d(TAG, "testDeleteSingleData: item: $item")
//    }
//    private fun testUpdateBulkData(){
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "NEW UPDATE BULK name 999")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "NEW des 999")
//            put(TasksContract.Columns.TASK_SHORT_ORDER, 999)
//        }
//        val where = "${ TasksContract.Columns.TASK_NAME } = ?"
//        val selectionArg = arrayOf("work1")
//        val item = contentResolver.update(TasksContract.CONTENT_URI,values,where,selectionArg)
//        Log.d(TAG, "testUpdateBulkData: item: $item")
//    }
//    private fun testUpdateSingleData(){
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "NEW UPDATE SINGLE name ${(100..1000).random()}")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "NEW des ${(100..1000).random()}")
//            put(TasksContract.Columns.TASK_SHORT_ORDER, (100..1000).random())
//        }
//        val item = contentResolver.update(TasksContract.getUriFromId(5),values,null,null)
//        Log.d(TAG, "testUpdateSingleData: item: $item")
//    }
//    private fun testInsertData(db: SQLiteDatabase){
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "test task ${(100..1000).random()}")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "this is test des ${(100..1000).random()}")
//            put(TasksContract.Columns.TASK_SHORT_ORDER, (100..1000).random())
//        }
//        val uri = contentResolver.insert(TasksContract.CONTENT_URI,values)
//        Log.d(TAG, "testInsertData: uri: $uri")
//    }
//    fun makeCursor(db: SQLiteDatabase): Cursor? {
////        return db.rawQuery("SELECT * FROM ${TasksContract.TABLE_NAME};",null)
//        return contentResolver.query(TasksContract.CONTENT_URI,null,null,null,TasksContract.Columns.TASK_SHORT_ORDER)
////        val projection = arrayOf(TasksContract.Columns.ID,TasksContract.Columns.TASK_NAME)
////        val sortOrder = TasksContract.Columns.TASK_NAME
////        return contentResolver.query(TasksContract.getUriFromId(2)
////        ,projection
////        ,null
////        ,null
////        ,sortOrder)
//    }
//    fun GotData(cursor: Cursor){
//        cursor.use {
//                Log.d(TAG, "onCreate: cursor start")
//                while (it.moveToNext()){
//                    printDaTa(it)
//                }
//            }
//            cursor.close()
//    }
//    fun printDaTa(it: Cursor){
//        val id = it.getInt(0)
//        val name = it.getString(1)
//        val des = it.getString(2)
//        val short = it.getInt(3)
//        Log.d(TAG, "onCreate: Id: $id, name: $name, des: $des, shortOrder: $short"
//        )
//    }