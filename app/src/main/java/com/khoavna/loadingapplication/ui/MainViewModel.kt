package com.khoavna.loadingapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _downLoadState = MutableLiveData<MainActivity.DownloadState>()
    val downloadState: LiveData<MainActivity.DownloadState> = _downLoadState

    fun updateDownLoadStatus(status : MainActivity.DownloadState) {
        _downLoadState.value = status
    }
}