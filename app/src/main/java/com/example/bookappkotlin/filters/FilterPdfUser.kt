package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterPdfUser
import com.example.bookappkotlin.models.ModelPdf

class FilterPdfUser:Filter {

    //arrayList for which want book
    private var filterArrayList: ArrayList<ModelPdf>

    private var adapterPdfUser: AdapterPdfUser

    constructor(filterArrayList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterArrayList = filterArrayList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()){

            //uppercase and lowercase sens.
            constraint = constraint.toString().uppercase()

            val filteredModels:ArrayList<ModelPdf> = ArrayList()

            for(i in 0 until filterArrayList.size){
                //if match
                if(filterArrayList[i].title.uppercase().contains(constraint)){
                    //add to list
                    filteredModels.add(filterArrayList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels

        }

        else{
            //doesnt match
            results.count = filterArrayList.size
            results.values = filterArrayList

        }

        return results
    }

    override fun publishResults(constrainst: CharSequence?, results: FilterResults) {
        //apply filter
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changed

        adapterPdfUser.notifyDataSetChanged()

    }
}