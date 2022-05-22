package com.example.logingoogle.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.logingoogle.CompraActivity
import com.example.logingoogle.ProductActivity
import com.example.logingoogle.R
import com.example.logingoogle.databinding.FragmentHomeBinding
import com.squareup.picasso.Picasso

class ProductFragment : Fragment() {

    private val picasso = Picasso.get()

    // some testing pictures
    val data = listOf(
        "https://i.imgur.com/DvpvklR.png",
        "https://somehost/nosirve.png", // no se encuentra  aproposito para ver error icon y loading
        "https://www.w3schools.com/images/w3schools_green.jpg",
        "https://www.w3schools.com/images/picture.jpg",
        "https://www.w3schools.com/images/sun.gif",
        "https://www.w3schools.com/images/venglobe.gif"
    )
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
*/
        // if the user press back on this fragment intercept this event for confirm to exit
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as ProductActivity).handleLogout()
                }
            })
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val gridView: GridView = binding.grid
        //val textView: TextView = binding.textGallery
        /*galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/

        val custom_list_data = ArrayList<CList>()

        val custom_adapter = CListAdapter(container!!.context, custom_list_data)

        for (i in 0..49)
            custom_list_data.add(
                CList(
                    data[(Math.random() * data.size).toInt()],
                    "Product: $i",
                    "fake description blaaaaaaaaaaaaa aaaaaaa aaaaaaa aaaaaaa....."
                )
            )

        gridView.adapter = custom_adapter
        gridView.setOnItemClickListener { parent, view, pos, id ->
            //Toast.makeText(container!!.context, "item clicked: $id", Toast.LENGTH_SHORT).show()
            //pass information to compra activity
            val intent = Intent(container.context, CompraActivity::class.java)
            val bundle = Bundle()
            bundle.putString("title", custom_list_data[pos].mCListTxt)
            bundle.putString("descripcion", custom_list_data[pos].mCListDesc)
            bundle.putString("thumbail", custom_list_data[pos].mClistImg)
            intent.putExtras(bundle)
            startActivity(intent)

        }

        return root
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }


    // ---------------------------- Classes for adapter ----------------------------
    private inner class CListAdapter(
        private val getContext: Context,
        private val customListItem: ArrayList<CList>
    ) :
        ArrayAdapter<CList>(getContext, 0, customListItem) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listLayout = convertView
            val holder: ViewHolder
            if (listLayout == null) {
                val inflateList = (getContext as Activity).layoutInflater
                listLayout = inflateList.inflate(R.layout.custom_list, parent, false)
                holder = ViewHolder()
                holder.mTxt = listLayout!!.findViewById(R.id.titleItem)
                holder.mImg = listLayout.findViewById(R.id.imgItem)
                listLayout.tag = holder
            } else {
                holder = listLayout.tag as ViewHolder
            }

            val listItem = customListItem[position]
            holder.mTxt!!.text = listItem.mCListTxt
            //holder.mImg!!.setImageResource(listItem.mClistImg)
            //load via picasso
            picasso.load(listItem.mClistImg).fit().centerCrop()
                .placeholder(R.drawable.ic_loading) // just for example
                .error(R.drawable.ic_error_icon)
                .into(holder.mImg)
            return listLayout
        }

    }

    private class ViewHolder {
        var mTxt: TextView? = null
        var mImg: ImageView? = null
    }

    private class CList(var mClistImg: String, var mCListTxt: String, var mCListDesc: String)
}