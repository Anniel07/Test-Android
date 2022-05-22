package com.example.logingoogle

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.logingoogle.databinding.ActivityProductBinding
import com.facebook.login.LoginManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProductActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProductBinding

    private lateinit var provider: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras
        provider = bundle?.getCharSequence("provider").toString()


        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain2.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_product, R.id.nav_perfil, R.id.nav_misproductos,
                R.id.nav_configuracion
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.menu.findItem(R.id.nav_exit)
            .setOnMenuItemClickListener { item ->
                handleLogout()
                true
            }
        if (Firebase.auth.currentUser != null) {
            // update header information
            val userWelcome: TextView = navView.getHeaderView(0).findViewById(R.id.user_welcome)
            userWelcome.text = "Welcome: " + Firebase.auth.currentUser!!.displayName
            val emailWelcome: TextView = navView.getHeaderView(0).findViewById(R.id.email_welcome)
            emailWelcome.text = Firebase.auth.currentUser!!.email
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.product, menu)
        return true
    }
    */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    // logout with google and return to login page
    private fun logout() {
        if (checkCurrentUser()) {
            if (provider.equals(Provider.FACEBOOK.name))
                LoginManager.getInstance().logOut()
            signOut()
            // redirect to login screen
            val intent = Intent(this, LoginScreen::class.java)
            Toast.makeText(this, "Ha cerrado su sesión con $provider", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

    //  signOut with firebase
    private fun signOut() {
        // [START auth_sign_out]
        Firebase.auth.signOut()
        // [END auth_sign_out]

    }

    private fun checkCurrentUser(): Boolean {
        // [START check_current_user]
        val user = Firebase.auth.currentUser
        return user != null
        // [END check_current_user]
    }

    fun handleLogout() {
        if (checkCurrentUser()) { // make sure the user is login in
            val alertBuilder = AlertDialog.Builder(this@ProductActivity)
            alertBuilder.setTitle("Confirm?")
            alertBuilder.setMessage("Está seguro que desea cerrar sesión con ${provider}?")
            alertBuilder.setCancelable(false)
            alertBuilder.setPositiveButton("YES") { _, _ ->
                logout()
            }
            alertBuilder.setNegativeButton("NO") { _, _ ->
                // do nothing
            }
            alertBuilder.create().show()
        }
    }


}