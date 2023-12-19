package com.example.tempus

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TempusWidgetConfigureActivity : AppCompatActivity() {
    private lateinit var theme: Spinner
    private lateinit var seeksize: SeekBar
    private lateinit var fsize: TextView
    private lateinit var sub: Button
    private var aid = AppWidgetManager.INVALID_APPWIDGET_ID

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tempus_widget_configure)

        theme = findViewById(R.id.spinner_theme)
        seeksize = findViewById(R.id.seek_bar_font_size)
        fsize = findViewById(R.id.text_view_font_size)
        sub = findViewById(R.id.button_confirm)


        val ct = arrayOf("Light", "Dark", "Colorful")
        val tadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ct)
        tadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        theme.adapter = tadapter


        seeksize.progress = 50
        fsize.text = "50%"

        theme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val timus = parent?.getItemAtPosition(position) as String
                tpref(timus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        seeksize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fsize.text = "$progress%"
                fsizes(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })


        sub.setOnClickListener {
            config()
        }

        aid = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)!!
        if (aid == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
    }

    private fun tpref(theme: String) {

        val sharedPreferences = getSharedPreferences("widget_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("theme", theme)
        editor.apply()
    }

    private fun fsizes(fontSize: Int) {

        val sharedPreferences = getSharedPreferences("widget_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("font_size", fontSize)
        editor.apply()
    }


    private fun config() {

        val appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        TempusWidget.updateAppWidget(this, appWidgetManager, appWidgetId ?: return)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        setResult(RESULT_OK, resultValue)

        finish()
    }
}
