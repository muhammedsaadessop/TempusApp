package com.example.tempus

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.widget.RemoteViews

class TempusWidget : AppWidgetProvider() {

    companion object {
        const val timer = "com.example.tempus.timer"
        const val timer2 = "com.example.tempus.timer2"
        const val id = "widgetId"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val remoteViews = RemoteViews(context.packageName, R.layout.tempus_widget)


            val timerintent = Intent(context, TempusWidget::class.java).apply {
                action = timer
                putExtra(id, appWidgetId)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                timerintent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.button_start, pending)

            val intentStop = Intent(context, TempusWidget::class.java).apply {
                action = timer2
                putExtra(id, appWidgetId)
            }
            val timerStop = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intentStop,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.button_stop, timerStop)


            val timerprefernce = context.getSharedPreferences(
                "widget_preferences",
                Context.MODE_PRIVATE
            )
            val timerstyle = timerprefernce.getString("theme", "Light") ?: "Light"
            val fontSize = timerprefernce.getInt("font_size", 50)
            val defaultTime = timerprefernce.getLong("default_time", 10)

            val bc = when (timerstyle) {
                "Light" -> Color.WHITE
                "Dark" -> Color.GRAY
                "Colorful" -> Color.rgb(116, 69, 221)
                else -> Color.WHITE
            }
            remoteViews.setInt(R.id.widget_layout, "setBackgroundColor", bc)

            remoteViews.setFloat(R.id.text_view_countdown, "setTextSize", fontSize.toFloat())

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            timer -> {
                val ids =
                    intent.getIntExtra(id, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (ids != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val remoteViews =
                        RemoteViews(context.packageName, R.layout.tempus_widget)

                    val intentStop = Intent(context, TempusWidget::class.java).apply {
                        action = timer2
                        putExtra(id, ids)
                    }
                    val pendingIntentStop = PendingIntent.getBroadcast(
                        context,
                        ids,
                        intentStop,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    remoteViews.setOnClickPendingIntent(R.id.button_stop, pendingIntentStop)


                    remoteViews.setChronometer(
                        R.id.chronometer_stopwatch,
                        SystemClock.elapsedRealtime(),
                        null,
                        true
                    )

                    val tempmanagewr = AppWidgetManager.getInstance(context)
                    tempmanagewr.updateAppWidget(ids, remoteViews)
                }
            }
            timer2 -> {
                val tempus =
                    intent.getIntExtra(id, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (tempus != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val remoteViews =
                        RemoteViews(context.packageName, R.layout.tempus_widget)

                    val intentStart = Intent(context, TempusWidget::class.java).apply {
                        action = timer
                        putExtra(id, tempus)
                    }
                    val tStart = PendingIntent.getBroadcast(
                        context,
                        tempus,
                        intentStart,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    remoteViews.setOnClickPendingIntent(R.id.button_start, tStart)


                    remoteViews.setChronometer(
                        R.id.chronometer_stopwatch,
                        SystemClock.elapsedRealtime(),
                        null,
                        false
                    )

                    val awm = AppWidgetManager.getInstance(context)
                    awm.updateAppWidget(tempus, remoteViews)
                }
            }
        }
    }
}
