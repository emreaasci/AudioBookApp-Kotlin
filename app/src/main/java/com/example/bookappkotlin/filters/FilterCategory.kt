package com.example.bookappkotlin.filters

import android.widget.Filter
import com.example.bookappkotlin.adapters.AdapterCategory
import com.example.bookappkotlin.models.ModelCategory

class FilterCategory:Filter {

    private val filterList: ArrayList<ModelCategory>
    private val adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //value not empty or null
        if(constraint != null && constraint.isNotEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels:ArrayList<ModelCategory> = ArrayList()

            for(i in 0 until  filterList.size){
                if(filterList[i].category.uppercase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList[i])
                }

            }
            results.count = filteredModels.size
            results.values = filteredModels

        }

        else{
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes

        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //notify changes

        adapterCategory.notifyDataSetChanged()
    }
}