package com.example.bookappkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookappkotlin.MyApplication
import com.example.bookappkotlin.R
import com.example.bookappkotlin.databinding.RowCommentBinding
import com.example.bookappkotlin.databinding.RowFavBooksBinding
import com.example.bookappkotlin.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment>{

    val context: Context

    val commetList:ArrayList<ModelComment>

    private lateinit var binding: RowCommentBinding

    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commnetList: ArrayList<ModelComment>) {
        this.context = context
        this.commetList = commnetList

        firebaseAuth = FirebaseAuth.getInstance()
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderComment(binding.root)
    }

    override fun getItemCount(): Int {
        return commetList.size
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        // get data

        val model = commetList[position]

        val id = model.id
        val bookId = model.bookId
        val uid = model.uid
        val timestamp = model.timestamp
        val comment = model.comment


        //convert date
        val date = MyApplication.formatTimeStamp(timestamp.toLong())


        //set data
        holder.commentTv.text = comment
        holder.dateTv.text = date

        loadUserDetails(model,holder)

        //handle delete commet
        holder.itemView.setOnClickListener {
            //logged in
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid){
                deleteComment(model,holder)
            }
        }

    }

    private fun deleteComment(model: ModelComment, holder: AdapterComment.HolderComment) {
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Commet")
            .setMessage("Are you sure?")
            .setPositiveButton("DELETE"){d,e->

                val bookId = model.bookId
                val commentId = model.id

                //delete comment
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {e->
                        Toast.makeText(context, "Failed delete comment due to ${e.message}", Toast.LENGTH_SHORT).show()

                    }
            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get image and name
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.nameTv.text = name

                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)

                    }
                    catch (e:Exception){
                        //set default image
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    inner class HolderComment(itemView: View) : RecyclerView.ViewHolder(itemView){
        val profileIv = binding.profileIv
        val nameTv = binding.nameTv
        val dateTv = binding.dateTv
        val commentTv = binding.commentTv

    }
}