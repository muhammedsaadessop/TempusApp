package com.example.tempus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

// this is the home page where the summary is stored
// tasks are found under categories and each leads to its own pages
class Home : AppCompatActivity() {
    private var selectedTabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val homeID = intent.getIntExtra("home", 0)
        val homeLayout = layoutInflater.inflate(homeID, null)
        setContentView(homeLayout)
        val refresh = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Home, Home::class.java)
                intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, refresh)
        securGuard()
        populateFields()


        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        when {
            sharedPreferences.getBoolean("isFirstLogin", true) -> {
                loggedOnNotification()
                sharedPreferences.edit().putBoolean("isFirstLogin", false).apply()
            }
        }


        FirebaseApp.initializeApp(this)
        print()
        // the assigning of the buttons
        val addbtn = findViewById<ImageButton>(R.id.addbtn)
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)

        val cat = findViewById<Button>(R.id.category_selected)
        val task = findViewById<Button>(R.id.task_selected)
        task.setOnClickListener() {
            //changes page to home
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        cat.setOnClickListener() {
            // changes to tasks
            val tasks = Intent(this, Tasks::class.java)
            startActivity(tasks)
            overridePendingTransition(0, 0)
            finish()

        }
        homebtn.setOnClickListener {
// changes to home
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()


        }

        breaksbtn.setOnClickListener {
            // changes to breaks
            val breakspage = Intent(this, Breaks::class.java)
            startActivity(breakspage)
            overridePendingTransition(0, 0)
            finish()

        }

        statsbtn.setOnClickListener {
            // changes to statistics
            val statspage = Intent(this, Statistics::class.java)
            startActivity(statspage)
            overridePendingTransition(0, 0)
            finish()

        }

        settingsbtn.setOnClickListener {
            // changes to settings page
            val settingspage = Intent(this, AppSettings::class.java)
            startActivity(settingspage)
            overridePendingTransition(0, 0)
            finish()

        }

        addbtn.setOnClickListener() {

            // changes to quick view
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


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the selected tab index
        outState.putInt("selectedTabIndex", selectedTabIndex)
    }

    private fun securGuard() {
// this checks user tokens
        // if invalid forces the user to login again
        // if deleted forces the user out of the app
        val tempusSecurity = FirebaseAuth.getInstance()
        tempusSecurity.addAuthStateListener { firebaseAuth ->
            when (firebaseAuth.currentUser) {
                null -> {

                    val sharedPreferences =
                        getSharedPreferences("preferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                    AppSettings.Preloads.userSName = null
                    val intent = Intent(this@Home, Login::class.java)
                    intent.putExtra("login", R.layout.login)
                    overridePendingTransition(0, 0)
                    startActivity(intent)

                }
            }
        }

        val tempusUsers = FirebaseAuth.getInstance().currentUser
        tempusUsers?.reload()?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    // stuff to do

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
                                    val intent = Intent(this@Home, Login::class.java)
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


    object TaskClass {
        private const val rows = 10
        private const val columns = 12

        val tasks = Array(rows) { arrayOfNulls<String>(columns) }
        val hours = Array(rows) { arrayOfNulls<String>(columns) }


    }



    private var animationsPlayed = false
    private fun print() {
        val recyclerview = findViewById<RecyclerView>(R.id.mRecycler_category)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val db = FirebaseFirestore.getInstance()
        val itemsRef = db.collection("CategoryStorage")
        val cv: CardView = findViewById(R.id.interval_cardview)
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        val categoryQuery = itemsRef.whereEqualTo("userIdCat", userid.toString().trim())
        val task = categoryQuery.get()

        task.addOnSuccessListener { documents ->

            val items = mutableListOf<ItemsViewModel>()

            for (document in documents) {
                val name = document.getString("categoryID") ?: ""
                val description = document.getString("totalHours") ?: ""
                val item = ItemsViewModel(name, description)
                items.add(item)

val close = findViewById<ImageButton>(R.id.interval_btn_exit)

                val checkTime = 300000L // Update progress every 300000 milliseconds (5 minutes)
                val db = Firebase.firestore
                val categoryRef = db.collection("CategoryStorage").document(name)
                categoryRef.get().addOnSuccessListener { document ->
                    try {

                        val totalHours = findViewById<ProgressBar>(R.id.progress_category)
                        val categoryHours = document.get("totalTimeCompleted").toString().toInt()
                        Log.d("stuff23", categoryHours.toString())

                        val loop = Handler(Looper.getMainLooper())

                        val updater = object : Runnable {
                            override fun run() {
                                val quarter = when (totalHours.progress) {
                                    totalHours.max / 4 -> "25"
                                    totalHours.max / 2 -> "50"
                                    totalHours.max * 3 / 4 -> "75"
                                    totalHours.max -> "100"
                                    else -> ""
                                }

                                val taskPreference =
                                    getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
                                val taskPlayed = taskPreference.getStringSet(
                                    "${name}_played_quarters",
                                    setOf()
                                )
                                val catAnimation = taskPlayed?.contains(quarter)



                                if (totalHours.progress != null && totalHours.progress != 0) {
                                    when (totalHours.progress) {

                                        totalHours.max / 4 -> {
                                            if (!catAnimation!!) {

                                                animationsPlayed = true

                                                cv.findViewById<TextView>(R.id.interval_title_first).text =
                                                    name
                                                cv.findViewById<TextView>(R.id.interval_title_second).text =
                                                    "is 25% completed. \n" +
                                                            " \n" +
                                                            " WELL DONE!!!"
                                                cv.findViewById<ImageView>(R.id.interval_icon)
                                                    .setImageResource(R.drawable.twentyfive)
                                                cv.visibility = View.VISIBLE
                                                val animation = AnimationUtils.loadAnimation(
                                                    this@Home,
                                                    R.anim.transition_one
                                                )
                                                cv.startAnimation(animation)
                                                val editor = taskPreference.edit()
                                                editor.putStringSet(
                                                    "${name}_played_quarters",
                                                    taskPlayed + quarter
                                                )
                                                editor.apply()
                                                close.setOnClickListener()
                                                {cv.visibility = View.GONE

                                                }


                                            }

                                        }

                                        totalHours.max / 2 -> {
                                            if (!catAnimation!!) {

                                                cv.findViewById<TextView>(R.id.interval_title_first).text =
                                                    name
                                                cv.findViewById<TextView>(R.id.interval_title_second).text =
                                                    "is 50% completed. \n" +
                                                            " \n" +
                                                            " WELL DONE!!!"
                                                cv.findViewById<ImageView>(R.id.interval_icon)
                                                    .setImageResource(R.drawable.fifty)
                                                cv.visibility = View.VISIBLE
                                                val animation = AnimationUtils.loadAnimation(
                                                    this@Home,
                                                    R.anim.transition_two
                                                )
                                                cv.startAnimation(animation)
                                                val editor = taskPreference.edit()
                                                editor.putStringSet(
                                                    "${name}_played_quarters",
                                                    taskPlayed + quarter
                                                )
                                                editor.apply()
                                                close.setOnClickListener()
                                                {cv.visibility = View.GONE

                                                }

                                            }
                                        }

                                        totalHours.max * 3 / 4 -> {
                                            if (!catAnimation!!) {
                                                cv.findViewById<TextView>(R.id.interval_title_first).text =
                                                    name
                                                cv.findViewById<TextView>(R.id.interval_title_second).text =
                                                    "is 75% completed. \n" +
                                                            " \n" +
                                                            " WELL DONE!!!"
                                                cv.findViewById<ImageView>(R.id.interval_icon)
                                                    .setImageResource(R.drawable.seventyfive)
                                                cv.visibility = View.VISIBLE
                                                val animation = AnimationUtils.loadAnimation(
                                                    this@Home,
                                                    R.anim.transition_three
                                                )
                                                cv.startAnimation(animation)

                                                val editor = taskPreference.edit()
                                                editor.putStringSet(
                                                    "${name}_played_quarters",
                                                    taskPlayed + quarter
                                                )
                                                editor.apply()
                                                close.setOnClickListener()
                                                {cv.visibility = View.GONE

                                                }
                                            }
                                        }

                                        totalHours.max -> {
                                            if (!catAnimation!!) {
                                                cv.visibility = View.VISIBLE
                                                cv.findViewById<TextView>(R.id.interval_title_first).text =
                                                    name
                                                cv.findViewById<TextView>(R.id.interval_title_second).text =
                                                    "is completed. \n" +
                                                            " \n" +
                                                            " WELL DONE!!!"
                                                cv.findViewById<ImageView>(R.id.interval_icon)
                                                    .setImageResource(R.drawable.hundred)
                                                val animation = AnimationUtils.loadAnimation(
                                                    this@Home,
                                                    R.anim.transition_four
                                                )
                                                cv.startAnimation(animation)
                                                val editor = taskPreference.edit()
                                                editor.putStringSet(
                                                    "${name}_played_quarters",
                                                    taskPlayed + quarter
                                                )
                                                editor.apply()
                                                close.setOnClickListener()
                                                {cv.visibility = View.GONE

                                                }
                                            }
                                        }
                                    }
                                }


                                // Schedule the next update
                                loop.postDelayed(this, checkTime)
                            }
                        }
                        loop.post(updater)
                    } catch (e: Exception) {
                        //ddf
                    }

                }

            }
            try {


                var sortedItems = items.sortedBy { it.text }

                val adapter = CustomAdapter(sortedItems as MutableList<ItemsViewModel>)
                adapter.onTaskClickListener = { _ ->

                }
                recyclerview.adapter = adapter

                val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                    0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        val item = sortedItems[position]
                        val mutableItems = sortedItems.toMutableList()

                        if (direction == ItemTouchHelper.RIGHT) {
                            // Swipe to the right (edit action)
                            val intent = Intent(this@Home, EditCategoryActivity::class.java)
                            intent.putExtra(
                                "item_id", item.text
                            ) // Pass the item ID or relevant data to the EditActivity
                            startActivity(intent)
                            recreate()
                        } else if (direction == ItemTouchHelper.LEFT) {

                            // Delete the item from the database
                            val databaseRef = itemsRef.document(item.text)
                            databaseRef.delete().addOnSuccessListener {

                                mutableItems.removeAt(position)
                                sortedItems = mutableItems
                                adapter.notifyItemRemoved(position)

                                Toast.makeText(
                                    this@Home, "Removed successfully", Toast.LENGTH_SHORT

                                ).show()
                                recreate()
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    this@Home,
                                    "Failed to remove: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }


                    // Add swipe action buttons
                    override fun onChildDraw(
                        c: Canvas,
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        dX: Float,
                        dY: Float,
                        actionState: Int,
                        isCurrentlyActive: Boolean
                    ) {
                        super.onChildDraw(
                            c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                        )

                        val itemView = viewHolder.itemView
                        if (dX > 50) {

                            when {
                                dX > 0 -> {
                                    // Swiping to the right (edit action)
                                    val editIcon = ContextCompat.getDrawable(
                                        this@Home, R.drawable.edit_icon
                                    )
                                    val editIconMargin =
                                        (itemView.height - editIcon?.intrinsicHeight!!) / 2
                                    val editIconTop = itemView.top + editIconMargin
                                    val editIconBottom = editIconTop + editIcon.intrinsicHeight
                                    val editIconLeft = itemView.left + editIconMargin
                                    val editIconRight =
                                        itemView.left + editIconMargin + editIcon.intrinsicWidth

                                    editIcon.setBounds(
                                        editIconLeft, editIconTop, editIconRight, editIconBottom
                                    )

                                    val editBackground = ContextCompat.getDrawable(
                                        this@Home, R.drawable.edit_button_background
                                    )
                                    editBackground?.setBounds(
                                        itemView.left,
                                        itemView.top,
                                        itemView.left + dX.toInt(),
                                        itemView.bottom
                                    )
                                    editBackground?.draw(c)
                                    editIcon.draw(c)
                                }
                            }

                        } else if (dX < -50) {


                            // Swiping to the left (delete action)
                            val deleteIcon =
                                ContextCompat.getDrawable(this@Home, R.drawable.delete_icon)
                            val deleteIconMargin =
                                (itemView.height - deleteIcon?.intrinsicHeight!!) / 2
                            val deleteIconTop = itemView.top + deleteIconMargin
                            val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight
                            val deleteIconLeft =
                                itemView.right - deleteIconMargin - deleteIcon.intrinsicWidth
                            val deleteIconRight = itemView.right - deleteIconMargin

                            deleteIcon.setBounds(
                                deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom
                            )

                            val deleteBackground = ContextCompat.getDrawable(
                                this@Home, R.drawable.delete_button_background
                            )
                            deleteBackground?.setBounds(
                                itemView.right + dX.toInt(),
                                itemView.top,
                                itemView.right,
                                itemView.bottom
                            )
                            deleteBackground?.draw(c)
                            deleteIcon.draw(c)
                        }
                    }
                }


                val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
                itemTouchHelper.attachToRecyclerView(recyclerview)

            } catch (e: Exception) {
                //sds
            }
        }
    }


    data class Task(val name: String, val hours2: String)

    data class ItemsViewModel(val text: String, val hours: String)
    data class store(
        var numbers: Int = 0
    )

    fun notifs(context: Context) {


    }

    private var num = 0

    class CustomAdapter(private val catList: MutableList<ItemsViewModel> = mutableListOf()) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        val s = store()
        var onTaskClickListener: ((Task) -> Unit)? = null
        private var totalItemCount = 0

        fun setTotalItemCount(count: Int) {
            this.totalItemCount = count
            s.numbers = count
            Log.d("stuf3", s.numbers.toString())
            notifyDataSetChanged()
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemsViewModel = catList[position]
            holder.textView.text = itemsViewModel.text
            holder.textView2.text = itemsViewModel.hours
            val timeComponents = itemsViewModel.hours.split(":")
            val hours = timeComponents[0].toInt()
            val minutes = timeComponents[1].toInt()
            val totalMinutes = hours * 60 + minutes
            holder.progressBar.max = totalMinutes
            val db = Firebase.firestore
            val tasksRef = db.collection("TaskStorage")
            val category = itemsViewModel.text
            Log.d("stuff5", itemsViewModel.text.toString())
            val categoryRef =
                db.collection("CategoryStorage").document(itemsViewModel.text.toString())
            categoryRef.get().addOnSuccessListener { document ->

try {


    val categoryHours = document.get("totalTimeCompleted").toString().toInt()
    holder.progressBar.progress = categoryHours
}
catch (e:Exception)
{
    /////ddfdfd
}
            }
            tasksRef.whereEqualTo("categoryName", category).get()
                .addOnSuccessListener { documents ->
                    val count = documents.size()
                    holder.textView3.text = "Tasks:${count.toString()}"


                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }



            holder.itemView.setOnClickListener {

                when (holder.tasksLayout.visibility) {
                    View.GONE -> {
                        holder.tasksLayout.visibility = View.VISIBLE
                        populateTasks(holder, itemsViewModel.text)
                    }

                    else -> {
                        holder.tasksLayout.visibility = View.GONE
                    }
                }
            }


        }


        override fun getItemCount(): Int {
            return catList.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = this.itemView.findViewById(R.id.mTitle)
            val textView2: TextView = this.itemView.findViewById(R.id.mHours_category)
            val tasksLayout: LinearLayout = this.itemView.findViewById(R.id.tasksLayout)
            val progressBar: ProgressBar = this.itemView.findViewById(R.id.progress_category)
            val textView3: TextView = this.itemView.findViewById(R.id.mNum_tasks)
        }

        private fun populateTasks(holder: ViewHolder, categoryTask: String) {

            val db = FirebaseFirestore.getInstance()
            val tasksRef = db.collection("TaskStorage")
            val userid = FirebaseAuth.getInstance().currentUser?.uid


            val catQuery = tasksRef.whereEqualTo("userIdTask", userid)
                .whereEqualTo("categoryName", categoryTask)

            val task = catQuery.get()


            task.addOnSuccessListener { documents ->
                val tasksByTab = mutableMapOf<String, MutableList<Task>>()
                for (document in documents) {
                    // Get the data for each document
                    val name = document.getString("taskName") ?: ""
                    val hours = document.getString("duration") ?: ""

                    val tabName = document.getString("tabID") ?: ""

                    val task = Task(name, hours)
                    when {
                        tasksByTab.containsKey(tabName) -> {
                            tasksByTab[tabName]?.add(task)

                        }

                        else -> {
                            tasksByTab[tabName] = mutableListOf(task)


                        }
                    }

                }
                var totalItemCount = 0


                holder.tasksLayout.removeAllViews()

                for ((_, tasks) in tasksByTab) {

                    val displayTab = TextView(holder.itemView.context)
                    displayTab.setTypeface(null, Typeface.BOLD)
                    holder.tasksLayout.addView(displayTab)
                    val newView = RecyclerView(holder.itemView.context)
                    newView.layoutManager = LinearLayoutManager(holder.itemView.context)
                    val subAdapter = SubTasksAdapter(tasks)
                    newView.adapter = subAdapter
                    totalItemCount += subAdapter.getItemCount()
                    Log.d("stuff", subAdapter.getItemCount().toString())
                    val e = store()
                    e.numbers = totalItemCount
                    val custom = CustomAdapter()
                    custom.setTotalItemCount(totalItemCount)
                    Log.d("STUFF2", e.numbers.toString())
                    holder.tasksLayout.addView(newView)
                }

            }

        }


    }


    class SubTasksAdapter(private val tasks: List<Task>) :
        RecyclerView.Adapter<SubTasksAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.mTitle3)
            val textView2: TextView = itemView.findViewById(R.id.mHpurs2)


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.subtask, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            holder.textView.text = task.name
            holder.textView2.text = task.hours2

            holder.itemView.setOnClickListener() {
                val clickedData = tasks[position]
                val context = holder.itemView.context
                val homeIntent = Intent(context, TaskPage::class.java)
                homeIntent.putExtra("task", clickedData.name)
                homeIntent.putExtra("duration", clickedData.hours2)
                val bundle = Bundle()
                val bundle2 = Bundle()
                bundle.putSerializable("myDataList", TaskClass.tasks)
                homeIntent.putExtra("position1", position) // use "position" as the key
                homeIntent.putExtras(bundle)
                bundle2.putSerializable("Hours", TaskClass.hours)
                homeIntent.putExtra("position2", position) // use "position" as the key
                homeIntent.putExtras(bundle2)
                homeIntent.putExtra("itemId", clickedData.name)
                context.startActivity(homeIntent)
            }

        }

        override fun getItemCount(): Int {


            return tasks.size


        }
    }


    private fun loggedOnNotification() {
        val logIntent = Intent(this, AppSettings::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val settingsIntent =
            PendingIntent.getActivity(this, 0, logIntent, PendingIntent.FLAG_MUTABLE)

        val user = FirebaseAuth.getInstance().currentUser?.email
        val channelId = "login"
        val channelName = "Loginuser"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance)

        val tempusManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        tempusManager.createNotificationChannel(notificationChannel)

        val builder = NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.user)
            .setContentTitle("$user logged in").setContentText("welcome to tempus $user")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT).setContentIntent(settingsIntent)
            .setAutoCancel(true)


        val unverifiedBuilder =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.user)
                .setContentTitle("$user logged in")
                .setContentText("welcome to tempus new $user please verify your account")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT).setContentIntent(settingsIntent)
                .setAutoCancel(true)


        val userid = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database
        val myRef = database.getReference("UserDetails")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {
                    val documentName = ds.key

                    when {
                        documentName == userid.toString().trim() -> {
                            tempusManager.notify(0, builder.build())


                        }

                        AppSettings.Preloads.userSName.isNullOrEmpty() -> {

                            tempusManager.notify(1, unverifiedBuilder.build())
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

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
                            AppSettings.Preloads.names =
                                data.child("firstname").getValue(String::class.java).toString()
                            AppSettings.Preloads.emails =
                                data.child("emailaddress").getValue(String::class.java).toString()
                            AppSettings.Preloads.surname =
                                data.child("surname").getValue(String::class.java).toString()
                            AppSettings.Preloads.userSName =
                                data.child("displayname").getValue(String::class.java).toString()
                            AppSettings.Preloads.conPass =
                                data.child("confirmkey").getValue(String::class.java).toString()
                            AppSettings.Preloads.pass =
                                data.child("password").getValue(String::class.java).toString()


                        }
                    }


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // stuff to do

            }
        })
    }
}