package my.com.syazli.mychef.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import my.com.syazli.mychef.R
import my.com.syazli.mychef.models.CategoryModel

class CategoryAdapter(private val context: Context, private val list: List<CategoryModel>) : BaseAdapter() {

    override fun getCount(): Int = list.size
    override fun getItem(position: Int): Any = list[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_food_category, parent, false)

        val image = view.findViewById<ImageView>(R.id.imgCategory)
        val title = view.findViewById<TextView>(R.id.tvCategoryName)

        val item = list[position]
        title.text = item.name

        Glide.with(context)
            .load(item.imageRes)
            .centerCrop()
            .into(image)

        return view
    }
}

