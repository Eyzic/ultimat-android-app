package com.example.ultimat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.ultimat.api.Guid
import com.example.ultimat.api.ShoppingListListAPI

// This fragment is the onboarding guide when the users start the app without a configured list
class LetsGetStartedFragment : Fragment() {

    private lateinit var shoppingListListAPI: ShoppingListListAPI
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shoppingListListAPI = listener?.listAPI!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lets_get_started, container, false)

        view.findViewById<Button>(R.id.button2).setOnClickListener {
            dialogForName(
                getString(R.string.new_list),
                view.context,
                layoutInflater
            ) { shoppingListListAPI.addList(it) { id -> shoppingListListAPI.updateListofLists {listener?.showList(id) }} }
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

    interface OnFragmentInteractionListener {
        fun showList(id : Guid)

        val listAPI: ShoppingListListAPI
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LetsGetStartedFragment()
    }
}

