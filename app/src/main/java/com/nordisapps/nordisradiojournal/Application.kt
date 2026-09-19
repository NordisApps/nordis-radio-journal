package com.nordisapps.nordisradiojournal

import android.app.Application
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.nordisapps.nordisradiojournal.viewmodel.SharedStateHolder

class MyApp : Application() {
    lateinit var imageLoader: ImageLoader
        private set

    val sharedState = SharedStateHolder()

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        val appCheck = Firebase.appCheck
        if (BuildConfig.DEBUG) {
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}