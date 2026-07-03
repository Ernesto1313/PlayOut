package com.ernesto.playout.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UploadStatusTracker {
    // "uploading", "pending", "error"
    private val _statuses = MutableStateFlow<Map<Int, String>>(emptyMap())
    val statuses: StateFlow<Map<Int, String>> = _statuses

    fun setStatus(fid: Int, status: String) {
        _statuses.value = _statuses.value + (fid to status)
    }

    fun getStatus(fid: Int): String {
        return _statuses.value[fid] ?: "uploading"
    }
}
