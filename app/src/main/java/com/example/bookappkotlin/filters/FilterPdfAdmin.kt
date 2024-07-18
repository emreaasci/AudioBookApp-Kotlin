package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterPdfAdmin
import com.example.bookappkotlin.models.ModelPdf

class FilterPdfAdmin: Filter {
    private var filterArrayList: ArrayList<ModelPdf>

    private var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterArrayList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
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

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        adapterPdfAdmin.notifyDataSetChanged()
    }


}