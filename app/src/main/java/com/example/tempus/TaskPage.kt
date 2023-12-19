package com.example.tempus


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale


// THIS PAGE HANDLES THE DISPLAY OF THE TASKS WHEN A SPECIFIC TASK IS CLICKED
class TaskPage : AppCompatActivity() {

    //Button object instantiaion
    private var taskCountDown: TextView? = null
    private var breakCount: TextView? = null

    private var stop: ImageView? = null
    private var breakStop: ImageView? = null

    private var playPause: ImageView? = null
    private var playPauseBreak: ImageView? = null

    private var restart: ImageView? = null
    private var restartBreak: ImageView? = null

    private var timer: CountDownTimer? = null
    private var timerBreaks: CountDownTimer? = null

    private var taskRunning = false
    private var breaksRunning = false

    private var startTime: Long = 0
    private var startTimeBreaks: Long = 0

    private var remainingTime: Long = 0
    private var remainingTimeBreak: Long = 0

    private var finish: Long = 0
    private var finishBreaks: Long = 0

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "your_channel_id"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.task_page)
            val tasksBack = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(this@TaskPage, Tasks::class.java)
                    intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()

                }
            }
            onBackPressedDispatcher.addCallback(this, tasksBack)
            //Instantiation of methods
            security()
            FirebaseApp.initializeApp(this)
            taskPopulation()
            getBreaks()
            //Assigning the UI elements to variables
            val taskImage = findViewById<ImageView>(R.id.task_image)
            val homebtn = findViewById<ImageButton>(R.id.hometbtn)
            val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
            val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
            val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)
            val addbtn = findViewById<ImageButton>(R.id.addbtn)
            //Linking the text views
            taskCountDown = findViewById(R.id.text_view_countdown)
            breakCount = findViewById(R.id.break_view_countdown)
            stop = findViewById(R.id.task_set)
            breakStop = findViewById(R.id.break_set)
            playPause = findViewById(R.id.task_start_pause)
            playPauseBreak = findViewById(R.id.break_start_pause)
            restart = findViewById(R.id.task_reset)
            restartBreak = findViewById(R.id.break_reset)

            stop!!.setOnClickListener {
                playPause!!.visibility = View.GONE
                restart!!.visibility = View.GONE

            }

            breakStop!!.setOnClickListener {
                playPauseBreak!!.visibility = View.GONE
                restartBreak!!.visibility = View.GONE
            }
            //The playPause for tasktimer setOnClickListener
            playPause!!.setOnClickListener {
                if (taskRunning) {
                    pauseTask()
                } else {
                    startTimer()
                }
            }
            //The playPause21 breakstimer setOnClickListener
            playPauseBreak!!.setOnClickListener {
                if (breaksRunning) {
                    pauseBreaks()
                } else {
                    beginBreak()
                }
            }

            restart!!.setOnClickListener {
                reset()
                recreate()
            }


            restartBreak!!.setOnClickListener {
                resetBreak()
                recreate()
            }

            taskImage.isEnabled = true
            taskImage.isClickable = true
            taskImage.setOnClickListener {
                Log.d("MyApp", "ImageView clicked")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Choose an option")
                builder.setItems(arrayOf("Take a photo", "Pick from gallery")) { _, which ->
                    when (which) {

                        0 -> camera.launch(null)
                        1 -> galleryContent.launch("imageURL/*")
                    }

                }

                val dialog = builder.create()
                dialog.show()
            }

            addbtn.setOnClickListener()
            {
                val tform = Intent(this, TaskForm::class.java)
                overridePendingTransition(0, 0)
                startActivity(tform)
                finish()

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
                overridePendingTransition(0, 0)
                startActivity(breakspage)
                finish()

            }

            statsbtn.setOnClickListener {
                val statspage = Intent(this, Statistics::class.java)
                overridePendingTransition(0, 0)
                startActivity(statspage)
                finish()

            }

            settingsbtn.setOnClickListener {
                val settingspage = Intent(this, AppSettings::class.java)
                overridePendingTransition(0, 0)
                startActivity(settingspage)
                finish()

            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
        }


    }

    //sets the start time of the task timer
    private fun startTimeTask(milliseconds: Long) {
        startTime = milliseconds
        reset()

    }

    //sets the start time of the break timer
    private fun startTimeBreaks(milliseconds: Long) {
        startTimeBreaks = milliseconds
        resetBreak()

    }

    // This function starts the timer and handles the notifications
    private fun startTimer() {
        createNotificationChannel()

        // Build the initial notification without the timer countdown
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Running")
            .setContentText("The timer is currently running.")
            .setSmallIcon(R.drawable.clock)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        // Show the initial notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        // Set the end time for the timer
        finish = System.currentTimeMillis() + remainingTime

        // Starting of the countdown timer
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateText()
                updates(calculationsTask())

                if (remainingTime % (30 * 60 * 1000) == 0L) {
                    val updatedNotificationBuilder =
                        notificationBuilder.setContentText(calculationsTask())

                    notificationManager.notify(
                        NOTIFICATION_ID,
                        updatedNotificationBuilder.build()
                    )
                }


            }

            // When the timer is done this methods runs
            override fun onFinish() {
                taskRunning = false
                updateViewTask()

                // Update the notification when the timer finishes
                val updatedNotificationBuilder =
                    notificationBuilder.setContentText("Timer finished")
                notificationManager.notify(
                    NOTIFICATION_ID,
                    updatedNotificationBuilder.build()
                )
            }
        }.start()

        taskRunning = true
        updateViewTask()
    }

    // This method begins the break timer and handles the notifications
    private fun beginBreak() {
        createNotificationChannel()
        // Build the initial notification without the timer countdown
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Running")
            .setContentText("The timer is currently running.")
            .setSmallIcon(R.drawable.clock)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the initial notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        // Set the end time for the timer
        finishBreaks = System.currentTimeMillis() + remainingTimeBreak
        // Start the countdown timer
        timerBreaks = object : CountDownTimer(remainingTimeBreak, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeBreak = millisUntilFinished
                updateTimerBreaks()
                updates(getFormattedTimeLeftBreak())
                if (remainingTimeBreak % (30 * 60 * 1000) == 0L) {
                    val updatedNotificationBuilder =
                        notificationBuilder.setContentText(getFormattedTimeLeftBreak())
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        updatedNotificationBuilder.build()
                    )
                }
            }

            // This function is tasked to handle what happens when the break timer is done
            override fun onFinish() {
                breaksRunning = false
                updateViewBreak()
                // Update the notification when the timer finishes
                val updatedNotificationBuilder =
                    notificationBuilder.setContentText("Timer finished")
                notificationManager.notify(
                    NOTIFICATION_ID,
                    updatedNotificationBuilder.build()
                )
            }
        }.start()
        breaksRunning = true
        updateViewBreak()
    }

    //Method to handle the updating of the notification
    private fun updates(contentText: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Running")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.clock)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)


        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    //The function calculates the remaining time in hours, minutes, and seconds from a variable remainingTime
    private fun calculationsTask(): String {
        val hours = (remainingTime / 1000) / 3600
        val minutes = ((remainingTime / 1000) / 60) % 60
        val seconds = (remainingTime / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun getFormattedTimeLeftBreak(): String {
        val hours = (remainingTimeBreak / 1000) / 3600
        val minutes = ((remainingTimeBreak / 1000) / 60) % 60
        val seconds = (remainingTimeBreak / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }


    //This function establishes the creation of the notification channel that the timers are using
    private fun createNotificationChannel() {
        val channelName = "Timer Channel"
        val channelDescription = "Channel for displaying timer notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        channel.description = channelDescription


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    //Pauses the task timer, updates the view, and performs database operations to store the remaining time and calculate completed time for the task.
    private fun pauseTask() {
        // Cancel the timer and update taskRunning and the view.
        timer!!.cancel()
        taskRunning = false
        updateViewTask()
        val currentTime = remainingTime
        val currentTimeFormatted = calculationsTask()
        Log.d("YourTag", "PauseDuration Retrieved: $currentTimeFormatted")
        val currentTimeWithoutSeconds = currentTimeFormatted.substringBeforeLast(":")
        Log.d("YourTag", "currentTimeWithoutSeconds: $currentTimeWithoutSeconds")
        // Retrieve the itemId from the intent.
        val itemId = intent.getStringExtra("itemId")

        val db = FirebaseFirestore.getInstance()
        //gets the currnt value of the signed in user id
        val userid = FirebaseAuth.getInstance().currentUser?.uid

        //Perform database operations.
        db.collection("TaskStorage")
            .whereEqualTo("userIdTask", userid.toString().trim())
            .whereEqualTo("taskName", itemId)
            .get()
            .addOnSuccessListener { queryResult ->
                // Get the document object
                val document = queryResult.documents.lastOrNull()
                if (document != null) {
                    // Retrieve relevant fields from the document.
                    val durationVar = document.getString("timeRemaining")
                    val completedstuff = document.getString("completedHours")
                    val intialduration = document.getString("duration")
                    Log.d("YourTag", "PauseDuration Retrieved: $durationVar")
                    // Split and parse hour, minute, and second components of initial duration.
                    val hourd = intialduration?.split(":")
                    val dhour1 = hourd?.getOrNull(0)?.toIntOrNull() ?: 0
                    val min2 = hourd?.getOrNull(1)?.toIntOrNull() ?: 0
                    val sec2 = hourd?.getOrNull(2)?.toIntOrNull() ?: 0
                    // Split and parse hour, minute, and second components of completed hours.
                    val hourpart = completedstuff?.split(":")
                    val dhour = hourpart?.getOrNull(0)?.toIntOrNull() ?: 0
                    val min = hourpart?.getOrNull(1)?.toIntOrNull() ?: 0
                    val sec = hourpart?.getOrNull(2)?.toIntOrNull() ?: 0
                    // Split and parse hour, minute, and second components of remaining time.
                    val parts = durationVar?.split(":")
                    val hours = parts?.getOrNull(0)?.toIntOrNull() ?: 0
                    val minutes = parts?.getOrNull(1)?.toIntOrNull() ?: 0
                    val seconds = parts?.getOrNull(2)?.toIntOrNull() ?: 0
                    // Calculate millis3 and millis2 for duration calculations.
                    var millis3 = (dhour1 * 60 * 60 * 1000) + (min2 * 60 * 1000) + (sec2 * 1000)
                    val millis2 = (dhour * 60 * 60 * 1000) + (min * 60 * 1000) + (sec * 1000)
                    // Calculate durationMillis for remaining time calculations.
                    val durationMillis =
                        (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000)
                    // Calculate the new remaining time.
                    var new = millis3 - durationMillis

                    // Format the calculated difference.
                    val differenceFormatted = form(new.toLong())


                    Log.d("YourTag", "Completed Time: $differenceFormatted")

                    // Update the value of "timeRemaining" and "completedHours" fields in the document.
                    document.reference.update("timeRemaining", currentTimeFormatted)
                    document.reference.update("completedHours", differenceFormatted)

                    val catname = findViewById<TextView>(R.id.category_name)

                    val db = Firebase.firestore
                    val categoryRef =
                        db.collection("CategoryStorage").document(catname.text.toString())
                    // Retrieve the category document and perform updates.
                    categoryRef.get()
                        .addOnSuccessListener {
                            // Split and parse hour, minute, and second components of the calculated difference.
                            val timeComponents = differenceFormatted.split(":")
                            val hours = timeComponents[0].toInt()
                            val minutes = timeComponents[1].toInt()
                            val seconds = timeComponents[2].toInt()
                            val totalMinutes = hours * 60 + minutes + (seconds / 60)
                            Log.d("total", totalMinutes.toString())
                            // Update the "totalTimeCompleted" field in the category document.
                            categoryRef.update("totalTimeCompleted", totalMinutes.toString())
                        }
                }
            }
    }


    private fun String.toMillis(): Long {
        val parts = this.split(":")
        val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0
        return ((hours * 3600) + (minutes * 60)) * 1000
    }


    // Pauses the breaks timer, updates the view, and performs database operations to store the remaining break time.
    private fun pauseBreaks() {
        // Cancel the breaks timer and update breaksRunning and the view.
        timerBreaks!!.cancel()
        breaksRunning = false
        updateViewBreak()
        //Retrieve the remaining break time
        val currentTime = remainingTimeBreak
        Log.d("YourTag", "PauseDurationBreaks Retrieved: $currentTime")
        // Retrieve the formatted current time.
        val currentTimeFormatted = calculationsTask()
        Log.d("YourTag", "PauseDurationBreaks Retrieved: $currentTimeFormatted")
        // Retrieve the itemId from the intent.
        val itemId = intent.getStringExtra("itemId")
        val db = FirebaseFirestore.getInstance()
        // Retrieve the current user's ID.
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        // Perform database operations.
        db.collection("TaskStorage")
            .whereEqualTo("userIdTask", userid.toString().trim())
            .whereEqualTo("taskName", itemId)
            .get()
            .addOnSuccessListener { queryResult ->
                // Get the document object
                val document = queryResult.documents.lastOrNull()
                if (document != null) {
                    // Retrieve the break duration from the document.
                    val breakDurationVar = document.get("breakDurations")
                    Log.d("YourTag", "PauseDuration Retrieved: $breakDurationVar")
                    // Calculate minutes and seconds from the remaining break time.
                    val minutes = currentTime / 60000
                    val seconds = (currentTime % 60000) / 1000
                    // Format the time as a string.
                    val timeString = String.format("%02d:%02d", minutes, seconds)
                    Log.d("totalmins", seconds.toString())
                    // Update the "breakDurations" field in the document with the remaining break time.
                    document.reference.update("breakDurations", timeString)
                }
            }
    }

    // Extension function to format milliseconds as a time string

    //resets the task timer
    private fun reset() {
        remainingTime = startTime
        updateText()
        updateViewTask()
    }


    //Formats the time in mills to a hour min and seconds format
    private fun form(millis: Long): String {
        val hours = (millis / 1000) / 3600
        val minutes = ((millis / 1000) / 60) % 60
        val seconds = (millis / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    //Resets the break timer when the button is pressed
    private fun resetBreak() {
        remainingTimeBreak = startTimeBreaks
        updateTimerBreaks()
        updateViewBreak()
    }


    //The function calculates the remaining time in hours, minutes, and seconds from a variable for the task timer
    private fun updateText() {
        val hours = (remainingTime / 1000).toInt() / 3600
        val minutes = ((remainingTime / 1000) % 3600).toInt() / 60
        val seconds = (remainingTime / 1000).toInt() % 60
        val timeLeftFormatted: String = if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        taskCountDown!!.text = timeLeftFormatted
    }

    //The function calculates the remaining time in hours, minutes, and seconds from a variable for the break timer
    private fun updateTimerBreaks() {
        val hours = (remainingTimeBreak / 1000).toInt() / 3600
        val minutes = ((remainingTimeBreak / 1000) % 3600).toInt() / 60
        val seconds = (remainingTimeBreak / 1000).toInt() % 60
        val timeLeftFormatted: String = if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
        breakCount!!.text = timeLeftFormatted
    }

    //Method to controll the visability of the buttons for the task timer
    private fun updateViewTask() {
        when {
            taskRunning -> {
                stop!!.visibility = View.INVISIBLE
                restart!!.visibility = View.INVISIBLE
                playPause?.setImageResource(R.drawable.pause_icon)

            }

            else -> {

                stop!!.visibility = View.VISIBLE
                playPause?.setImageResource(R.drawable.play_icon)

                when {
                    remainingTime < 1000 -> {
                        playPause!!.visibility = View.INVISIBLE
                    }

                    else -> {
                        playPause!!.visibility = View.VISIBLE
                    }
                }
                when {
                    remainingTime < startTime -> {
                        restart!!.visibility = View.VISIBLE
                    }

                    else -> {
                        restart!!.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    //Method to controll the visability of the buttons for the break timer
    private fun updateViewBreak() {
        when {
            breaksRunning -> {
                breakStop!!.visibility = View.INVISIBLE
                restartBreak!!.visibility = View.INVISIBLE
                playPauseBreak?.setImageResource(R.drawable.pause_icon)
            }

            else -> {
                breakStop!!.visibility = View.VISIBLE
                playPauseBreak?.setImageResource(R.drawable.play_icon)
                when {
                    remainingTimeBreak < 1000 -> {
                        playPauseBreak!!.visibility = View.INVISIBLE
                    }

                    else -> {
                        playPauseBreak!!.visibility = View.VISIBLE
                    }
                }
                when {
                    remainingTimeBreak < startTimeBreaks -> {
                        restartBreak!!.visibility = View.VISIBLE
                    }

                    else -> {
                        restartBreak!!.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }


    //Method to that triggers when the timer is stoped it is stored in SharedPreferences
    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong("startTimeInMillis", startTime)
        editor.putLong("millisLeft", remainingTime)
        editor.putBoolean("timerRunning", taskRunning)
        editor.putLong("endTime", finish)
        editor.apply()
        when {
            timer == null && !taskRunning -> {
                timer?.cancel()
            }
        }
    }

    fun onStopBreak() {

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong("startTimeInMillisBreak", startTimeBreaks)
        editor.putLong("millisLeftBreak", remainingTimeBreak)
        editor.putBoolean("timerRunningBreak", breaksRunning)
        editor.putLong("endTimeBreak", finishBreaks)
        editor.apply()
        when {
            timerBreaks != null -> {
                timerBreaks!!.cancel()
            }
        }
    }

    //Method to that triggers when the timer is started it is stored in SharedPreferences
    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        startTime = prefs.getLong("startTimeInMillis", 600000)
        remainingTime = prefs.getLong("millisLeft", startTime)
        taskRunning = prefs.getBoolean("timerRunning", false)
        updateText()
        updateViewTask()
        when {
            taskRunning -> {
                finish = prefs.getLong("endTime", 0)
                remainingTime = finish - System.currentTimeMillis()
                when {
                    remainingTime < 0 -> {
                        remainingTime = 0
                        taskRunning = false
                        updateText()
                        updateViewTask()
                    }

                    else -> {
                        startTimer()
                    }
                }
            }
        }
    }

    fun onStartBreak() {

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        startTimeBreaks = prefs.getLong("startTimeInMillisBreak", 600000)
        remainingTimeBreak = prefs.getLong("millisLeftBreak", startTimeBreaks)
        breaksRunning = prefs.getBoolean("timerRunningBreak", false)
        updateTimerBreaks()
        updateViewBreak()
        when {
            breaksRunning -> {
                finishBreaks = prefs.getLong("endTimeBreak", 0)
                remainingTimeBreak = finishBreaks - System.currentTimeMillis()
                when {
                    remainingTimeBreak < 0 -> {
                        remainingTimeBreak = 0
                        breaksRunning = false
                        updateTimerBreaks()
                        updateViewBreak()
                    }

                    else -> {
                        beginBreak()
                    }
                }
            }
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
                val intent = Intent(this@TaskPage, Login::class.java)
                intent.putExtra("login", R.layout.login)
                overridePendingTransition(0, 0)
                startActivity(intent)

            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {


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
                                    AppSettings.Preloads.userSName = null
                                    val intent = Intent(this@TaskPage, Login::class.java)
                                    intent.putExtra("login", R.layout.login)
                                    overridePendingTransition(0, 0)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    //Method to get the breaks value from the database
    private fun getBreaks() {
        val itemId = intent.getStringExtra("itemId")
        Log.d("YourTag", "ItemID: $itemId")
        //THIS INDEX LETS THE FOR LOOP SORT THE SPECIFIC INDEX WHICH WONT CHANGE AS PER THE POSITION WHICH WILL CHANGE
        val db = FirebaseFirestore.getInstance()
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("YourTag", "userid: $userid")
        // Query Firestore to get the data for the clicked item
        db.collection("TaskStorage")
            .whereEqualTo("userIdTask", userid.toString().trim())
            .whereEqualTo("taskName", itemId)
            .get()
            .addOnSuccessListener { documents ->
                // Get the last document in the result, which corresponds to the clicked item
                val document = documents.documents.lastOrNull()
                if (document != null) {
                    // Get the data for the clicked item from the document
                    // Use the data from Firestore to populate the fields in your form
                    val breakVal = document.get("breakDurations")
                    val breakval = breakVal.toString()
                    val breakParts = breakval?.split(":")
                    val breakValInMilliseconds = when (breakParts?.size) {
                        2 -> {
                            val minutes = breakParts[0].toLongOrNull() ?: 0
                            val seconds = breakParts[1].toLongOrNull() ?: 0
                            minutes * 60000 + seconds * 1000
                        }

                        1 -> {
                            val minutes = breakval.toLongOrNull() ?: 0
                            minutes * 60000
                        }

                        else -> 0
                    }
                    Log.d("YourTag", "BreakVal: $breakValInMilliseconds")
                    startTimeBreaks(breakValInMilliseconds)

                }
            }
    }

    //This method gets the values from the database and displaies them in the text fields. It is also responsible for getting the value using by the task timer
    private fun taskPopulation() {

        try {
            // Retrieve references to various TextView and ImageView UI elements used in the task details screen.
            val tName = findViewById<TextView>(R.id.task_name)
            val catname = findViewById<TextView>(R.id.category_name)
            val desc = findViewById<TextView>(R.id.description_text)
            val sDate = findViewById<TextView>(R.id.start_date_display)
            val eDate = findViewById<TextView>(R.id.end_date_display)
            val hours2 = findViewById<TextView>(R.id.hour_text)
            val min = findViewById<TextView>(R.id.min_text)
            val max = findViewById<TextView>(R.id.max_text)
            val date = findViewById<TextView>(R.id.date_display)
            val taskImage = findViewById<ImageView>(R.id.task_image)


            val itemId = intent.getStringExtra("itemId")
            //THIS INDEX LETS THE FOR LOOP SORT THE SPECIFIC INDEX WHICH WONT CHANGE AS PER THE POSITION WHICH WILL CHANGE


            val db = FirebaseFirestore.getInstance()


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
                        // Get the data for the clicked item from the document

                        // Use the data from Firestore to populate the fields in your form
                        tName.text = document.getString("taskName")
                        catname.text = document.getString("categoryName")
                        desc.text = document.getString("description")
                        sDate.text = document.getString("starTime")
                        eDate.text = document.getString("endTime")
                        hours2.text = document.getString("duration")
                        val timeRem = document.getString("timeRemaining")
                        Log.d("YourTag", "Duration: " + hours2.text)

                        min.text = document.getString("minGoal")
                        max.text = document.getString("maxGoal")
                        date.text = document.getString("dateAdded")
                        val url = document.getString("imageURL")
                        //The below code gets the duration field in the database and then spits it up into three parts
                        val durationParts = timeRem?.split(":")
                        val durationInMillis = when (durationParts?.size) {
                            3 -> {
                                val hours = durationParts[0].toLong()
                                val minutes = durationParts[1].toLong()
                                val seconds = durationParts[2].toLong()
                                hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000
                            }

                            2 -> {
                                val hours = durationParts[0].toLong()
                                val minutes = durationParts[1].toLong()
                                hours * 60 * 60 * 1000 + minutes * 60 * 1000
                            }

                            else -> 0
                        }
                        //sets the Task timer to the value of the duration in millis
                        startTimeTask(durationInMillis)


                        Glide.with(this)
                            .load(url)
                            .into(taskImage)


                    }
                }


        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    private val galleryContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { url: Uri? ->


            when {
                url != null -> {
                    val task = findViewById<TextView>(R.id.task_name)

                    val imageView = findViewById<ImageView>(R.id.task_image)
                    imageView.setImageURI(url)

                    val store = Firebase.storage.reference.child(task.text.toString().trim())


                    val choice = store.putFile(url)
                    choice.addOnSuccessListener {

                        val message = "IMAGE UPLOADED ,PLEASE RESTART THE APP"
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        cacheClosure(this)
                    }.addOnFailureListener {

                    }
                }
            }
        }

    //Takes a picture using the device's camera, displays it in an ImageView, and uploads it to Firebase Storage.
    private val camera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo: Bitmap? ->

            val task = findViewById<TextView>(R.id.task_name)

            val imageView = findViewById<ImageView>(R.id.task_image)
            imageView.setImageBitmap(photo)


            val imageRef = Firebase.storage.reference.child(task.text.toString().trim())

            val stream = ByteArrayOutputStream()
            photo?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val data = stream.toByteArray()

            val uploadDP = imageRef.putBytes(data)
            uploadDP.addOnSuccessListener {

                val message = "IMAGE UPLOADED ,PLEASE RESTART THE APP"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                cacheClosure(this)

            }.addOnFailureListener {
                val message = "INVALID IMAGE!"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }


    //Deletes all files and directories within the cache directory of the given context.
    private fun cacheClosure(context: Context) {
        try {
            val location: File = context.cacheDir
            appFiles(location)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Recursively deletes all files and directories under the given directory.
    private fun appFiles(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val sub: Array<String> = dir.list() as Array<String>
            for (i in sub.indices) {
                val deleted = appFiles(File(dir, sub[i]))
                if (!deleted) {
                    return false
                }
            }
        }
        return dir?.delete() ?: false
    }


}
