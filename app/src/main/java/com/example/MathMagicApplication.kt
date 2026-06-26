package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.GameRepository

class MathMagicApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database.gameDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MathMagicApplication
            private set
    }
}
