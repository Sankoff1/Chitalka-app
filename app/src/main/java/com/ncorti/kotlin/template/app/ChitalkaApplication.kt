package com.ncorti.kotlin.template.app

import android.app.Application
import com.chitalka.debug.installConsoleCapture

class ChitalkaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installConsoleCapture()
    }
}
