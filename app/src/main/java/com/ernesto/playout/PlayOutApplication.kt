package com.ernesto.playout

import android.app.Application
import com.ernesto.playout.data.remote.FirebaseSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PlayOutApplication : Application() {

    @Inject
    lateinit var syncManager: FirebaseSyncManager

    override fun onCreate() {
        super.onCreate()
        syncManager.syncFacilities()
    }
}
