package com.example.bookappkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappkotlin.filters.FilterPdfAdmin
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.activities.PdfDetailActivity
import com.example.bookappkotlin.activities.PdfEditActivity
import com.example.bookappkotlin.databinding.RowPdfAdminBinding
import com.example.bookappkotlin.models.ModelPdf

class AdapterPdfAdmin:RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    private var context:Context
    public var pdfArrayList:ArrayList<ModelPdf>
    private val filterArrayList: ArrayList<ModelPdf>

    private var filter: FilterPdfAdmin? = null

    private lateinit var binding:RowPdfAdminBinding

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterArrayList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //get data, set data

        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val author = model.author
        val voiceUrl = model.voiceUrl

        //convert timestamp aa/mm/yyyy
        val date = MyApplication.formatTimeStamp(timestamp)


        //setdata
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.authorTv.text = author


        //load category
        MyApplication.loadCategory(categoryId, holder.categoryTV)

        //pdf thubnail
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //load edit book, delete book
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model,holder)
        }

        //handle book detail
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId) //will be used to load detail
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title
        val voiceUrl = model.voiceUrl

        val TAG = "deletecevoice"

        //options to show on dialog
        val options = arrayOf("Edit","Delete")

        //alert diolog
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Choose option")
            .setItems(options){dialog,position->
                // handle item click
                if(position == 0){
                    //edit click
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId)
                    context.startActivity(intent)
                }
                else if(position == 1){
                    //delete click
                    Log.d(TAG, "moreOptionsDialog: $voiceUrl")
                    MyApplication.deleteBook(context, bookId, bookUrl,bookTitle,voiceUrl)
                }
            }
            .show()

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfAdmin(filterArrayList,this)
        }

        return filter as FilterPdfAdmin
    }


    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTV = binding.categoryTV
        val sizeTv = binding.sizeTv
        val authorTv = binding.authorTv
        val moreBtn = binding.moreBtn

    }


}