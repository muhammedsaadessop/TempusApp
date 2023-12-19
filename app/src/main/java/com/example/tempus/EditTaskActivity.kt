package com.example.tempus

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import kotlin.math.absoluteValue

class EditTaskActivity : AppCompatActivity() {

    // Creation of objects that are the UI elements
    private val e = Errors()
    private lateinit var selectedDateText: TextView
    private lateinit var selectedStartTimeText: TextView
    private lateinit var selectedEndTimeText: TextView

    private val catEmpty = Crouton.makeText(this, e.emptyCat, Style.ALERT)
    private val taskEmpty = Crouton.makeText(this, e.emptyTaskName, Style.ALERT)
    private val noMing = Crouton.makeText(this, e.noMinGoal, Style.ALERT)
    private val noMx = Crouton.makeText(this, e.noMaxGoal, Style.ALERT)
    private val emptyBody = Crouton.makeText(this, e.emptyDesc, Style.ALERT)
    private val sDate = Crouton.makeText(this, e.noStartDate, Style.ALERT)
    private val sTime = Crouton.makeText(this, e.startTimeNotChosen, Style.ALERT)
    private val eTime = Crouton.makeText(this, e.endTimeNotChosen, Style.ALERT)
    private val noCat = Crouton.makeText(this, e.noCat, Style.ALERT)

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_task)
        setPage()
        security()
        val tasksBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@EditTaskActivity, Tasks::class.java)
                intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, tasksBack)

        selectedDateText = findViewById(R.id.edit_selectedDateText)
        selectedStartTimeText = findViewById(R.id.edit_selectedStartTimeText)
        selectedEndTimeText = findViewById(R.id.edit_selectedEndTimeText)

        //Calling of tasks
        tasks()

        val uploadImage = findViewById<ImageView>(R.id.edit_imgGallery)
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)
        val addbtn = findViewById<ImageButton>(R.id.addbtn)


        // Handling of the uploading of an image
        uploadImage.setOnClickListener {
            val task = findViewById<EditText>(R.id.edit_taskNameInput)
            if (task.text.toString().isEmpty()) {
                val message = "TASK MUST BE ENTERED FIRST! "
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Choose an option")
                builder.setItems(arrayOf("Take a photo?", "Pick from gallery?","No Picture?")) { _, which ->
                    when (which) {
                        0 -> camera.launch(null)
                        1 -> galleryContent.launch("imageURL/*")
                        2 -> noPic()
                    }
                }
                val dialog = builder.create()
                dialog.show()
            }

        }

        // this creates a vertical layout Manager

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


    }
    fun noPic() {
        val task = findViewById<EditText>(R.id.edit_taskNameInput)
        val noPicstore = Firebase.storage.reference.child(task.text.toString().trim())
        val noTaskimages = findViewById<ImageView>(R.id.edit_imgGallery)
        noTaskimages.setImageResource(R.drawable.task)

        val convertNoImage = BitmapFactory.decodeResource(resources, R.drawable.task)


        val drawPic = File(cacheDir, "task.png")
        val gotten = FileOutputStream(drawPic)
        convertNoImage.compress(Bitmap.CompressFormat.PNG, 100, gotten)
        gotten.close()

        // Upload the file to Firebase Storage
        val url = Uri.fromFile(drawPic)
        val picsUpload = noPicstore.putFile(url)

        picsUpload.addOnSuccessListener {

        }.addOnFailureListener {

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
                val intent = Intent(this@EditTaskActivity, Login::class.java)
                intent.putExtra("login", R.layout.login)
                overridePendingTransition(0, 0)
                startActivity(intent)

            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

            } else {
                val exception = task.exception
                if (exception is FirebaseAuthInvalidUserException) {
                    val errorCode = exception.errorCode
                    if (errorCode == "ERROR_USER_NOT_FOUND") {
                        val sharedPreferences =
                            getSharedPreferences("preferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                        AppSettings.Preloads.userSName = null
                        val intent = Intent(this@EditTaskActivity, Login::class.java)
                        intent.putExtra("login", R.layout.login)
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    //Method that handles the saving of data to the database
    private val galleryContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { url: Uri? ->
            try {
                when {
                    url != null -> {
                        val task = findViewById<EditText>(R.id.edit_taskNameInput)
                        val imageView = findViewById<ImageView>(R.id.edit_imgGallery)
                        imageView.setImageURI(url)
                        val store =
                            Firebase.storage.reference.child(task.text.toString().trim())
                        val choice = store.putFile(url)
                        choice.addOnSuccessListener {
                        }.addOnFailureListener {

                        }
                    }
                }
            } catch (e: Exception) {
                // Handle the exception here

            }


        }
    private val camera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo: Bitmap? ->
            try {
                val task = findViewById<EditText>(R.id.edit_taskNameInput)
                val imageView = findViewById<ImageView>(R.id.edit_imgGallery)
                imageView.setImageBitmap(photo)
                val imageRef = Firebase.storage.reference.child(task.text.toString().trim())
                val imageStream = ByteArrayOutputStream()
                photo?.compress(Bitmap.CompressFormat.JPEG, 100, imageStream)
                val data = imageStream.toByteArray()
                val uploadDP = imageRef.putBytes(data)
                uploadDP.addOnSuccessListener {
                    val message = "IMAGE UPLOADED "
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    val message = "INVALID IMAGE!"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle the exception here


            }
        }


    // Method that handles the selection of a date using a date picker
    fun selectDate(view: View) {
        try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->

                    selectedDateText.text = "$dayOfMonth/${month + 1}/$year"
                },
                year,
                month,
                dayOfMonth
            )
            datePickerDialog.show()
        } catch (e: Exception) {
            // Handle the exception here
        }

    }

    //Method that handles the selection of a timer for start time
    fun selectTime(view: View) {
        try {
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = object : TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedStartTime =
                        String.format("%02d:%02d", hourOfDay, minute)
                    selectedStartTimeText.text = selectedStartTime
                },
                hourOfDay,
                minute,
                true
            ) {
                override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    val roundedMinute = (minute / 1) * 1
                    when {
                        minute != roundedMinute -> {
                            view?.minute = roundedMinute
                        }
                    }
                }
            }

            timePickerDialog.show()
        } catch (e: Exception) {
            // Handle the exception here

        }


    }

    //Method that handles the selection of a timer for start time and end time
    fun selectEndTime(view: View) {
        try {
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = object : TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedEndTime =
                        String.format("%02d:%02d", hourOfDay, minute )
                    selectedEndTimeText.text = selectedEndTime
                },
                hourOfDay,
                minute,
                true
            ) {
                //Intervals for time picker
                override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    val roundedMinute = (minute / 1) * 1
                    when {
                        minute != roundedMinute -> {
                            view?.minute = roundedMinute
                        }
                    }
                }
            }
            timePickerDialog.show()

        } catch (e: Exception) {

        }
    }

    // Getting the data from the database and setting into the UI elements
    fun setPage() {
        try {
            // Connecting the ui elements to the variables.
            val tName = findViewById<TextView>(R.id.edit_taskNameInput)
            val catname = findViewById<Spinner>(R.id.edit_category_spinner)
            val desc = findViewById<TextView>(R.id.edit_taskDescriptionInput)
            val sDate = findViewById<TextView>(R.id.edit_selectedStartTimeText)
            val eDate = findViewById<TextView>(R.id.edit_selectedEndTimeText)
            // val hours2 = findViewById<TextView>(R.id.hour_text)
            val min = findViewById<Spinner>(R.id.edit_minimumGoalSpinner)
            val max = findViewById<Spinner>(R.id.edit_maximumGoalSpinner)
            val date = findViewById<TextView>(R.id.edit_selectedDateText)
            val taskImage = findViewById<ImageView>(R.id.edit_imgGallery)
            //Gets the value of the item id of the task shown on the edit page.
            val itemId = intent.getStringExtra("item_id")
            Log.d("itemid", itemId.toString())
            //THIS INDEX LETS THE FOR LOOP SORT THE SPECIFIC INDEX WHICH WONT CHANGE AS PER THE POSITION WHICH WILL CHANGE
            val db = FirebaseFirestore.getInstance()
            //Gets the value of the current logged in user.
            val userid = FirebaseAuth.getInstance().currentUser?.uid
            // Query Firestore to get the data for the clicked item
            db.collection("TaskStorage")
                .whereEqualTo("userIdTask", userid.toString().trim())
                .whereEqualTo("taskName", itemId)
                .get()
                .addOnSuccessListener { documents ->
                    // Get the last document in the result, which corresponds to the clicked item
                    val document = documents.documents.lastOrNull()
                    if (document != null) {
                        // Use the data from Firestore to populate the fields in your form
                        tName.text = document.getString("taskName")
                        Log.d("YourTag", "name: " + tName)
                        desc.text = document.getString("description")
                        sDate.text = document.getString("starTime")
                        eDate.text = document.getString("endTime")
                        min.setSelection(document.getString("minGoal")?.toInt() ?: 0)
                        max.setSelection(document.getString("maxGoal")?.toInt() ?: 0)
                        date.text = document.getString("dateAdded")
                        val url = document.getString("imageURL")
                        Glide.with(this)
                            .load(url)
                            .into(taskImage)
                    }
                }

        } catch (e: Exception) {
            // Handle the exception here

        }


    }

    //The tasks method gets the values from the database and then assigns retrived values to the text fields.
    private fun tasks() {
        try {
            val spinner = findViewById<Spinner>(R.id.edit_category_spinner)
            val userid = Firebase.auth.currentUser?.uid
            val db = FirebaseFirestore.getInstance()
            val spinnerList = mutableListOf<String>()
            db.collection("CategoryStorage")
                .whereEqualTo("userIdCat", userid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val value =
                            document.getString("categoryID")
                        if (value != null) {
                            spinnerList.add(value)
                        }
                    }


                    val spinnerAdapter =
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = spinnerAdapter

                }
            val create = findViewById<Button>(R.id.edit_createTask)
            val task = findViewById<EditText>(R.id.edit_taskNameInput)
            val description = findViewById<EditText>(R.id.edit_taskDescriptionInput)
            val dates = findViewById<TextView>(R.id.edit_selectedDateText)
            val start = findViewById<TextView>(R.id.edit_selectedStartTimeText)
            val end = findViewById<TextView>(R.id.edit_selectedEndTimeText)
            val minimum = findViewById<Spinner>(R.id.edit_minimumGoalSpinner)
            val maximumGoalSpinner = findViewById<Spinner>(R.id.edit_maximumGoalSpinner)
            val maxArray = (1..24).toList()
            val maxAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, maxArray)
            maxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            maximumGoalSpinner.adapter = maxAdapter
            val minAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, maxArray)
            minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            minimum.adapter = minAdapter

            create.setOnClickListener {

                try {

                    val max = maximumGoalSpinner.selectedItem.toString()
                    val min = minimum.selectedItem.toString()
                    val selectedItem = spinner.selectedItem.toString()
                    //Error checking code for when the values are empty.
                    when {
                        task.text.toString().isEmpty() -> {
                            taskEmpty.show()
                        }

                        description.text.toString().isEmpty() -> {
                            emptyBody.show()
                        }

                        start.text.toString().isEmpty() -> {

                            sTime.show()
                        }

                        end.text.toString().isEmpty() -> {
                            eTime.show()

                        }

                        dates.text.toString().isEmpty() -> {
                            sDate.show()

                        }

                        selectedItem.isEmpty() -> {
                            catEmpty.show()
                        }

                        max.isEmpty() -> {
                            noMx.show()
                        }

                        min.isEmpty() -> {
                            noMing.show()
                        }

                        else -> {
                            var picture: String

                            val firestore = Firebase.firestore
                            val storageRef =
                                Firebase.storage.reference.child(task.text.toString().trim())
                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                picture = downloadUrl.toString()
                                val taskName = task.text.toString().trim()
                                val categoryTask = selectedItem.trim()
                                val tabName = "$categoryTask$taskName"
                                val description = description.text.toString().trim()
                                val startTime = start.text.toString().trim()
                                val endTime = end.text.toString().trim()
                                val maxGoal = max.trim()
                                val mingoal = minimum.selectedItem.toString().trim()
                                val date = dates.text.toString().trim()

                                val breaksHours = 0
                                //Gets the current users id
                                val userid = Firebase.auth.currentUser?.uid
                                //Gets the start time
                                val start = start.text.toString().replace(Regex("[^\\w\\s:]"), "")
                                //Gets the end time
                                val end = end.text.toString().replace(Regex("[^\\w\\s:]"), "")

                                //Slipts start time into hors and mins
                                val startSplit = start.split(":")
                                val sHours = startSplit[0].toInt()
                                val sMinutes = startSplit[1].toInt()
                                //Splits the end time into two parts hours and mins
                                val endTimeParts = end.split(":")
                                val endHours = endTimeParts[0].toInt()
                                val endMinutes = endTimeParts[1].toInt()
                                val startTotalMinutes = sHours * 60 + sMinutes
                                val endTotalMinutes = endHours * 60 + endMinutes

                                val diffMinutes =
                                    (endTotalMinutes - startTotalMinutes).absoluteValue

                                val diffHours = diffMinutes / 60
                                val diffRemainingMinutes = diffMinutes % 60
                                val categoryName = selectedItem.trim()
                                val db = Firebase.firestore
                                val categoryRef =
                                    db.collection("CategoryStorage").document(categoryName)
                                //  creates a reference to a specific document within the "CategoryStorage" collection
                                categoryRef.get()
                                    .addOnSuccessListener { document ->
                                        //Gets the value in the document totalHours
                                        val categoryHours = document.get("totalHours")
                                        //Takes out the : from categoryHours
                                        val currentSplit = categoryHours.toString().split(":")
                                        val hoursValue = currentSplit[0].toInt()
                                        val minutesValue = currentSplit[1].toInt()
                                        val newTotalMinutes =
                                            hoursValue * 60 + minutesValue + diffMinutes
                                        val newHoursValue = newTotalMinutes / 60
                                        val newRemainingMinutesValue = newTotalMinutes % 60
                                        categoryRef.update(
                                            "totalHours",
                                            "%02d:%02d".format(
                                                newHoursValue,
                                                newRemainingMinutesValue
                                            )
                                        )
                                    }
                                //Formats the hours in to the correct format
                                val hours = "%02d:%02d".format(diffHours, diffRemainingMinutes)
                                val timeRemain = hours
                                //Checks if the picture is empty
                                when {
                                    picture.isEmpty() -> {
                                        val message = "ERROR NO IMAGE CHOSEN"
                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                    else -> {
                                        //Updates the the old vales in the datebase with the new values that the user and put in.
                                        db.collection("TaskStorage")
                                            .whereEqualTo("userIdTask", userid.toString().trim())
                                            .whereEqualTo("taskName", taskName)
                                            .get()
                                            .addOnSuccessListener { queryResult ->
                                                val document = queryResult.documents.lastOrNull()
                                                if (document != null) {
                                                    document.reference.update(
                                                        "categoryName",
                                                        categoryName
                                                    )
                                                    document.reference.update(
                                                        "description",
                                                        description
                                                    )
                                                    //THe updating with the new infomation to the database
                                                    document.reference.update("dateAdded", date)
                                                    document.reference.update("starTime", startTime)
                                                    document.reference.update("duration", hours)
                                                    document.reference.update(
                                                        "timeRemaining",
                                                        timeRemain
                                                    )
                                                    document.reference.update("imageURL", picture)
                                                    document.reference.update("endTime", endTime)
                                                    document.reference.update("starTime", startTime)
                                                    document.reference.update("minGoal", mingoal)
                                                    document.reference.update("maxGoal", maxGoal)
                                                    document.reference.update(
                                                        "breakDurations",
                                                        breaksHours
                                                    )
                                                    document.reference.update("tabID", tabName)
                                                    val message = "New Details Captured"
                                                    Toast.makeText(
                                                        applicationContext,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }

                                            }

                                    }
                                }

                            }.addOnFailureListener {
                                val message = "ERROR NO IMAGE CHOSEN"
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                    }


                } catch (e: Exception) {
                    // Handle the exception here
                }


            }
        } catch (e: Exception) {
            // Handle the exception here
        }
    }
}