package com.example.bookappkotlin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookappkotlin.adapters.AdapterPdfUser
import com.example.bookappkotlin.databinding.FragmentUserBookBinding
import com.example.bookappkotlin.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class UserBookFragment : Fragment {
    private lateinit var binding: FragmentUserBookBinding

    public companion object{
        private const val TAG = "USER_BOOK_TAG"

        //receive data
        public fun newInstance(categoryId:String, category:String, uid:String) : UserBookFragment{
            val fragment = UserBookFragment()
            //put data
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid  = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments

        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserBookBinding.inflate(LayoutInflater.from(context),container,false)

        Log.d(TAG, "onCreateView: category:$category")
        if(category == "All"){
            loadAllBook()
        }
        else if(category == "Most Viewed"){
            loadMostDownloadedViewedBooks("viewsCount")

        }
        else if(category == "Most Downloaded"){
            loadMostDownloadedViewedBooks("downloadsCount")

        }
        else{
            //load selected category
            loadCategorizedBook()
        }


        //search
        binding.searchEt.addTextChangedListener( object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e:Exception){
                    Log.d(TAG, "onTextChanged: search exception:${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        } )

        return binding.root
    }

    private fun loadAllBook() {
        pdfArrayList= ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add list
                    pdfArrayList.add(model!!)

                }
                //setup adaper
                adapterPdfUser= AdapterPdfUser(context!!,pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun loadMostDownloadedViewedBooks(orderBy: String) {
        pdfArrayList= ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) //load 10 most viewed or most downloaded
            .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add list
                    pdfArrayList.add(model!!)

                }
                //setup adaper
                adapterPdfUser= AdapterPdfUser(context!!,pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBook() {
        pdfArrayList= ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add list
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser= AdapterPdfUser(context!!,pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}