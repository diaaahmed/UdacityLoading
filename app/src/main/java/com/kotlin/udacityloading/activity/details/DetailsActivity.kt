package com.kotlin.udacityloading.activity.details

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.kotlin.udacityloading.BuildConfig
import com.kotlin.udacityloading.R
import com.kotlin.udacityloading.databinding.ActivityDetailsBinding
import com.kotlin.udacityloading.databinding.DetailContentBinding
import com.kotlin.udacityloading.download.DownloadStatus

class DetailsActivity : AppCompatActivity() {

    private val fileName by lazy {
        intent?.extras?.getString(EXTRA_FILE_NAME, unknownText) ?: unknownText
    }
    private val downloadStatus by lazy {
        intent?.extras?.getString(EXTRA_DOWNLOAD_STATUS, unknownText) ?: unknownText
    }

    private val unknownText by lazy { getString(R.string.unknown) }

    private val ui by lazy {
        ActivityDetailsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        ui.detailContent.initializeView()

//        setContentView<ActivityDetailsBinding>(this, R.layout.activity_details)
//            .apply {
//                setSupportActionBar(toolbar)
//                detailContent.initializeView()
//            }
    }

    private fun DetailContentBinding.initializeView() {
        fileNameText.text = fileName
        downloadStatusText.text = downloadStatus
        okButton.setOnClickListener { finish() }
        changeViewForDownloadStatus()
    }

    private fun DetailContentBinding.changeViewForDownloadStatus() {
        when (downloadStatusText.text) {
            DownloadStatus.SUCCESSFUL.statusText -> {
                changeDownloadStatusImageTo(R.drawable.ic_check_circle_outline_24)
                changeDownloadStatusColorTo(R.color.colorPrimaryDark)
            }
            DownloadStatus.FAILED.statusText -> {
                changeDownloadStatusImageTo(R.drawable.ic_error_24)
                changeDownloadStatusColorTo(com.google.android.material.R.color.design_default_color_error)
            }
        }
    }

    private fun DetailContentBinding.changeDownloadStatusImageTo(@DrawableRes imageRes: Int) {
        downloadStatusImage.setImageResource(imageRes)
    }

    private fun DetailContentBinding.changeDownloadStatusColorTo(@ColorRes colorRes: Int) {
        ContextCompat.getColor(this@DetailsActivity,colorRes)
            .also { color->
                downloadStatusImage.imageTintList = ColorStateList.valueOf(color)
                downloadStatusText.setTextColor(color)
            }

    }

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"

        fun bundleExtrasOf(
            fileName: String,
            downloadStatus: DownloadStatus
        ) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.statusText
        )
    }
}