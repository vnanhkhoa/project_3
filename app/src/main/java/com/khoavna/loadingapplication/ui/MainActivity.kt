package com.khoavna.loadingapplication.ui

import android.Manifest
import android.app.DownloadManager
import android.app.DownloadManager.STATUS_RUNNING
import android.app.DownloadManager.STATUS_SUCCESSFUL
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoavna.loadingapplication.MyApplication.Companion.CHANNEL_ID
import com.khoavna.loadingapplication.R
import com.khoavna.loadingapplication.databinding.ActivityMainBinding
import com.khoavna.loadingapplication.ui.component.State
import com.khoavna.loadingapplication.ui.detail.DetailActivity


class MainActivity : AppCompatActivity() {

    companion object {
        private const val URL_DOWNLOAD =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private val URL_DOWNLOAD_CONTENT = "content://downloads/my_downloads".toUri()

        const val FILE_NAME_KEY = "FILE_NAME_KEY"
        const val STATUS_KEY = "STATUS_KEY"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAIL = "FAIL"
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels()

    private var fileName = ""
    private var idDownload = 0L
    private val downloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    private val mResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            for (key in it.keys) {
                if (it[key] == false) {
                    showAlert()
                    return@registerForActivityResult
                }
            }
        }

    private val downloadContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadManager.query(DownloadManager.Query().setFilterById(idDownload))?.run {
                    if (moveToFirst()) {
                        val columnIndex = getColumnIndex(DownloadManager.COLUMN_STATUS)
                        when (getInt(columnIndex)) {
                            STATUS_SUCCESSFUL -> viewModel.updateDownLoadStatus(DownloadState.SUCCESSFUL)
                            STATUS_RUNNING -> viewModel.updateDownLoadStatus(DownloadState.RUNNING)
                            else -> viewModel.updateDownLoadStatus(DownloadState.FAILED)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        handlePermission()
        initEvents()
        viewModel.downloadState.observe(this) { state ->
            when (state) {
                DownloadState.SUCCESSFUL, DownloadState.FAILED -> {
                    if (binding.btnDownload.state == State.DEFAULT) return@observe
                    Toast.makeText(this, "Download Finished", Toast.LENGTH_SHORT).show()
                    binding.btnDownload.updateState(State.DEFAULT)
                    PendingIntentCompat.getActivity(
                        this,
                        1,
                        createIntent(state),
                        PendingIntent.FLAG_CANCEL_CURRENT,
                        false
                    ).let {
                        sendNotification(pendingIntent = it)
                    }

                }

                DownloadState.RUNNING -> {
                    binding.btnDownload.updateState(State.LOADING)
                }

                else -> {
                    // Don't something
                }
            }
        }
    }

    private fun handlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkPermission()) {
            mResultLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    private fun sendNotification(pendingIntent: PendingIntent) =
        NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.round_cloud_download_24)
            setContentTitle("Download $fileName")
            setContentText("Download $fileName is ${viewModel.downloadState.value!!.name.lowercase()}")
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            addAction(0,"Show download status", pendingIntent)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            with(NotificationManagerCompat.from(this@MainActivity)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkPermission()) {
                    showAlert()
                } else {
                    notify(idDownload.toInt(), build())
                }
            }
        }

    private fun createIntent(state: DownloadState) =
        Intent(this, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK;Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(FILE_NAME_KEY, fileName)
            putExtra(
                STATUS_KEY,
                if (state == DownloadState.FAILED) STATUS_FAIL else STATUS_SUCCESS
            )
        }

    private fun initEvents() {
        binding.apply {
            btnDownload.setOnClickListener {
                if (fileName.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please select file name", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (btnDownload.state == State.LOADING) return@setOnClickListener
                btnDownload.updateState(State.LOADING)
                downloadFile()
            }

            rgName.setOnCheckedChangeListener { radioGroup, id ->
                fileName = (radioGroup.findViewById<RadioButton>(id)).text.toString()
            }
        }
    }

    private fun downloadFile() {
        val request = DownloadManager.Request(Uri.parse(URL_DOWNLOAD)).apply {
            setTitle("Download File")
            setDescription("Download file description with file name: $fileName")
            setRequiresCharging(false)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        idDownload = downloadManager.enqueue(request)
        contentResolver.registerContentObserver(
            URL_DOWNLOAD_CONTENT, true, downloadContentObserver
        )
    }

    private fun showSettingSystem() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.data = Uri.fromParts("package", packageName, null)
            startActivity(it)
        }
    }

    private fun showAlert() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Required")
            .setMessage("This Permission Is Required For Proper Working Of The App")
            .setNegativeButton("No") { d, _ ->
                d.dismiss()
                Toast.makeText(
                    this,
                    "This Permission Is Required For Proper Working Of The App",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .setPositiveButton("OK") { d, _ ->
                showSettingSystem()
                d.dismiss()
            }.show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_DENIED

    enum class DownloadState {
        SUCCESSFUL, RUNNING, FAILED
    }
}