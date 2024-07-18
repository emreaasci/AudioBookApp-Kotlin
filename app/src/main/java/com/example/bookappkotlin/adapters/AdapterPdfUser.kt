package com.example.bookappkotlin.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.filters.FilterPdfUser
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.example.bookappkotlin.databinding.RowPdfUserBinding
import com.example.bookappkotlin.models.ModelPdf

class AdapterPdfUser :RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>,Filterable{

    //context for ctor
    private var context:Context
    //arraylist for hold pdfs
    public var pdfArrayList:ArrayList<ModelPdf>

    public var filterArrayList:ArrayList<ModelPdf>

    private var filter: FilterPdfUser? = null

    private lateinit var binding: RowPdfUserBinding

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterArrayList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //get data , set data ,handle btn
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp
        val author = model.author
        val startPage = model.startPage
        val endPage = model.endPage

        //convert date
        val date = MyApplication.formatTimeStamp(timestamp)

        // set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.authorTv.text = author

        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.processBar, null)

        //handle book details click
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",bookId)
            context.startActivity(intent)

        }

    }

    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        var pdfView = binding.pdfView
        var processBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTV
        var sizeTv = binding.sizeTv
        var authorTv = binding.authorTv

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfUser(filterArrayList,this)
        }

        return filter as FilterPdfUser
    }
}