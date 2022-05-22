package com.example.logingoogle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Demonstrate Firebase Authentication using a Google and Facebook
 */
class LoginScreen : Activity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    // facebook
    private val callbackManager = CallbackManager.Factory.create()

    //google
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var signin: CardView //button google
    private lateinit var signinFace: CardView //button face

    private lateinit var signinBtn: Button //this button dont work
    private lateinit var progressBar: ProgressBar
    private lateinit var contrainstLay: ConstraintLayout

    private var provider: Provider = Provider.NONE // which provider is used

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_screen)
        contrainstLay = findViewById(R.id.coordinatorLayout)
        progressBar = findViewById(R.id.loading)

        signin = findViewById(R.id.Signin) // singin whit google
        signin.setOnClickListener {

            // [START config_signin]
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_clientId))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            // [END config_signin]

            // block user interface
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            progressBar.visibility = View.VISIBLE
            //signin.isEnabled = false
            //Thread { signIn() }.start() //
            signIn()
        }

        signinFace = findViewById(R.id.SigninFacebook)
        signinFace.setOnClickListener {
            // block user interface
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            progressBar.visibility = View.VISIBLE
            // [START initialize_fblogin]
            // Initialize Facebook Login button
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        result?.let {
                            val token = it.accessToken
                            val credential = FacebookAuthProvider.getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener { task ->

                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        provider = Provider.FACEBOOK
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        /*Toast.makeText(baseContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show()
                                         */
                                        showLoginError(getString( R.string.face_login_error))
                                        updateUI(null)
                                    }
                                }
                        }
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException?) {
                        Log.w(TAG, error?.message.toString())
                        showLoginError(getString( R.string.face_login_error))
                    }

                })
        }

        // without provider don't work
        signinBtn = findViewById(R.id.signinBtn)
        //signinBtn.isEnabled = true // for test, comment this line for disable this button
        signinBtn.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

    }


    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                showLoginError(getString(R.string.google_login_error))
                updateUI(null)
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    provider = Provider.GOOGLE
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    var loginFailed = ""
                    if (task.exception.toString().contains("forbidden", true)) {
                        loginFailed = "Error al loguearse. Debe usar una vpn" // for country blocked by US
                    } else
                        loginFailed = getString(R.string.login_error)

                    showLoginError(loginFailed)
                    updateUI(null)
                }
            }
    }

    private fun showLoginError(loginFailed: String) {
        // error al loguearse
        val snackBar: Snackbar = Snackbar.make(
            contrainstLay,
            loginFailed, Snackbar.LENGTH_LONG
        )
        snackBar.show()
    }
    // [END auth_with_google]

    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }
    // [END signin]

    private fun updateUI(user: FirebaseUser?) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            //signin.isEnabled = true
            // unblock GUI
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        if (user != null) {
            //Log.w(TAG, user!!.email.toString())
            //Log.w(TAG, user!!.displayName.toString())
            val intent = Intent(this, ProductActivity::class.java)
            val bundle = Bundle()
            bundle.putString("provider", provider.name)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }


    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val GOOGLE_SIGN_IN = 9001
    }
}