package com.mongodb.realm.examples.kotlin
// :code-block-start: complete

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.realm.*
import io.realm.annotations.PrimaryKey

import io.realm.annotations.Required
import io.realm.kotlin.where
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask


class MainActivity : AppCompatActivity() {
    lateinit var uiThreadRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // :code-block-start: initialize-realm-local
        Realm.init(this) // context, usually an Activity or Application
        // :code-block-end:

        val realmName: String = "My Project"
        val config = RealmConfiguration.Builder()
            .name(realmName)
            .build()

        uiThreadRealm = Realm.getInstance(config)

        addChangeListenerToRealm(uiThreadRealm)

        val task : FutureTask<String> = FutureTask(BackgroundQuickStart(), "test")
        val executorService: ExecutorService = Executors.newFixedThreadPool(2)
        executorService.execute(task)

        // :hide-start:
        while(!task.isDone) {
            // wait for task completion
        }
        Log.v("QUICKSTART", "Result: ${task.get()}")

        finish() // destroy activity when background task completes
        // :hide-end:
    }

    fun addChangeListenerToRealm(realm : Realm) {
        // :code-block-start: watch-for-changes-local
        // all tasks in the realm
        val tasks : RealmResults<Task> = realm.where<Task>().findAllAsync()

        tasks.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Task>> { collection, changeSet ->
            // process deletions in reverse order if maintaining parallel data structures so indices don't change as you iterate
            val deletions = changeSet.deletionRanges
            for (i in deletions.indices.reversed()) {
                val range = deletions[i]
                Log.v("QUICKSTART", "Deleted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }

            val insertions = changeSet.insertionRanges
            for (range in insertions) {
                Log.v("QUICKSTART", "Inserted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }

            val modifications = changeSet.changeRanges
            for (range in modifications) {
                Log.v("QUICKSTART", "Updated range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
            }
        })
        // :code-block-end:
    }

    override fun onDestroy() {
        super.onDestroy()
        // the ui thread realm uses asynchronous transactions, so we can only safely close the realm
        // when the activity ends and we can safely assume that those transactions have completed
        uiThreadRealm.close()
    }

    class BackgroundQuickStart : Runnable {

        override fun run() {
            // :code-block-start: open-a-realm-local
            val realmName: String = "My Project"
            val config = RealmConfiguration.Builder().name(realmName).build()

            val backgroundThreadRealm : Realm = Realm.getInstance(config)
            // :code-block-end:

            // :code-block-start: create-object-local
            val task : Task = Task()
            task.name = "New Task"
            backgroundThreadRealm.executeTransaction { transactionRealm ->
                transactionRealm.insert(task)
            }
            // :code-block-end:

            // :code-block-start: read-object-local
            // all tasks in the realm
            val tasks : RealmResults<Task> = backgroundThreadRealm.where<Task>().findAll()
            // :code-block-end:

            // :code-block-start: filter-collection-local
            // you can also filter a collection
            val tasksThatBeginWithN : List<Task> = tasks.where().beginsWith("name", "N").findAll()
            val openTasks : List<Task> = tasks.where().equalTo("status", TaskStatus.Open.name).findAll()
            // :code-block-end:

            // :code-block-start: update-object-local
            val otherTask: Task = tasks[0]!!

            // all modifications to a realm must happen inside of a write block
            backgroundThreadRealm.executeTransaction { transactionRealm ->
                val innerOtherTask : Task = transactionRealm.where<Task>().equalTo("name", otherTask.name).findFirst()!!
                innerOtherTask.status = TaskStatus.Complete.name
            }
            // :code-block-end:

            // :code-block-start: delete-object-local
            val yetAnotherTask: Task = tasks.get(0)!!
            val yetAnotherTaskName: String = yetAnotherTask.name
            // all modifications to a realm must happen inside of a write block
            backgroundThreadRealm.executeTransaction { transactionRealm ->
                val innerYetAnotherTask : Task = transactionRealm.where<Task>().equalTo("name", yetAnotherTaskName).findFirst()!!
                innerYetAnotherTask.deleteFromRealm()
            }
            // :code-block-end:

            // because this background thread uses synchronous realm transactions, at this point all
            // transactions have completed and we can safely close the realm
            backgroundThreadRealm.close()
        }

    }
}

// :code-block-start: define-object-model-local

enum class TaskStatus(val displayName: String) {
    Open("Open"),
    InProgress("In Progress"),
    Complete("Complete"),
}

open class Task() : RealmObject() {
    @PrimaryKey
    var name: String = "task"

    @Required
    var status: String = TaskStatus.Open.name
    var statusEnum: TaskStatus
        get() {
            // because status is actually a String and another client could assign an invalid value,
            // default the status to "Open" if the status is unreadable
            return try {
                TaskStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                TaskStatus.Open
            }
        }
        set(value) { status = value.name }
}

// :code-block-end:
// :code-block-end: