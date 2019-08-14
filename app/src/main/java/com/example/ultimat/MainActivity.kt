package com.example.ultimat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.ultimat.api.Guid
import com.example.ultimat.api.ShoppingListItemAPI
import com.example.ultimat.api.ShoppingListListAPI

// Each of the fragments has its own interface this class needs to implement to facilitate communication, so there are a lot of interfaces here
class MainActivity : AppCompatActivity(),
    AboutFragment.OnFragmentInteractionListener,
    ManageListsFragment.OnFragmentInteractionListener,
    ViewShoppingListFragment.OnFragmentInteractionListener,
    LetsGetStartedFragment.OnFragmentInteractionListener {
    private lateinit var mDrawer: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var nvDrawer: NavigationView

    // Menu item ids can only be ints, so this map stores the convertion to the Guid of the list the menu item refers to
    private val menuIdToGuidMap: MutableMap<Int, Guid> = HashMap()
    override var listAPI: ShoppingListListAPI =
        ShoppingListListAPI(
            context = this,
            listUpdateCallback = {
                // Update the menu by removing the old items and adding new ones
                val menu: Menu = findViewById<NavigationView>(R.id.nav_view).menu
                menuIdToGuidMap.keys.forEach { id -> menu.removeItem(id) }
                menuIdToGuidMap.clear()
                for (list in it) {
                    val id = View.generateViewId()
                    menu.add(R.id.nav_group_shopping_lists, id, 1, list.name)
                        .setIcon(R.drawable.ic_list)
                        .isCheckable = true
                    menuIdToGuidMap[id] = list.id
                }

                // Only show "Home" if no lists exist
                menu.findItem(R.id.nav_home).isVisible = it.isEmpty()


                // If the current fragment is the manage-lists view, tell the fragment to update the list
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_content)
                if (currentFragment is ManageListsFragment) {
                    currentFragment.onDataUpdate()
                }
            })

    override var shoppingListItemAPI: ShoppingListItemAPI =
        ShoppingListItemAPI(
            context = this,
            listUpdateCallback = {
                // If the current fragment is a view of a list, tell the fragment to update it
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_content)
                if (currentFragment is ViewShoppingListFragment) {
                    currentFragment.onDataUpdate()
                }
            })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        toolbar = findViewById(R.id.toolbar)
        mDrawer = findViewById(R.id.drawer_layout)
        nvDrawer = findViewById(R.id.nav_view)


        // Setup toolbar
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar

        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_hamburger_menu)
        }


        // Setup drawer view
        setupDrawerContent(nvDrawer)


        // App startup (check if a server has been configured and that a list exists)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val serverAddress = sharedPreferences.getString("server_address", "none")
        val serverPort = sharedPreferences.getString("server_port", "none")

        // First, if no server is configured, open the settings view. This is just to make it easier when debugging,
        // not any scenario any user should find themself in, whereupon there is little guidance here.
        if (serverAddress == "none" || serverPort == "none") {
            supportFragmentManager.beginTransaction().replace(R.id.fl_content, SettingsFragment.newInstance()).commit()
        } else {

            //If a server is configured (most cases), then download list and set fragment
            listAPI.updateListofLists {
                if (it.isNotEmpty())
                    showList(it.first().id) // Default is the first fragment for now. TODO: make user configurable
                else // Show onboarding guide (lets get started fragment)
                    supportFragmentManager.beginTransaction().replace(
                        R.id.fl_content,
                        LetsGetStartedFragment.newInstance()
                    ).commit()
            }
        }
    }

    // Handle toolbar click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawer.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handle menu clicks
    private fun setupDrawerContent(navigationView: NavigationView) {

        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

    }

    // Menu item click
    fun selectDrawerItem(menuItem: MenuItem) {

        val fragment: Fragment = when (menuItem.itemId) {

            R.id.nav_home ->
                LetsGetStartedFragment.newInstance()

            R.id.nav_lists_manage ->
                ManageListsFragment.newInstance()

            R.id.nav_about ->
                AboutFragment.newInstance()

            R.id.nav_settings ->
                SettingsFragment.newInstance()

            else -> {
                if (menuIdToGuidMap.containsKey(menuItem.itemId)) { // Click on a list
                    ViewShoppingListFragment.newInstance(menuIdToGuidMap[menuItem.itemId]!!)
                } else AboutFragment.newInstance()
            }
        }


        // Insert the fragment by replacing any existing fragment
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit()


        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true

        // Set action bar title
        title = menuItem.title

        // Close the navigation drawer
        mDrawer.closeDrawers()

    }

    override fun showList(id: Guid) {
        shoppingListItemAPI.updateList(id) {
            // Get list from API
            val shoppingList = shoppingListItemAPI.shoppingList

            // Change title
            title = shoppingList.name

            // Highlight in menu
            val menu: Menu = findViewById<NavigationView>(R.id.nav_view).menu
            val menuItemId = menuIdToGuidMap.filterValues { it == id }.keys.first() // This is a lookup by value

            menu.findItem(menuItemId).isChecked = true
        }


        // Change fragment
        supportFragmentManager.beginTransaction().replace(R.id.fl_content, ViewShoppingListFragment.newInstance(id))
            .commit()

    }
}