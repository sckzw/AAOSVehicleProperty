package io.github.sckzw.aaosvehicleproperty.shared

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class GearStatusScreen(carContext: CarContext) : Screen(carContext) {
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private val propertyValues = mutableMapOf<Int, Int>()

    private val gearProperties = listOf(
        VehiclePropertyIds.GEAR_SELECTION,
        VehiclePropertyIds.CURRENT_GEAR
    )

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            val intValue = value.value as? Int ?: return
            propertyValues[value.propertyId] = intValue
            invalidate()
        }
        override fun onErrorEvent(propertyId: Int, areaId: Int) {}
    }

    init {
        try {
            car = Car.createCar(carContext)
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            
            gearProperties.forEach { propId ->
                propertyValues[propId] = fetchPropertyIntValue(propId)
            }
        } catch (_: Exception) {}

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                gearProperties.forEach { propId ->
                    try {
                        carPropertyManager?.registerCallback(
                            propertyCallback, propId, CarPropertyManager.SENSOR_RATE_NORMAL
                        )
                    } catch (_: Exception) {}
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                carPropertyManager?.unregisterCallback(propertyCallback)
            }
        })
    }

    override fun onGetTemplate(): Template {
        val gearSelection = propertyValues[VehiclePropertyIds.GEAR_SELECTION] ?: 0
        val currentGear = propertyValues[VehiclePropertyIds.CURRENT_GEAR] ?: 0

        val paneBuilder = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle(getGearName(gearSelection))
                    .addText(carContext.getString(R.string.label_gear_selection))
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle(getGearName(currentGear))
                    .addText(carContext.getString(R.string.label_current_gear))
                    .build()
            )

        val header = Header.Builder()
            .setStartHeaderAction(Action.APP_ICON)
            .setTitle(carContext.getString(R.string.title_gear_status))
            .build()

        return PaneTemplate.Builder(paneBuilder.build())
            .setHeader(header)
            .build()
    }

    private fun getGearName(gearValue: Int): String {
        val resId = when (gearValue) {
            0x0001 -> R.string.gear_neutral
            0x0002 -> R.string.gear_reverse
            0x0004 -> R.string.gear_park
            0x0008 -> R.string.gear_drive
            0x0010 -> R.string.gear_d1
            0x0020 -> R.string.gear_d2
            0x0040 -> R.string.gear_d3
            0x0080 -> R.string.gear_d4
            0x0100 -> R.string.gear_d5
            0x0200 -> R.string.gear_d6
            0x0400 -> R.string.gear_d7
            0x0800 -> R.string.gear_d8
            0x1000 -> R.string.gear_d9
            else -> 0
        }

        return if (resId != 0) {
            carContext.getString(resId)
        } else {
            carContext.getString(R.string.gear_unknown, gearValue)
        }
    }

    private fun fetchPropertyIntValue(propId: Int): Int {
        return try {
            val config = carPropertyManager?.getCarPropertyConfig(propId) ?: return 0
            val areaId = if (config.areaIds.contains(0)) 0 else config.areaIds.getOrNull(0) ?: 0
            val propertyValue = carPropertyManager?.getProperty<Int>(propId, areaId)
            propertyValue?.value ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
