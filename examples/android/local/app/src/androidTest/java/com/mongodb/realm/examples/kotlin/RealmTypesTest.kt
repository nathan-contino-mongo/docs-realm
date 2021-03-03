package com.mongodb.realm.examples.kotlin

import android.util.Log
import com.mongodb.realm.examples.Expectation
import com.mongodb.realm.examples.RealmTest
import com.mongodb.realm.examples.model.kotlin.BundledRealmModule
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class RealmTypesTest : RealmTest() {
    @Test
    fun testReadOnlyRealm() {
        val expectation = Expectation()
        activity!!.runOnUiThread {

            // :code-block-start: read-only
            val config = RealmConfiguration.Builder()
                .assetFile("bundled.realm")
                .readOnly() // :emphasize:
                .modules(BundledRealmModule())
                .build()
            val realm = Realm.getInstance(config)
            // :code-block-end:
            expectation.fulfill()
        }
        expectation.await()
    }

    @Test
    fun testInMemoryRealm() {
        val expectation = Expectation()
        activity!!.runOnUiThread {

            // :code-block-start: in-memory
            val config = RealmConfiguration.Builder()
                .inMemory() // :emphasize:
                .name("transient.realm")
                .build()
            val realm = Realm.getInstance(config)
            // :code-block-end:
            expectation.fulfill()
        }
        expectation.await()
    }

    @Test
    fun testDynamicRealm() {
        val expectation = Expectation()
        activity!!.runOnUiThread {

            // :code-block-start: dynamic
            val config = RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .name("dynamic.realm")
                // :hide-start:
                .inMemory() // make this example secretly transient so state doesn't save between test runs
                // :hide-end:
                .build()
            val dynamicRealm = DynamicRealm.getInstance(config) // :emphasize

            // all objects in a DynamicRealm are DynamicRealmObjects
            var frog: DynamicRealmObject? = null
            dynamicRealm.executeTransaction { transactionDynamicRealm: DynamicRealm ->
                // add type Frog to the schema with name and age fields
                dynamicRealm.schema
                    .create("Frog")
                    .addField("name", String::class.java)
                    .addField("age", Integer::class.java)
                frog = transactionDynamicRealm.createObject("Frog")
                frog?.set("name", "Wirt Jr.")
                frog?.set("age", 42)
            }

            // access all fields in a DynamicRealm using strings
            val name = frog?.getString("name")
            val age = frog?.getInt("age")

            // because an underlying schema still exists,
            // accessing a field that does not exist throws an exception
            try {
                frog?.getString("doesn't exist")
            } catch (e: IllegalArgumentException) {
                Log.e("EXAMPLE", "That field doesn't exist.")
            }

            // Queries still work normally
            val frogs = dynamicRealm.where("Frog")
                .equalTo("name", "Wirt Jr.")
                .findAll()
            // :code-block-end:
            expectation.fulfill()
        }
        expectation.await()
    }
}