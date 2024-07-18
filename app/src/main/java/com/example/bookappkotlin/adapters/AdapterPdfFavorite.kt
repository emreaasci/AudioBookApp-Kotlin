package com.example.bookappkotlin.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.example.bookappkotlin.databinding.RowFavBooksBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite:RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {
    private val context: Context

    private var bookArrayList:ArrayList<ModelPdf>

    private lateinit var binding:RowFavBooksBinding

    constructor(context: Context, bookArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.bookArrayList = bookArrayList
    }


    inner class HolderPdfFavorite(itemView:View) : RecyclerView.ViewHolder(itemView){

        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeBtn = binding.removeBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTV

        var sizeTv = binding.sizeTv
        var authorTv = binding.authorTv



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = RowFavBooksBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdfFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {

        val model = bookArrayList[position]

        loadBookDetailst(model,holder)

        //handle pdf dateils
        holder.itemView.setOnClickListener{
            val intent = Intent(context,PdfDetailActivity::class.java)
            intent.putExtra("bookId",model.id)
            context.startActivity(intent)
        }

        //handle remove btn
        holder.removeBtn.setOnClickListener {
            MyApplication.removeFromFav(context,model.id)
        }
    }

    private fun loadBookDetailst(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val author = "${snapshot.child("author").value}"

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()
                    model.author = author

                    //convert date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId",holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url","$title",holder.pdfView,holder.progressBar,null)
                    MyApplication.loadPdfSize("$url","$title",holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.authorTv.text = author

                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}