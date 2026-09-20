package io.github.sckzw.aaosvehicleproperty.shared

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class GearStatusSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return GearStatusScreen(carContext)
    }
}