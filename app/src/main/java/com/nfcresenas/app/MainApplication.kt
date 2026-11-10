package com.nfcresenas.app

import android.app.Application
import com.nfcresenas.app.data.AppDatabase

class MainApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
