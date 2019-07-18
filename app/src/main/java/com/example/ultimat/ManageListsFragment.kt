package com.example.ultimat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ultimat.api.Guid
import com.example.ultimat.api.ShoppingListListAPI
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Fragment for managing shopping lists
class ManageListsFragment : Fragment() {

    private lateinit var listAPI: ShoppingListListAPI
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAPI = listener?.listAPI!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_manage_lists, container, false);


        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_container)
        val mFab = view.findViewById<FloatingActionButton>(R.id.fab)


        // Recycler view
        viewAdapter = ManageShoppingListsAdapter(
            listAPI.shoppingLists,
            listAPI,
            layoutInflater,
            listener as Context
        ).apply {
            onItemClick = { shoppingList ->
                listener?.showList(shoppingList.id)
            }
        }
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(listener as Context)
            adapter = viewAdapter
        }


        // Swipe refresh
        swipeLayout?.setOnRefreshListener {
            listAPI.updateListofLists { swipeLayout.isRefreshing = false }
        }


        // FAB
        mFab?.setOnClickListener {
            dialogForName(
                getString(R.string.new_list),
                listener as Context,
                layoutInflater
            ) { newName -> listAPI.addList(newName) }
        }

        onDataUpdate(view)
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun onDataUpdate() {
        view?.let { onDataUpdate(it) }
    }
    fun onDataUpdate(view : View) {
        val emptyText = view.findViewById<TextView>(R.id.empty)
        val list = view.findViewById<RecyclerView>(R.id.list)

        if (listAPI.shoppingLists.isEmpty()) {
            emptyText?.visibility = View.VISIBLE
            list?.visibility = View.GONE
        } else {
            emptyText?.visibility = View.GONE
            list?.visibility = View.VISIBLE
        }

        viewAdapter.notifyDataSetChanged()
    }


    interface OnFragmentInteractionListener {
        fun showList(id : Guid)

        val listAPI: ShoppingListListAPI
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ManageListsFragment()
    }


    class ManageShoppingListsAdapter(
        private val shoppingLists: MutableList<ShoppingListListAPI.ShoppingList>,
        private val shoppingListListAPI: ShoppingListListAPI?,
        private val layoutInflater: LayoutInflater,
        private val context: Context
    ) : RecyclerView.Adapter<ManageShoppingListsAdapter.MyViewHolder>() {
        var onItemClick: ((ShoppingListListAPI.ShoppingList) -> Unit)? = null

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var nameText: TextView = itemView.findViewById(R.id.name_text)
            var idText: TextView = itemView.findViewById(R.id.id_text)
            var moreMenuButton: ImageView = itemView.findViewById(R.id.more_button)

            init {
                itemView.setOnClickListener { onItemClick?.invoke(shoppingLists[adapterPosition]) }
            }
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val textView = layoutInflater.inflate(R.layout.item_shopping_list, parent, false)
            return MyViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            holder.nameText.text = shoppingLists[position].name
            holder.idText.text = shoppingLists[position].id

            holder.moreMenuButton.setOnClickListener {
                val popupMenu = PopupMenu(holder.moreMenuButton.context, it)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.more_menu_change_name -> {
                            dialogForName(
                                context.getString(R.string.new_name),
                                context,
                                layoutInflater
                            ) { newName -> shoppingListListAPI?.changeNameOfList(shoppingLists[position].id, newName) }
                            true
                        }
                        R.id.more_menu_delete -> {
                            shoppingListListAPI?.deleteList(shoppingLists[position].id)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.inflate(R.menu.menu_manage_list_context)
                popupMenu.show()
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = shoppingLists.size
    }
}