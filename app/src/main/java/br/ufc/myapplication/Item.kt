package br.ufc.myapplication

import android.annotation.SuppressLint
import android.util.Log
import java.io.Serializable
import com.google.firebase.database.ServerValue

enum class ItemType{
    PLACA_DE_VIDEO, CPU, MEMORIA_RAM, FONTE, DISCO_RIGIDO, GABINETE, OUTRO
}

data class ItemModel(val name: String, val qtd: Int, val price: Float, val type: Int, val dateTime: MutableMap<String, String>)


class Item (var name: String, var qtd:Int, var price: Float, var type: ItemType) : Serializable {


    var id: String? = null

    constructor(item: Item): this(
            item.name,
            item.qtd,
            item.price,
            item.type
        ){

        if(item.id != null)
            this.id = item.id
    }









    @SuppressLint("NewApi")
    fun getItemModel(): ItemModel{
        return ItemModel(name, qtd, price, type.ordinal, ServerValue.TIMESTAMP)
    }

    fun getTypeChar(): Char {
        return type.name[0]
    }



    fun getPriceStr(): String {

        var mark : Char
        if(price.toString().contains('.'))
            mark = '.'
        else
            mark = ','

        val numL = price.toString().split(mark)[0]
        var numR = price.toString().split(mark)[1]
        if(numR.length == 1)
            numR += 0
        else if(numR.length > 1)
            numR = numR.substring(0,2)
        return "$numL,$numR"
    }

    override fun toString(): String {
        var str = ""
        if(id != null)
            str += id
        else
            str += "Item"
        return "$str(name='$name', qtd=$qtd, price=$price, type=$type)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (name != other.name) return false
        if (qtd != other.qtd) return false
        if (price != other.price) return false
        if (type != other.type) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + qtd
        result = 31 * result + price.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }


    companion object{
        private const val TAG = "ItemCompanionObject"
        private var localId = 0L

        fun fromFirebase(id: String, hashMapItem: HashMap<*,*>): Item{
            val item = Item(
                    hashMapItem["name"] as String,
                    (hashMapItem["qtd"] as Long).toInt(),
                    (hashMapItem["price"] as Number).toFloat(),
                    toItemType((hashMapItem["type"] as Long).toInt())
            )

            item.id = id


            return item

        }

        fun toItemType(index : Int) : ItemType{
            return when(index){
                0 -> ItemType.PLACA_DE_VIDEO
                1 -> ItemType.CPU
                2 -> ItemType.MEMORIA_RAM
                3 -> ItemType.FONTE
                4 -> ItemType.DISCO_RIGIDO
                5 -> ItemType.GABINETE
                6 -> ItemType.OUTRO
                else -> {
                    Log.d(TAG,"Index inexistente na conversão, retornando como " + ItemType.OUTRO.toString())
                    ItemType.OUTRO
                }
            }
        }
    }



}