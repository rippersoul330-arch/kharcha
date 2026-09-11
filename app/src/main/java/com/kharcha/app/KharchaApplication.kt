package com.kharcha.app

import android.app.Application
import com.kharcha.app.data.KharchaDatabase
import com.kharcha.app.data.KharchaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * App-wide singletons: the Room database and the repository.
 * Access via `(context.applicationContext as KharchaApplication).repository`.
 */
class KharchaApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: KharchaDatabase by lazy { KharchaDatabase.get(this) }

    val repository: KharchaRepository by lazy {
        KharchaRepository(database.expenseDao(), database.categoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Seed the built-in categories on first launch.
        applicationScope.launch {
            repository.ensureDefaultCategories()
        }
    }
}

/** Convenience accessor used across activities/services. */
val android.content.Context.kharchaRepository: KharchaRepository
    get() = (applicationContext as KharchaApplication).repository
