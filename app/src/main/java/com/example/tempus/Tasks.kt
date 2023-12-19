package com.example.tempus


import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style
import java.util.Calendar

class Tasks : AppCompatActivity() {
    object DateClass {
        //ASSIGNING TASKS
        var startDate = "dd/MM/yyyy"
        var endDate = "dd/MM/yyyy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_display)
        val homeBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Tasks, Home::class.java)
                intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()


            }
        }
        onBackPressedDispatcher.addCallback(this, homeBack)
        security()
        print()


        val cats = findViewById<Button>(R.id.task_selected)
        val date = findViewById<Button>(R.id.filterDates_btn)
        val addbtn = findViewById<ImageButton>(R.id.addbtn)
        val homebtn = findViewById<ImageButton>(R.id.hometbtn)
        val breaksbtn = findViewById<ImageButton>(R.id.breakstbtn)
        val statsbtn = findViewById<ImageButton>(R.id.statstbtn)
        val settingsbtn = findViewById<ImageButton>(R.id.settingstbtn)

        val cat = findViewById<Button>(R.id.category_selected)
        val task = findViewById<Button>(R.id.task_selected)
        task.setOnClickListener()
        {
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
        cat.setOnClickListener()
        {
            val tasks = Intent(this, Tasks::class.java)
            startActivity(tasks)
            overridePendingTransition(0, 0)
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

        date.setOnClickListener {
            val recyclerview = findViewById<RecyclerView>(R.id.mRecycler_task)
            recyclerview.layoutManager = LinearLayoutManager(this)
            val db = FirebaseFirestore.getInstance()
            val itemsRef = db.collection("TaskStorage")

            val userid = FirebaseAuth.getInstance().currentUser?.uid

            val dateQuery = itemsRef.whereEqualTo("userIdTask", userid.toString().trim())
                .whereGreaterThanOrEqualTo("dateAdded", DateClass.startDate)
                .whereLessThanOrEqualTo("dateAdded", DateClass.endDate)

            val task = dateQuery.get()

            task.addOnSuccessListener { documents ->

                val items = mutableListOf<ItemsViewModel>()
                if (documents.size() > 0) {

                    for (document in documents) {
                        val name = document.getString("taskName") ?: ""
                        val description = document.getString("duration") ?: ""
                        val sub = document.getString("categoryName") ?: ""
                        val date = document.getString("dateAdded") ?: ""
                        val imageUrl = document.getString("imageURL") ?: ""
                        val item = ItemsViewModel(name, description, sub, date, imageUrl)
                        items.add(item)
                    }

                    val sortedItems = items.sortedBy { it.date }
                    try {

                        val adapter = CustomAdapter(sortedItems.toMutableList())
                        recyclerview.adapter = adapter
                        adapter.notifyDataSetChanged()

                    } catch (e: Exception) {
                        // stuff to do


                    }
                } else {
                    val crouton =
                        Crouton.makeText(this, "NO TASKS WITHIN THIS RANGE FOUND", Style.INFO)
                    crouton.show()
                }

            }.addOnFailureListener()
            {
                val crouton = Crouton.makeText(
                    this,
                    "DATA ERROR:PLEASE CHECK CONNECTION OR CONTACT CUSTOMER SERVICES",
                    Style.ALERT
                )
                crouton.show()
            }
        }



        cat.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }

        cats.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.putExtra("home", getIntent().getIntExtra("home", R.layout.home))
            startActivity(intent)
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
                val intent = Intent(this@Tasks, Login::class.java)
                intent.putExtra("login", R.layout.login)
                overridePendingTransition(0, 0)
                startActivity(intent)

            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // stuff to do

            } else {
                val exception = task.exception
                if (exception is FirebaseAuthInvalidUserException) {
                    val errorCode = exception.errorCode
                    if (errorCode == "ERROR_USER_NOT_FOUND") {
                        val sharedPreferences =
                            getSharedPreferences("preferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("isFirstLogin", true).apply()
                        AppSettings.Preloads.userSName = null
                        val intent = Intent(this@Tasks, Login::class.java)
                        intent.putExtra("login", R.layout.login)
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    fun selectStartDate(view: View) {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->

                DateClass.startDate = "$dayOfMonth/${month + 1}/$year"
            }, year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    fun selectEndDate(view: View) {

        val taskCalendar = Calendar.getInstance()
        val taskYear = taskCalendar.get(Calendar.YEAR)
        val taskMonth = taskCalendar.get(Calendar.MONTH)
        val taskDayOfMonth = taskCalendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                DateClass.endDate = "$dayOfMonth/${month + 1}/$year"
            }, taskYear, taskMonth, taskDayOfMonth
        )
        datePickerDialog.show()
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

    private fun print() {
        val recyclerview = findViewById<RecyclerView>(R.id.mRecycler_task)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val db = FirebaseFirestore.getInstance()
        val itemsRef = db.collection("TaskStorage")


        val userid = FirebaseAuth.getInstance().currentUser?.uid


        val query = itemsRef.whereEqualTo("userIdTask", userid.toString().trim())


        val task = query.get()

        task.addOnSuccessListener { documents ->

            val items = mutableListOf<ItemsViewModel>()

            for (document in documents) {

                val name = document.getString("taskName") ?: ""
                val description = document.getString("duration") ?: ""
                val sub = document.getString("categoryName") ?: ""
                val date = document.getString("dateAdded") ?: ""
                val imageUrl = document.getString("imageURL") ?: ""
                val item = ItemsViewModel(name, description, sub, date, imageUrl)
                items.add(item)
            }


            val sortedItems = items.sortedBy { it.text }
            try {

                val adapter = CustomAdapter(sortedItems.toMutableList())
                recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()
                adapter.onTaskClickListener = { _ ->

                }
                recyclerview.adapter = adapter

                val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                    0,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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

                        if (direction == ItemTouchHelper.RIGHT) {
                            // Swipe to the right (edit action)
                            val intent = Intent(this@Tasks, EditTaskActivity::class.java)
                            intent.putExtra(
                                "item_id",
                                item.text
                            ) // Pass the item ID or relevant data to the EditActivity
                            startActivity(intent)
                            recreate()
                        } else if (direction == ItemTouchHelper.LEFT) {
                            // Swipe to the left (delete action)
                            val databaseRef = itemsRef.document(item.text)

                            databaseRef.delete()
                                .addOnSuccessListener {
                                    // Delete the item from the RecyclerView

                                    sortedItems.toMutableList().removeAt(position)
                                    adapter.notifyItemRemoved(position)

                                    Toast.makeText(
                                        this@Tasks,
                                        "Removed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    recreate()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@Tasks,
                                        "Failed to remove: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        recreate()
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
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )

                        val itemView = viewHolder.itemView
                        if (dX > 50) {


                            // Swiping to the right (edit action)
                            val editIcon =
                                ContextCompat.getDrawable(
                                    this@Tasks,
                                    R.drawable.edit_icon
                                )
                            val editIconMargin =
                                (itemView.height - editIcon?.intrinsicHeight!!) / 2
                            val editIconTop = itemView.top + editIconMargin
                            val editIconBottom = editIconTop + editIcon.intrinsicHeight
                            val editIconLeft = itemView.left + editIconMargin
                            val editIconRight =
                                itemView.left + editIconMargin + editIcon.intrinsicWidth

                            editIcon.setBounds(
                                editIconLeft,
                                editIconTop,
                                editIconRight,
                                editIconBottom
                            )

                            val editBackground = ContextCompat.getDrawable(
                                this@Tasks,
                                R.drawable.edit_button_background
                            )
                            editBackground?.setBounds(
                                itemView.left,
                                itemView.top,
                                itemView.left + dX.toInt(),
                                itemView.bottom
                            )
                            editBackground?.draw(c)
                            editIcon.draw(c)


                        } else if (dX < -50) {

                            imageDelete()
                            // Swiping to the left (delete action)
                            val deleteIcon =
                                ContextCompat.getDrawable(this@Tasks, R.drawable.delete_icon)
                            val deleteIconMargin =
                                (itemView.height - deleteIcon?.intrinsicHeight!!) / 2
                            val deleteIconTop = itemView.top + deleteIconMargin
                            val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight
                            val deleteIconLeft =
                                itemView.right - deleteIconMargin - deleteIcon.intrinsicWidth
                            val deleteIconRight = itemView.right - deleteIconMargin

                            deleteIcon.setBounds(
                                deleteIconLeft,
                                deleteIconTop,
                                deleteIconRight,
                                deleteIconBottom
                            )

                            val deleteBackground = ContextCompat.getDrawable(
                                this@Tasks,
                                R.drawable.delete_button_background
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
                // stuff to do aka error handling
            }
        }

    }


    data class ItemsViewModel(
        val text: String, val hours: String, val sub: String, var date: String, val imageUrl: String
    )

    class CustomAdapter(private var myDataList: MutableList<ItemsViewModel> = mutableListOf()) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        var onTaskClickListener: ((Home.Task) -> Unit)? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.tastk_tab, parent, false)
            return ViewHolder(view)

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemsViewModel = myDataList[position]
            Glide.with(holder.itemView).load(itemsViewModel.imageUrl).circleCrop()
                .into(holder.imageView)

            holder.textView.text = itemsViewModel.text
            holder.textView2.text = itemsViewModel.hours
            holder.textView3.text = itemsViewModel.sub

            holder.itemView.setOnClickListener {
                val clickedData = myDataList[position]
                val context = holder.itemView.context
                val intent = Intent(context, TaskPage::class.java)
                intent.putExtra("taskName", clickedData.text)
                intent.putExtra("duration", clickedData.hours)
                val bundle = Bundle()
                val bundle2 = Bundle()

                bundle.putSerializable("myDataList", Home.TaskClass.tasks)
                intent.putExtra("position1", position) // use "position" as the key
                intent.putExtras(bundle)
                bundle2.putSerializable("Hours", Home.TaskClass.hours)
                intent.putExtra("position2", position) // use "position" as the key
                intent.putExtras(bundle2)
                intent.putExtra("itemId", clickedData.text)
                context.startActivity(intent)

            }
        }

        override fun getItemCount(): Int {
            return myDataList.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.mTitle2)
            val textView2: TextView = itemView.findViewById(R.id.mHpurs)
            val textView3: TextView = itemView.findViewById(R.id.mSubtitle)
            val imageView: ImageView = itemView.findViewById(R.id.task_item_image)
        }


    }


}