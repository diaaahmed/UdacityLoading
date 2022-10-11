package com.kotlin.udacityloading.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.net.toUri
import com.kotlin.udacityloading.R
import com.kotlin.udacityloading.databinding.ActivityMainBinding
import com.kotlin.udacityloading.loading.ButtonState
import com.kotlin.udacityloading.download.DownloadNotificator
import com.kotlin.udacityloading.download.DownloadStatus
import com.kotlin.util.ext.getDownloadManager


class MainActivity : AppCompatActivity() {

    private var downloadFileName = ""
    private var downloadID: Long = NO_DOWNLOAD
    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: DownloadNotificator? = null

    private val ui by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui.root)

        registerReceiver(onDownloadCompletedReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        onLoadingButtonClicked()

    }

    // Here we use extension function
    private fun MainActivity.onLoadingButtonClicked() {
        with(ui.mainContent) {
            loadingButton.setOnClickListener {
                when (downloadOptionRadioGroup.checkedRadioButtonId) {
                    View.NO_ID ->
                        // Show toast if user not select any radio button
                        Toast.makeText(
                            this@MainActivity,
                            "Please select the file to download",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        downloadFileName =
                            findViewById<RadioButton>(downloadOptionRadioGroup.checkedRadioButtonId)
                                .text.toString()
                        requestDownload()
                    }
                }
            }
        }
    }

    private val onDownloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }

    }

    private fun getDownloadNotificator(): DownloadNotificator = when (downloadNotificator) {
        null -> DownloadNotificator(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when(getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))){
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else ->DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun requestDownload() {

        with(getDownloadManager()) {
            downloadID.takeIf { it != NO_DOWNLOAD }?.run {
                unregisterDownloadContentObserver()
                downloadID = NO_DOWNLOAD
            }

            val request = DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadID = enqueue(request)

            createAndRegisterDownloadContentObserver()
        }
    }

    private fun DownloadManager.createAndRegisterDownloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryProgress() {

        query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    // Here we control all status of download
                    when(getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)))
                    {
                        // Download is failed
                        DownloadManager.STATUS_FAILED -> {
                            ui.mainContent.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                        // Download is paused
                        DownloadManager.STATUS_PAUSED -> {
                        }
                        // Download is pending
                        DownloadManager.STATUS_PENDING -> {
                        }
                        // Download is running
                        DownloadManager.STATUS_RUNNING -> {
                            ui.mainContent.loadingButton.changeButtonState(ButtonState.Loading)
                        }
                        // Download is successful
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            ui.mainContent.loadingButton.changeButtonState(ButtonState.Completed)

                        }
                    }
                }
            }
        }
    }

    // On activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadCompletedReceiver)
        unregisterDownloadContentObserver()
        downloadNotificator = null
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val NO_DOWNLOAD = 0L
    }
}