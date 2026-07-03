package com.ernesto.playout

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.ernesto.playout.data.remote.FirebaseSyncManager
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PlayOutApplication : Application() {

    @Inject
    lateinit var syncManager: FirebaseSyncManager

    override fun onCreate() {
        super.onCreate()
        syncManager.syncFacilities()

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this@PlayOutApplication)
                .components {
                    add(OkHttpNetworkFetcherFactory(
                        callFactory = { OkHttpClient() }
                    ))
                }
                .build()
        }
    }
}
