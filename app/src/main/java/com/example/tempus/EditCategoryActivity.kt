package com.example.tempus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style

class EditCategoryActivity : AppCompatActivity() {
    // this is for error handling and calls from the error data class
    private val e = Errors()

    // the crouton shows the issue
    private val emptyCatName = Crouton.makeText(this, e.CatNewNameEmpty, Style.ALERT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)
        updateStuff()
// val for the buttons which  allow for switching pages
        val update = findViewById<Button>(R.id.EditCategory)
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        val categoryName = findViewById<TextView>(R.id.EditcatNameInput)
        val ids = findViewById<TextView>(R.id.edit_id)
        val db = FirebaseFirestore.getInstance()
        val addbtn = findViewById<ImageButton>(R.id.addbtn)
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)

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

        update.setOnClickListener()
        {
            if (categoryName.text.isNullOrEmpty()) {

                emptyCatName.show()

            } else {


                db.collection("CategoryStorage").whereEqualTo("categoryID", ids.text.toString())
                    .whereEqualTo("userIdCat", userid).get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.update("categoryID", categoryName.text.toString())

                            val categoryStorage = Firebase.firestore
// get the data from 'oldDocument'
                            categoryStorage.collection("CategoryStorage")
                                .document(ids.text.toString())
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        val data = document.data
                                        // save the data to 'newDocument'
                                        categoryStorage.collection("CategoryStorage")
                                            .document(categoryName.text.toString())
                                            .set(data!!)
                                            .addOnSuccessListener {
                                                // delete the old document
                                                categoryStorage.collection("CategoryStorage")
                                                    .document(ids.text.toString())
                                                    .delete()
                                            }
                                    }
                                    Toast.makeText(
                                        applicationContext,
                                        "updated category",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()


                                }
                        }


                    }
            }
        }

    }

    private fun updateStuff() {
        val tName = findViewById<TextView>(R.id.EditcatNameInput)
        val ids = findViewById<TextView>(R.id.edit_id)

        val itemId = intent.getStringExtra("item_id")


        val db = FirebaseFirestore.getInstance()


        val userid = FirebaseAuth.getInstance().currentUser?.uid
// Query Firestore to get the data for the clicked item
        db.collection("CategoryStorage")
            .whereEqualTo("userIdCat", userid.toString().trim())
            .whereEqualTo("categoryID", itemId)
            .get()
            .addOnSuccessListener { documents ->
                // Get the last document in the result, which corresponds to the clicked item
                val document = documents.documents.lastOrNull()
                if (document != null) {

                    tName.text = document.getString("categoryID")
                    ids.text = document.getString("categoryID")


                }
            }
    }
}