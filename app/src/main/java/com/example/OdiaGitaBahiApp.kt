package com.example

import android.app.Application
import com.example.data.local.SongBookDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.BaserowNetworkFactory
import com.example.data.repository.NetworkMonitor
import com.example.data.repository.SongRepository

class AppContainer(application: Application) {
    val database: SongBookDatabase by lazy {
        SongBookDatabase.getInstance(application)
    }
    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(application)
    }
    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(application)
    }
    private val baserowApiService by lazy {
        BaserowNetworkFactory.createApiService()
    }
    val songRepository: SongRepository by lazy {
        SongRepository(
            context = application,
            songDao = database.songDao(),
            apiService = baserowApiService,
            preferencesRepository = preferencesRepository
        )
    }
}

class OdiaGitaBahiApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
