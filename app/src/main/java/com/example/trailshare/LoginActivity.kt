package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {


    // Launcher to handle the sign-in result using FirebaseUI
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)  // Call our custom function to handle login result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if the user is already logged in, otherwise start login process
        checkIfLoggedInOrStartLogin()
    }

    // Checks if a user is already logged in
    private fun checkIfLoggedInOrStartLogin() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Already logged in → go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            launchSignInFlow()  // If not logged in → start the sign-in flow
        }
    }

    // Starts the FirebaseUI sign-in process
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create the sign-in screen using FirebaseUI with our providers
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        // Launch the sign-in screen
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Login success
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Login failed or cancelled → try again (no toast, no finish)
            launchSignInFlow()
        }
    }
}
