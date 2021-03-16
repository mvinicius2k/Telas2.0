package br.ufc.myapplication

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    suspend fun insert(item: Item){

        coroutineScope {
            val id = referece.push().key

            if(id == null){
                Log.d(TAG,"id nulo")
                launch {
                    throw IOException("Erro de conexão")
                }

            }

            referece.child("Items-$id").setValue(item.getItemModel())
        }.await()




    }

    suspend fun update(item: Item){
        coroutineScope {
            referece.child(item.id!!).setValue(item.getItemModel()).addOnFailureListener {
                throw IOException("Erro de conexão")
            }.await()
        }

    }


    suspend fun delete(item: Item){

        delete(item.id!!)
    }

    suspend fun delete(id: String){
        referece.child(id).removeValue().addOnFailureListener {
            throw IOException("Falha de conexão")
        }.await()
    }

    @Throws(IOException::class)
    suspend fun getItems(): ArrayList<Item>{
        val list = ArrayList<Item>()
        coroutineScope {
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
                launch {
                    throw  IOException("Sem conexão com a internet, tente novamente")
                }

            }.addOnCompleteListener {

            }.await()
        }







        return list






    }
}