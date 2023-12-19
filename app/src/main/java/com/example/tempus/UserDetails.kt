package com.example.tempus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style

class UserDetails : AppCompatActivity() {
    private val e = Errors()
    private val m = Messages()
    private val validMessage = Crouton.makeText(this, m.confirmedLogin, Style.INFO)
    private val returns = Crouton.makeText(this, e.newSignInRequired, Style.INFO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@UserDetails, AppSettings::class.java)
                intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        FirebaseApp.initializeApp(this)
        security()
        details()
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)
        val addbtn = findViewById<ImageButton>(R.id.addbtn)


        homebtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        breaksbtn.setOnClickListener {
            val breakspage = Intent(this, Breaks::class.java)
            startActivity(breakspage)
            overridePendingTransition(0, 0)
            finish()
        }

        statsbtn.setOnClickListener {
            val statspage = Intent(this, Statistics::class.java)
            startActivity(statspage)
            overridePendingTransition(0, 0)
            finish()
        }

        settingsbtn.setOnClickListener {
            val settingspage = Intent(this, AppSettings::class.java)
            startActivity(settingspage)
            overridePendingTransition(0, 0)
            finish()
        }

        addbtn.setOnClickListener {
            val tform = Intent(this, CategoryForm::class.java)
            startActivity(tform)
            overridePendingTransition(0, 0)
            finish()

        }


    }

    private fun security() {

        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {

                val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                AppSettings.Preloads.userSName = null
                val intent = Intent(this@UserDetails, Login::class.java)
                intent.putExtra("login", R.layout.login)
                overridePendingTransition(0, 0)
                startActivity(intent)

            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // to do stuff here

            } else {
                val exception = task.exception
                if (exception is FirebaseAuthInvalidUserException) {
                    val errorCode = exception.errorCode
                    if (errorCode == "ERROR_USER_NOT_FOUND") {
                        val sharedPreferences =
                            getSharedPreferences("preferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                        AppSettings.Preloads.userSName = null
                        val intent = Intent(this@UserDetails, Login::class.java)
                        intent.putExtra("login", R.layout.login)
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    private fun details() {
        val username: EditText = findViewById(R.id.userName_text)
        val password: EditText = findViewById(R.id.passwordEditText)
        val conPassword: EditText = findViewById(R.id.confirmPasswordEditText)
        val name: EditText = findViewById(R.id.nameEditText)
        val surname: EditText = findViewById(R.id.surnameEditText)
        val email: EditText = findViewById(R.id.emailEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        username.setText(AppSettings.Preloads.userSName)
        password.setText(AppSettings.Preloads.pass)
        name.setText(AppSettings.Preloads.names)
        surname.setText(AppSettings.Preloads.surname)
        email.setText(AppSettings.Preloads.emails)
        conPassword.setText(AppSettings.Preloads.conPass)





        saveButton.setOnClickListener {


            when {
                password.text.toString() != conPassword.text.toString() -> {
                    val message = " passwords do not match"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                }

                name.text.toString().isEmpty() -> {
                    val message = " no firstname entered "
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                surname.text.toString().isEmpty() -> {
                    val message = " no surname entered "
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                password.text.toString().length < 7 -> {
                    val message = " enter password is too short"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                conPassword.text.toString().length < 7 -> {
                    val message = " confirmkey password is too short "
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                }

                email.text.toString().isEmpty() -> {
                    val message = " no emailaddress entered "
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                else -> { // move to the next screen if filled

                    val database = Firebase.database
                    val userid = FirebaseAuth.getInstance().currentUser?.uid
                    val myRef = database.getReference("UserDetails")

                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let {
                        it.updateEmail((email.text.toString().replace("\\s".toRegex(), "")))
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    myRef.child(userid.toString()).child("displayname").setValue(
                                        username.text.toString().replace("\\s".toRegex(), "")
                                    )
                                    myRef.child(userid.toString()).child("password").setValue(
                                        password.text.toString().replace("\\s".toRegex(), "")
                                    )
                                    myRef.child(userid.toString()).child("confirmkey").setValue(
                                        conPassword.text.toString().replace("\\s".toRegex(), "")
                                    )
                                    myRef.child(userid.toString()).child("firstname")
                                        .setValue(name.text.toString().replace("\\s".toRegex(), ""))
                                    myRef.child(userid.toString()).child("surname")
                                        .setValue(
                                            surname.text.toString().replace("\\s".toRegex(), "")
                                        )
                                    myRef.child(userid.toString()).child("emailaddress")
                                        .setValue(
                                            email.text.toString().replace("\\s".toRegex(), "")
                                        )
                                    emailChange()

                                } else {

                                    // to do stuff soon
                                }
                            }.addOnFailureListener { exception ->

                                when ((exception as? FirebaseAuthException)?.errorCode) {
                                    "ERROR_REQUIRES_RECENT_LOGIN" -> {
                                        authorise()

                                    }

                                    "ERROR_USER_TOKEN_EXPIRED" -> {
                                        returns.show()
                                        FirebaseAuth.getInstance().signOut()


                                        val message =
                                            " ${AppSettings.Preloads.userSName} HAS LOGGED OUT!"
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        val sharedPreferences =
                                            getSharedPreferences(
                                                "preferences",
                                                Context.MODE_PRIVATE
                                            )
                                        sharedPreferences.edit().putBoolean("isFirstLogin", true)
                                            .apply()
                                        AppSettings.Preloads.userSName = null
                                        val intent = Intent(this@UserDetails, Login::class.java)
                                        intent.putExtra("login", R.layout.login)
                                        overridePendingTransition(0, 0)
                                        startActivity(intent)


                                    }

                                    else -> {

                                        // to do stuff
                                    }
                                }
                            }

                        it.updatePassword(password.text.toString().replace("\\s".toRegex(), ""))
                            .addOnCompleteListener { task ->
                                when {
                                    task.isSuccessful -> {
                                        passwordChange()


                                    }
                                }
                            }.addOnFailureListener { exception ->
                                when ((exception as? FirebaseAuthException)?.errorCode) {
                                    "ERROR_REQUIRES_RECENT_LOGIN" -> {
                                        authorise()

                                    }

                                    "ERROR_USER_TOKEN_EXPIRED" -> {
                                        returns.show()
                                        FirebaseAuth.getInstance().signOut()


                                        val message =
                                            " ${AppSettings.Preloads.userSName} HAS LOGGED OUT!"
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        val sharedPreferences =
                                            getSharedPreferences(
                                                "preferences",
                                                Context.MODE_PRIVATE
                                            )
                                        sharedPreferences.edit().putBoolean("isFirstLogin", true)
                                            .apply()
                                        AppSettings.Preloads.userSName = null
                                        val intent = Intent(this@UserDetails, Login::class.java)
                                        intent.putExtra("login", R.layout.login)
                                        overridePendingTransition(0, 0)
                                        startActivity(intent)

                                    }

                                    else -> {
                                        // to do stuff soon

                                    }
                                }
                            }
                    }


                }
            }
        }
    }

    private var isDialogOpen = false
    private fun authorise() {

        if (!isDialogOpen) {
            isDialogOpen = true
            val builder = AlertDialog.Builder(this)

            val titleView = layoutInflater.inflate(R.layout.authenticate_title, null)
            builder.setCustomTitle(titleView)

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            // Set up the username input
            val emailVerification = EditText(this)
            emailVerification.hint = "Email"
            emailVerification.inputType =
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            layout.addView(emailVerification)

            // Set up the password input
            val passwordInput = EditText(this)
            passwordInput.hint = "password"
            passwordInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layout.addView(passwordInput)
            val confirmInput = EditText(this)
            confirmInput.hint = "password"
            confirmInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layout.addView(confirmInput)

            builder.setView(layout)

            with(builder) {
                setNegativeButtonIcon(
                    ContextCompat.getDrawable(
                        context, R.drawable.baseline_cancel_presentation_24
                    )
                ).setNegativeButton("CLOSE") { _, _ ->
                    Log.d("MyTag", "closing")
                    val alertDialog = builder.create()
                    alertDialog.dismiss()
                    Log.d("MyTag", "closed")
                    isDialogOpen = false

                }
                setPositiveButtonIcon(
                    ContextCompat.getDrawable(
                        context, R.drawable.baseline_check_box_24
                    )
                ).setPositiveButton("SUBMIT") { _, _ ->


                    when {
                        emailVerification.text.isNullOrEmpty() -> {
                            Snackbar.make(emailVerification, e.regEmailError, Snackbar.LENGTH_SHORT)
                                .show()


                        }

                        confirmInput.text.isNullOrEmpty() -> {
                            Snackbar.make(
                                confirmInput,
                                e.confirmPasswordCantBeEmpty,
                                Snackbar.LENGTH_SHORT
                            ).show()


                        }

                        passwordInput.text.isNullOrEmpty() -> {
                            Snackbar.make(
                                passwordInput,
                                e.passwordCantBeEmpty,
                                Snackbar.LENGTH_SHORT
                            ).show()


                        }

                        emailVerification.text.isNullOrEmpty() && confirmInput.text.isNullOrEmpty() -> {
                            Snackbar.make(
                                emailVerification,
                                e.emailValidationEmptyError,
                                Snackbar.LENGTH_SHORT
                            ).show()

                            Snackbar.make(
                                emailVerification,
                                e.confirmPasswordCantBeEmpty,
                                Snackbar.LENGTH_SHORT
                            ).show()


                        }

                        passwordInput.text.toString() != confirmInput.text.toString() -> {
                            Snackbar.make(passwordInput, e.passwordNotMatch, Snackbar.LENGTH_SHORT)
                                .show()


                        }

                        else -> {

                            val user = FirebaseAuth.getInstance().currentUser
                            val credential = EmailAuthProvider.getCredential(
                                emailVerification.text.toString().trim(),
                                confirmInput.text.toString().trim()
                            )
                            user?.reauthenticate(credential)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    validMessage.show()


                                } else {
                                    // Failed to re-authenticate user
                                }
                            }

                        }
                    }
                    isDialogOpen = false
                }
            }.create()

            val alertDialog = builder.create()
            alertDialog.show()
            val button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val layoutParams = button.layoutParams as LinearLayout.LayoutParams
            with(button) {

                setPadding(0, 0, 20, 0)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.WHITE)

                layoutParams.weight = 10f
                button.layoutParams = layoutParams
            }
            alertDialog.show()
            val buttons = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            val layoutParams2 = buttons.layoutParams as LinearLayout.LayoutParams
            with(buttons) {
                setPadding(250, 0, 20, 0)
                setTextColor(Color.WHITE)
                layoutParams2.weight = 10f
                buttons.layoutParams = layoutParams2
            }

            builder.setOnCancelListener { isDialogOpen = false }
        }

    }

    private fun emailChange() {
        val logIntent = Intent(this, AppSettings::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val settingsIntent =
            PendingIntent.getActivity(this, 0, logIntent, PendingIntent.FLAG_MUTABLE)

        val user = FirebaseAuth.getInstance().currentUser?.email
        val channelId = "Details Changed"
        val channelName = "Accounts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        val tempusManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        tempusManager.createNotificationChannel(notificationChannel)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.user)
            .setContentTitle("$user Account details")
            .setContentText("Email has been changed and Details captured $user")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(settingsIntent)
            .setAutoCancel(true)



        tempusManager.notify(0, builder.build())


    }

    private fun passwordChange() {
        val logIntent = Intent(this, AppSettings::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val settingsIntent =
            PendingIntent.getActivity(this, 0, logIntent, PendingIntent.FLAG_MUTABLE)

        val user = FirebaseAuth.getInstance().currentUser?.email
        val channelId = "Details Changed"
        val channelName = "Accounts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        val tempusManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        tempusManager.createNotificationChannel(notificationChannel)


        val unverifiedBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.user)
            .setContentTitle("$user logged in")
            .setContentText("welcome to tempus new $user please verify your account")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(settingsIntent)
            .setAutoCancel(true)

        tempusManager.notify(1, unverifiedBuilder.build())
    }


}