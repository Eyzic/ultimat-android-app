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
import com.example.ultimat.api.ShoppingListItemAPI
import com.google.android.material.floatingactionbutton.FloatingActionButton


// Fragment to manage the items a a shopping list
class ViewShoppingListFragment : Fragment() {
    private lateinit var listItemAPI: ShoppingListItemAPI
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private val LIST_ID = "list_id"

    private var listId: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listId = it.getString(LIST_ID)
        }
        listItemAPI = listener?.shoppingListItemAPI!!

        listItemAPI.updateList(this.listId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_shopping_list, container, false)


        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_container)
        val mFab = view.findViewById<FloatingActionButton>(R.id.fab)


        //Recycler view
        viewAdapter = ViewShoppingListAdapter(
            listId!!,
            listItemAPI.shoppingList,
            listItemAPI,
            layoutInflater,
            listener as Context
        ).apply {
            onItemClick = { shoppingItem ->
                listItemAPI.changeItem(listId!!, shoppingItem.id, shoppingItem.description, !shoppingItem.checked)
            }
        }
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(listener as Context)
            adapter = viewAdapter
        }


        // Swipe refresh
        swipeLayout?.setOnRefreshListener {
            listItemAPI.updateList(this.listId!!) { swipeLayout.isRefreshing = false }
        }


        // FAB
        mFab?.setOnClickListener {
            dialogForName(
                getString(R.string.new_item),
                listener as Context,
                layoutInflater
            ) { newName -> listItemAPI.addItem(this.listId!!, newName) }
        }
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

    fun onDataUpdate() {viewAdapter.notifyDataSetChanged()}


    interface OnFragmentInteractionListener {
        var shoppingListItemAPI : ShoppingListItemAPI
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            ViewShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putString(LIST_ID, param1)
                }
            }
    }

    class ViewShoppingListAdapter(
        private val listId: Guid,
        private val shoppingList: ShoppingListItemAPI.ShoppingList,
        private val shoppingListItemAPI: ShoppingListItemAPI,
        private val layoutInflater: LayoutInflater,
        private val context: Context
    ) : RecyclerView.Adapter<ViewShoppingListAdapter.MyViewHolder>() {
        var onItemClick: ((ShoppingListItemAPI.ShoppingListItem) -> Unit)? = null

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var nameText: TextView = itemView.findViewById(R.id.name_text)
            var idText: TextView = itemView.findViewById(R.id.id_text)
            var moreMenuButton: ImageView = itemView.findViewById(R.id.more_button)
            var checkedStatus: ImageView = itemView.findViewById(R.id.checked_status)

            init {
                itemView.setOnClickListener { onItemClick?.invoke(shoppingList.items[adapterPosition]) }
            }
        }


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemView = layoutInflater.inflate(R.layout.item_shopping_list_item, parent, false)
            return MyViewHolder(itemView)
        }

        // Set up each item
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            holder.nameText.text = shoppingList.items[position].description
            holder.idText.text = shoppingList.items[position].id
            holder.checkedStatus.setImageResource(
                if (shoppingList.items[position].checked)
                    R.drawable.ic_checkbox_checked
                else
                    R.drawable.ic_checkbox_unchecked

            )

            holder.moreMenuButton.setOnClickListener {
                val popupMenu = PopupMenu(holder.moreMenuButton.context, it)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.more_menu_change_name -> {
                            dialogForName(
                                context.getString(R.string.new_name),
                                context,
                                layoutInflater
                            ) { newName ->
                                shoppingListItemAPI.changeItem(
                                    listId,
                                    shoppingList.items[position].id,
                                    newName,
                                    shoppingList.items[position].checked
                                )
                            }
                            true
                        }
                        R.id.more_menu_delete -> {
                            shoppingListItemAPI.deleteItem(listId, shoppingList.items[position].id)
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
        override fun getItemCount() = shoppingList.items.size

    }
}
