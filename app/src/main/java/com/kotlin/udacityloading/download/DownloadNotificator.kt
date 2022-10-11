package com.kotlin.udacityloading.download

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.kotlin.udacityloading.R
import com.kotlin.util.ext.createDownloadStatusChannel
import com.kotlin.util.ext.getNotificationManager
import com.kotlin.util.ext.sendDownloadCompletedNotification

class DownloadNotificator(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    init {
        lifecycle.addObserver(this).also {
        }
    }

    fun notify(
        fileName: String,
        downloadStatus: DownloadStatus,
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                context.getString(R.string.download_completed),
                Toast.LENGTH_SHORT
            ).show();
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createDownloadStatusChannel(applicationContext)
                sendDownloadCompletedNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

}