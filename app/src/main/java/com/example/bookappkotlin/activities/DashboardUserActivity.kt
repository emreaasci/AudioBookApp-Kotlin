package com.example.bookappkotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookappkotlin.activities.MainActivity
import com.example.bookappkotlin.UserBookFragment
import com.example.bookappkotlin.databinding.ActivityDashboardUserBinding
import com.example.bookappkotlin.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    //firease auth
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var categoryList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardUserBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.categoriesTl.setupWithViewPager(binding.viewPager)

        //handle log out btn
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //handle profile btn
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }



    }

    private fun setupWithViewPagerAdapter(viewPager:ViewPager){
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this)

        categoryList = ArrayList()

        //categories load from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryList.clear()

                //add static categories
                val modelAll = ModelCategory("01","All",1,"")
                val modelViewed = ModelCategory("01","Most Viewed",1,"")
                val modelDownloaded = ModelCategory("01","Most Downloaded",1,"")

                //added to list
                categoryList.add(modelAll)
                categoryList.add(modelViewed)
                categoryList.add(modelDownloaded)
                viewPagerAdapter.addFragment(
                    UserBookFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ),modelAll.category)
                viewPagerAdapter.addFragment(
                    UserBookFragment.newInstance(
                        "${modelViewed.id}",
                        "${modelViewed.category}",
                        "${modelViewed.uid}"
                    ),modelViewed.category)
                viewPagerAdapter.addFragment(
                    UserBookFragment.newInstance(
                        "${modelDownloaded.id}",
                        "${modelDownloaded.category}",
                        "${modelDownloaded.uid}"
                    ),modelDownloaded.category)

                //refresh
                viewPagerAdapter.notifyDataSetChanged()

                //load from db
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryList.add(model!!)

                    viewPagerAdapter.addFragment(
                        UserBookFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ),model.category)

                    //refresh
                    viewPagerAdapter.notifyDataSetChanged()
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm:FragmentManager, behavior:Int, context: Context): FragmentPagerAdapter(fm,behavior){
        //hold to fragments
        private val fragmentList: ArrayList<UserBookFragment> = ArrayList()
        //list for categories title
        private val fragmentTitleList:ArrayList<String> = ArrayList()

        private val context:Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: UserBookFragment, title:String){
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if(firebaseUser == null){
            //
            binding.subTitleTv.text = "Not logged in"

            //hide profile btn cause not logged in
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }

        else{
            //logging in show user info
            val email = firebaseUser.email
            binding.subTitleTv.text = email

            //show profile btn cause not logged in
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE

        }
    }
}