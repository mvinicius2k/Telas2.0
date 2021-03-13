package br.ufc.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {



    private val TAG = "MainActivity"

    private lateinit var listItems : ArrayList<Item>
    private lateinit var listItemsSearch : LinkedList<Item>
    private lateinit var rvAdapter : RecyclerView.Adapter<*>
    private lateinit var rvManager : RecyclerView.LayoutManager
    private lateinit var tbNew : ToggleButton
    private lateinit var tbEdit : ToggleButton
    private lateinit var etId : EditText
    private lateinit var txtEmpty : TextView
    private lateinit var spnId : Spinner
    private lateinit var tbRemove: ToggleButton

    private lateinit var fireBaseDB: FirebaseDB


    private lateinit var rvItems : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tbRemove = findViewById(R.id.tb_remove)
        rvItems = findViewById(R.id.rv_items)
        tbNew = findViewById(R.id.tb_new)
        tbEdit = findViewById(R.id.tb_edit)
        etId = findViewById(R.id.et_id)
        txtEmpty = findViewById(R.id.txt_empty)
        fireBaseDB = FirebaseDB(Constants.PATH_DB)


        listItems = ArrayList()
        listItemsSearch = LinkedList()
        rvManager = LinearLayoutManager(this)
        rvAdapter = RecyclerAdapter(listItemsSearch)
        spnId = findViewById(R.id.spn_id)

        ArrayAdapter.createFromResource(this, R.array.spn_id_array, android.R.layout.simple_spinner_item)
                .also { arrayAdapter ->
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spnId.adapter = arrayAdapter
                }


        rvItems.apply {
            setHasFixedSize(false)
            layoutManager = rvManager
            adapter = rvAdapter
        }

        tbNew.setOnCheckedChangeListener { _, isChecked ->

            if(isChecked){
                tbEdit.visibility = View.GONE
                tbRemove.visibility = View.GONE
                spnId.visibility = View.VISIBLE
            } else {


                val intent = Intent(this,AddActivity::class.java)
                intent.putExtra("id", spnId.selectedItemId.toInt())
                startActivityForResult(intent, ActivityKind.AddActivity_New.ordinal)
                tbEdit.visibility = View.VISIBLE
                tbRemove.visibility = View.VISIBLE
                spnId.visibility = View.GONE

            }


        }

        tbEdit.setOnCheckedChangeListener { _, isChecked ->

            if(isChecked){

                tbNew.visibility = View.GONE
                tbRemove.visibility = View.GONE
                etId.visibility = View.VISIBLE


            } else {

                var pos : Int

                try{
                    pos = etId.text.toString().toInt()
                } catch (e : Exception){
                    e.printStackTrace()
                    tbEdit.isChecked = false
                    tbNew.visibility = View.VISIBLE
                    tbRemove.visibility = View.VISIBLE
                    etId.visibility = View.GONE
                    return@setOnCheckedChangeListener

                }


                if(pos >= listItems.size || pos < 0){
                    Toast.makeText(this, "Item " + etId.text.toString() + " não existe", Toast.LENGTH_SHORT).show()
                    tbEdit.isChecked = true
                    return@setOnCheckedChangeListener
                }

                val intent = Intent(this,AddActivity::class.java)



                intent.putExtra("item", listItemsSearch[pos])
                intent.putExtra("line", pos)
                startActivityForResult(intent, ActivityKind.AddActivity_Edit.ordinal)
                tbNew.visibility = View.VISIBLE
                etId.visibility = View.GONE
            }


        }

        tbRemove.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){

                tbNew.visibility = View.GONE
                tbEdit.visibility = View.GONE
                etId.visibility = View.VISIBLE




            } else {
                if(!etId.text.toString().isNullOrBlank()){
                    var removeLine = etId.text.toString().toInt()
                    fireBaseDB.delete(listItems[removeLine])
                    listItems.removeAt(removeLine)
                    listItemsSearch.removeAt(removeLine)
                    rvAdapter.notifyDataSetChanged()
                }



                tbNew.visibility = View.VISIBLE
                tbEdit.visibility = View.VISIBLE
                etId.visibility = View.GONE
            }
        }
        GlobalScope.launch(Dispatchers.Default) {
            fillFromFirebase()
        }
















    }

    private suspend fun fillFromFirebase() = withContext(Dispatchers.IO){




        listItems.addAll( fireBaseDB.getItems())
        listItemsSearch.addAll(listItems)

        if(listItems.size > 0){
            runOnUiThread {
                rvAdapter.notifyDataSetChanged()
                rvItems.visibility = View.VISIBLE
                txtEmpty.visibility = View.GONE
                tbRemove.isEnabled = true
                tbEdit.isEnabled = true
                tbNew.isEnabled = true


            }
        }




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrBlank()){
                    listItemsSearch.clear()
                    listItemsSearch.addAll(listItems)
                    rvAdapter.notifyDataSetChanged()
                    return true
                }

                Log.d(TAG,"Buscando por '$newText'")

                val result = listItemsSearch.filter {
                    it.name.toLowerCase().contains(newText.toLowerCase())
                }

                listItemsSearch.clear()
                listItemsSearch.addAll(result)

                rvAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })

        return true








        return super.onCreateOptionsMenu(menu)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode in intArrayOf(ActivityKind.AddActivity_New.ordinal, ActivityKind.AddActivity_Edit.ordinal)){
            if(resultCode == Activity.RESULT_OK){
                val item = data?.getSerializableExtra("item") as Item
                val line = data?.getIntExtra("line",-1)

                if(requestCode == ActivityKind.AddActivity_New.ordinal){
                    listItems.add(item)
                    listItemsSearch.add(item)

                    fireBaseDB.insert(item)
                } else if(line > -1){ //Item editado
                    listItems[line] = item
                    listItemsSearch[line] = item
                    fireBaseDB.update(item)
                }


                rvItems.visibility = View.VISIBLE
                rvAdapter.notifyDataSetChanged();
                txtEmpty.visibility = View.GONE



            } else if(resultCode == Activity.RESULT_CANCELED)
                return


        }






    }
}