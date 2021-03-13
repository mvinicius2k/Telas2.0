package br.ufc.myapplication

import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.math.log

class FirebaseDB {

    @Transient
    private val TAG = "FirebaseDB"

    private var referece: DatabaseReference
    private var database: FirebaseDatabase

    constructor(path: String){
        database = FirebaseDatabase.getInstance()
        referece = database.getReference(path)


    }

    @Throws(IOException::class)
    fun insert(item: Item){



        val id = referece.push().key

        if(id == null){
            Log.d(TAG,"id nulo")
            throw IOException()
        }

        referece.child("Items-$id").setValue(item.getItemModel())
    }

    fun update(item: Item){
        referece.child(item.id!!).setValue(item.getItemModel())
    }


    fun delete(item: Item){
        delete(item.id!!)
    }

    fun delete(id: String){
        referece.child(id).removeValue()
    }

    @Throws(IOException::class)
    suspend fun getItems(): ArrayList<Item>{

        val list = ArrayList<Item>()

        val query = referece.get().addOnSuccessListener { dataSnapshot ->




            val item = dataSnapshot.value as HashMap<*,*>

            var id = ""

            item.keys.forEach {
                val values = item[it] as HashMap<*,*>
                Log.d(TAG, it.toString())
                id = it.toString()

                list.add(Item.fromFirebase(id, values))


            }





        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
            throw  IOException()
        }.addOnCompleteListener {

        }.await()


         list.forEach {
             //Log.i(TAG, "lista:\n$it")
         }

        return list






    }
}