package com.example.tempus

import android.app.AlarmManager
import android.app.Dialog
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style
import java.io.File
import kotlin.system.exitProcess

class AppSettings : AppCompatActivity() {
    private val m = Messages()
    private val e = Errors()
    private val emailType = Crouton.makeText(this, e.notYourUsername, Style.ALERT)
    private val passwordEmpty = Crouton.makeText(this, e.passwordCantBeEmpty, Style.ALERT)
    private val usernameEmpty = Crouton.makeText(this, e.emptyUserName, Style.ALERT)
    private val noDetails = Crouton.makeText(this, e.noDetailsEntered, Style.ALERT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_settings)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@AppSettings, Home::class.java)
                intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        security()
        populateFields()
        FirebaseApp.initializeApp(this)
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)
        val accsetting = findViewById<CardView>(R.id.account_setting)
        val addbtn = findViewById<ImageButton>(R.id.addbtn)
        val logout = findViewById<CardView>(R.id.logout)
        val deleteappdata = findViewById<CardView>(R.id.delete_data)
        val deleteuser = findViewById<CardView>(R.id.delete_user)
        val appcache = findViewById<CardView>(R.id.delete_cache)
        val privacyPolicy = findViewById<CardView>(R.id.privacy)

        privacyPolicy.setOnClickListener()
        {
            val breakspage = Intent(this, PrivacyPolicy::class.java)
            startActivity(breakspage)
            overridePendingTransition(0, 0)
            finish()
        }
        appcache.setOnClickListener()
        {
            val clearCache = AlertDialog.Builder(this)
            clearCache.setTitle("Do you want to clear cache(closes app)?")
            clearCache.setItems(arrayOf("Yes", "Cancel")) { _, which ->
                when (which) {

                    0 -> activate()
                    1 -> Dialog.BUTTON_NEGATIVE
                }

            }

            val dialog = clearCache.create()
            dialog.show()

        }
        deleteuser.setOnClickListener()
        {
            val userDeletion = AlertDialog.Builder(this)
            userDeletion.setTitle("Are you sure?")
            userDeletion.setItems(arrayOf("Yes", "Cancel")) { _, which ->
                when (which) {

                    0 -> deleteUser()
                    1 -> Dialog.BUTTON_NEGATIVE
                }

            }

            val dialog = userDeletion.create()
            dialog.show()


        }
        homebtn.setOnClickListener {
            val homepage = Intent(this, Home::class.java)
            homepage.putExtra("home", intent.getIntExtra("home", R.layout.home))
            startActivity(homepage)
            overridePendingTransition(0, 0)
            finish()
        }
//
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

        addbtn.setOnClickListener()
        {


            val shortcut = BottomSheetDialog(this)
            val shortcutView = layoutInflater.inflate(R.layout.shortcut, null)

            shortcut.setContentView(shortcutView)

            shortcut.show()

            val createNewCat = shortcutView.findViewById<Button>(R.id.add_category)

            createNewCat.setOnClickListener {
                val newForm = Intent(this, CategoryForm::class.java)
                startActivity(newForm)
                overridePendingTransition(0, 0)
                finish()

                shortcut.dismiss()
            }

            val createNewTask = shortcutView.findViewById<Button>(R.id.add_task)
            createNewTask.setOnClickListener {

                val newTask = Intent(this, TaskForm::class.java)
                startActivity(newTask)
                overridePendingTransition(0, 0)
                finish()

                shortcut.dismiss()
            }




        }
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()


            val message = " ${Preloads.userSName} HAS LOGGED OUT!"
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
            Preloads.userSName = null
            val loginpage = Intent(this@AppSettings, Login::class.java)
            loginpage.putExtra("login", R.layout.login)
            overridePendingTransition(0, 0)
            startActivity(loginpage)
        }
        accsetting.setOnClickListener {
            when (Preloads.userSName) {
                null -> {
                    accountVerify()
                }

                else -> {
                    val accsettings = Intent(this, UserDetails::class.java)
                    startActivity(accsettings)
                    overridePendingTransition(0, 0)
                    finish()


                }
            }
        }
        deleteappdata.setOnClickListener {

            val deleteoptions = AlertDialog.Builder(this)
            deleteoptions.setTitle("Choose an option")
            deleteoptions.setItems(
                arrayOf("Delete all Images", "Delete Tasks and Images", "Wipe Application DATA")
            ) { _, which ->
                when (which) {

                    0 -> imageDelete()
                    1 -> tasksImageDelete()
                    2 -> allDelete()

                }


            }
            val dialog = deleteoptions.create()
            dialog.show()
        }
    }

    private fun activate() {
        cache(this)
        restartApp()
    }

    private fun security() {

        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            when (firebaseAuth.currentUser) {
                null -> {

                    val sharedPreferences =
                        getSharedPreferences("preferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                    Preloads.userSName = null
                    val intent = Intent(this@AppSettings, Login::class.java)
                    intent.putExtra("login", R.layout.login)
                    overridePendingTransition(0, 0)
                    startActivity(intent)

                }
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    //stuff to do

                }

                else -> {
                    when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            when (exception.errorCode) {
                                "ERROR_USER_NOT_FOUND" -> {
                                    val sharedPreferences =
                                        getSharedPreferences("preferences", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putBoolean("isFirstLogin", true)
                                        .apply()
                                    Preloads.userSName = null
                                    val loginpage = Intent(this@AppSettings, Login::class.java)
                                    loginpage.putExtra("login", R.layout.login)
                                    overridePendingTransition(0, 0)
                                    startActivity(loginpage)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private var isDialogOpen = false
    private fun restartApp() {
        val intent = Intent(applicationContext, Home::class.java)
        val nid = 0
        val nid2 = PendingIntent.getActivity(
            applicationContext,
            nid,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val amgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        amgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, nid2)
        exitProcess(0)
    }

    private fun emailCheck(email: String): Boolean {
        val emailCheck = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
        return emailCheck.toRegex().matches(email)
    }

    private fun accountVerify() {
        if (!isDialogOpen) {
            isDialogOpen = true
            val builder = AlertDialog.Builder(this)

            val titleView = layoutInflater.inflate(R.layout.authenticate_title, null)
            builder.setCustomTitle(titleView)

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            // Set up the username input
            val usernameInput = EditText(this)
            usernameInput.hint = "Username"
            layout.addView(usernameInput)

            // Set up the password input
            val passwordInput = EditText(this)
            passwordInput.hint = "password"
            passwordInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layout.addView(passwordInput)

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
                    try {
                        val username = usernameInput.text.toString().trim()
                        val password = passwordInput.text.toString().trim()
                        val verify = username + password
                        val database = Firebase.database
                        val userid = FirebaseAuth.getInstance().currentUser?.uid
                        val myRef = database.getReference("UserDetails")
                        val oldRef =
                            FirebaseDatabase.getInstance().getReference("UserDetails/$verify")
                        val newRef = FirebaseDatabase.getInstance()
                            .getReference("UserDetails/${userid.toString()}")

                        when {
                            usernameInput.text.isNullOrEmpty() -> {
                                usernameEmpty.show()

                            }

                            emailCheck(username) -> {
                                emailType.show()

                            }

                            passwordInput.text.isNullOrEmpty() -> {
                                passwordEmpty.show()

                            }

                            usernameInput.text.isNullOrEmpty() && passwordInput.text.isNullOrEmpty() -> {
                                noDetails.show()

                            }

                            else -> {

                                oldRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val data = dataSnapshot.value as? Map<*, *>
                                        if (dataSnapshot.exists()) {
                                            if (data != null) {
                                                newRef.setValue(data) { error, _ ->
                                                    if (error == null) {
                                                        oldRef.removeValue()
                                                        myRef.child(userid.toString())
                                                            .child("userid")
                                                            .setValue(userid)
                                                        recreate()
                                                    }
                                                }
                                            } else {
                                                validerror(Errors())
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Handle error
                                    }
                                })
                            }
                        }
                        isDialogOpen = false
                    } catch (E: DatabaseException) {
                        emailType.show()
                    }
                }.create()
            }
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


    fun validerror(errors: Errors) {
        val crouton = Crouton.makeText(this, errors.validationError, Style.ALERT)
        crouton.show()
    }

    object Preloads {
        var names: String = ""
        var surname: String = ""
        var emails: String = ""
        var userSName: String? = null
        var conPass: String = ""
        var pass: String = ""
    }

    private fun populateFields() {
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database
        val myRef = database.getReference("UserDetails")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {

                    val userId = data.child("userid").getValue(String::class.java)
                    when {
                        userId.toString().trim() == userid.toString().trim() -> {

                            when {
                                Preloads.names.isEmpty() -> {
                                    Preloads.names =
                                        data.child("firstname").getValue(String::class.java)
                                            .toString()
                                    Preloads.emails =
                                        data.child("emailaddress").getValue(String::class.java)
                                            .toString()
                                    Preloads.surname =
                                        data.child("surname").getValue(String::class.java)
                                            .toString()
                                    Preloads.userSName =
                                        data.child("displayname").getValue(String::class.java)
                                            .toString()
                                    Preloads.conPass =
                                        data.child("confirmkey").getValue(String::class.java)
                                            .toString()
                                    Preloads.pass =
                                        data.child("password").getValue(String::class.java)
                                            .toString()


                                }
                            }
                        }
                    }


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //stuff to do still
            }
        })

    }

    private fun allDelete() {

        val db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser?.uid

        val storage = Firebase.storage

        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskName = task.getString("taskName") ?: ""
                    val imageRef = storage.reference.child(taskName)
                    imageRef.delete()
                }
            }




        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    db.collection("TaskStorage").document(task.id).delete()
                }
            }


        db.collection("CategoryStorage").whereEqualTo("userIdCat", userid).get()
            .addOnSuccessListener { categories ->
                for (category in categories) {
                    db.collection("CategoryStorage").document(category.id).delete()
                }
            }
    }

    private fun tasksImageDelete() {
        val db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser?.uid

        val storage = Firebase.storage

        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskName = task.getString("taskName") ?: ""
                    val imageRef = storage.reference.child(taskName)
                    imageRef.delete()
                }
            }




        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    db.collection("TaskStorage").document(task.id).delete()
                }
            }


    }

    private fun imageDelete() {


        val db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser?.uid

        val storage = Firebase.storage

        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskName = task.getString("taskName") ?: ""
                    val imageRef = storage.reference.child(taskName)
                    imageRef.delete()
                }
            }


    }

    private fun deleteUser() {
        val db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser?.uid

        val storage = Firebase.storage

        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskName = task.getString("taskName") ?: ""
                    val imageRef = storage.reference.child(taskName)
                    imageRef.delete()
                }
            }




        db.collection("TaskStorage").whereEqualTo("userIdTask", userid).get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    db.collection("TaskStorage").document(task.id).delete()
                }
            }


        db.collection("CategoryStorage").whereEqualTo("userIdCat", userid).get()
            .addOnSuccessListener { categories ->
                for (category in categories) {
                    db.collection("CategoryStorage").document(category.id).delete()
                }
            }
        val ref = FirebaseDatabase.getInstance().getReference()
     ref.child("UserDetails").child(userid.toString()).removeValue()




        val tempusUser = FirebaseAuth.getInstance().currentUser
        tempusUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val sharedPreferences =
                    getSharedPreferences("preferences", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                Preloads.userSName = null
                val intent = Intent(this@AppSettings, Login::class.java)
                intent.putExtra("login", R.layout.login)
                overridePendingTransition(0, 0)
                Toast.makeText(this, m.deleteConfirmation, Toast.LENGTH_SHORT).show()
                startActivity(intent)


            }
        }
    }

    private fun cache(context: Context) {
        try {
            val dir: File = context.cacheDir
            appFiles(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appFiles(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children: Array<String> = dir.list() as Array<String>
            for (i in children.indices) {
                val success = appFiles(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir?.delete() ?: false
    }
}