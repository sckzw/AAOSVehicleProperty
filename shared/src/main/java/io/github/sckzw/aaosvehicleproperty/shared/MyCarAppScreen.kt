package io.github.sckzw.aaosvehicleproperty.shared

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.Exception

class MyCarAppScreen(carContext: CarContext) : Screen(carContext) {
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private val propertyValues = mutableMapOf<Int, String>()

    // 1. 車両基本情報 (CAR_INFO)
    private val infoProperties = listOf(
        PropertyInfo("Make", VehiclePropertyIds.INFO_MAKE),
        PropertyInfo("Model", VehiclePropertyIds.INFO_MODEL),
        PropertyInfo("Model Year", VehiclePropertyIds.INFO_MODEL_YEAR),
        PropertyInfo("Model Trim", VehiclePropertyIds.INFO_MODEL_TRIM),
        PropertyInfo("Fuel Capacity", VehiclePropertyIds.INFO_FUEL_CAPACITY),
        PropertyInfo("Fuel Type", VehiclePropertyIds.INFO_FUEL_TYPE),
        PropertyInfo("EV Battery Capacity", VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY),
        PropertyInfo("EV Connector Type", VehiclePropertyIds.INFO_EV_CONNECTOR_TYPE),
        PropertyInfo("Fuel Door Location", VehiclePropertyIds.INFO_FUEL_DOOR_LOCATION),
        PropertyInfo("EV Port Location", VehiclePropertyIds.INFO_EV_PORT_LOCATION),
        PropertyInfo("Multi EV Port Locations", VehiclePropertyIds.INFO_MULTI_EV_PORT_LOCATIONS),
        PropertyInfo("Driver Seat", VehiclePropertyIds.INFO_DRIVER_SEAT),
        PropertyInfo("Exterior Dimensions", VehiclePropertyIds.INFO_EXTERIOR_DIMENSIONS),
        PropertyInfo("Vehicle Size Class", VehiclePropertyIds.INFO_VEHICLE_SIZE_CLASS),
        PropertyInfo("Vehicle Curb Weight", VehiclePropertyIds.VEHICLE_CURB_WEIGHT),
        PropertyInfo("GSR Compliance", VehiclePropertyIds.GENERAL_SAFETY_REGULATION_COMPLIANCE),
        PropertyInfo("ETC Card Type", VehiclePropertyIds.ELECTRONIC_TOLL_COLLECTION_CARD_TYPE),
        PropertyInfo("ETC Card Status", VehiclePropertyIds.ELECTRONIC_TOLL_COLLECTION_CARD_STATUS)
    )

    // 2. 走行状態・パワートレイン
    private val dynamicProperties = listOf(
        PropertyInfo("Vehicle Speed", VehiclePropertyIds.PERF_VEHICLE_SPEED),
        PropertyInfo("Vehicle Speed Display", VehiclePropertyIds.PERF_VEHICLE_SPEED_DISPLAY),
        PropertyInfo("Wheel Tick", VehiclePropertyIds.WHEEL_TICK),
        PropertyInfo("Gear Selection", VehiclePropertyIds.GEAR_SELECTION),
        PropertyInfo("Current Gear", VehiclePropertyIds.CURRENT_GEAR),
        PropertyInfo("Parking Brake On", VehiclePropertyIds.PARKING_BRAKE_ON),
        PropertyInfo("Parking Brake Auto", VehiclePropertyIds.PARKING_BRAKE_AUTO_APPLY),
        PropertyInfo("EV Regen Level", VehiclePropertyIds.EV_BRAKE_REGENERATION_LEVEL),
        PropertyInfo("EV Stopping Mode", VehiclePropertyIds.EV_STOPPING_MODE),
        PropertyInfo("Engine RPM", VehiclePropertyIds.ENGINE_RPM),
        // 3. アクセル・ブレーキ
        PropertyInfo("Accel Pedal %", VehiclePropertyIds.ACCELERATOR_PEDAL_COMPRESSION_PERCENTAGE),
        PropertyInfo("Brake Pedal %", VehiclePropertyIds.BRAKE_PEDAL_COMPRESSION_PERCENTAGE),
        PropertyInfo("Brake Fluid Low", VehiclePropertyIds.BRAKE_FLUID_LEVEL_LOW),
        PropertyInfo("Brake Pad Wear %", VehiclePropertyIds.BRAKE_PAD_WEAR_PERCENTAGE),
        // 4. ステアリング
        PropertyInfo("Steering Angle", VehiclePropertyIds.PERF_STEERING_ANGLE),
        // 5. タイヤ
        PropertyInfo("Tire Pressure", VehiclePropertyIds.TIRE_PRESSURE),
        // 6. 燃料・EV・航続距離
        PropertyInfo("Fuel Level", VehiclePropertyIds.FUEL_LEVEL),
        PropertyInfo("Fuel Level Low", VehiclePropertyIds.FUEL_LEVEL_LOW),
        PropertyInfo("Range Remaining", VehiclePropertyIds.RANGE_REMAINING),
        PropertyInfo("EV Battery Level", VehiclePropertyIds.EV_BATTERY_LEVEL),
        PropertyInfo("EV Current Capacity", VehiclePropertyIds.EV_CURRENT_BATTERY_CAPACITY),
        PropertyInfo("EV Battery Temp", VehiclePropertyIds.EV_BATTERY_AVERAGE_TEMPERATURE),
        PropertyInfo("EV Charge Rate", VehiclePropertyIds.EV_BATTERY_INSTANTANEOUS_CHARGE_RATE),
        PropertyInfo("EV Charge State", VehiclePropertyIds.EV_CHARGE_STATE),
        PropertyInfo("EV Regen State", VehiclePropertyIds.EV_REGENERATIVE_BRAKING_STATE),
        PropertyInfo("EV Charge Limit", VehiclePropertyIds.EV_CHARGE_CURRENT_DRAW_LIMIT),
        PropertyInfo("EV Charge % Limit", VehiclePropertyIds.EV_CHARGE_PERCENT_LIMIT),
        PropertyInfo("EV Port Connected", VehiclePropertyIds.EV_CHARGE_PORT_CONNECTED),
        PropertyInfo("EV Port Open", VehiclePropertyIds.EV_CHARGE_PORT_OPEN),
        PropertyInfo("Fuel Door Open", VehiclePropertyIds.FUEL_DOOR_OPEN),
        // 7. 燃費・電費
        PropertyInfo("Inst Fuel Econ", VehiclePropertyIds.INSTANTANEOUS_FUEL_ECONOMY),
        PropertyInfo("Inst EV Efficiency", VehiclePropertyIds.INSTANTANEOUS_EV_EFFICIENCY),
        // 8. 外部環境
        PropertyInfo("Outside Temp", VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE),
        PropertyInfo("Night Mode", VehiclePropertyIds.NIGHT_MODE),
        // 10. ライト・ウインカー
        PropertyInfo("Turn Signal State", VehiclePropertyIds.TURN_SIGNAL_STATE),
        PropertyInfo("Turn Signal Switch", VehiclePropertyIds.TURN_SIGNAL_SWITCH),
        // 11. ワイパー
        PropertyInfo("Wipers State", VehiclePropertyIds.WINDSHIELD_WIPERS_STATE),
        PropertyInfo("Wipers Switch", VehiclePropertyIds.WINDSHIELD_WIPERS_SWITCH),
        // 12. ホーン
        PropertyInfo("Horn Engaged", VehiclePropertyIds.VEHICLE_HORN_ENGAGED),
        // 13. 自動運転状態
        PropertyInfo("AD Level", VehiclePropertyIds.VEHICLE_DRIVING_AUTOMATION_CURRENT_LEVEL)
    )

    // 9. 表示単位
    private val unitProperties = listOf(
        PropertyInfo("Dist Units", VehiclePropertyIds.DISTANCE_DISPLAY_UNITS),
        PropertyInfo("Fuel Vol Units", VehiclePropertyIds.FUEL_VOLUME_DISPLAY_UNITS),
        PropertyInfo("Tire Press Units", VehiclePropertyIds.TIRE_PRESSURE_DISPLAY_UNITS),
        PropertyInfo("Speed Units", VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS),
        PropertyInfo("EV Batt Units", VehiclePropertyIds.EV_BATTERY_DISPLAY_UNITS),
        PropertyInfo("Fuel Cons Units", VehiclePropertyIds.FUEL_CONSUMPTION_UNITS_DISTANCE_OVER_VOLUME)
    )

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            propertyValues[value.propertyId] = value.value.toString()
            invalidate()
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
        }
    }

    init {
        try {
            car = Car.createCar(carContext)
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            
            val allProps = infoProperties + dynamicProperties + unitProperties
            allProps.forEach { prop ->
                propertyValues[prop.id] = fetchPropertyValue(prop.id)
            }
        } catch (e: Exception) {
            // Handle connection error
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                dynamicProperties.forEach { prop ->
                    try {
                        carPropertyManager?.registerCallback(
                            propertyCallback,
                            prop.id,
                            CarPropertyManager.SENSOR_RATE_NORMAL
                        )
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                carPropertyManager?.unregisterCallback(propertyCallback)
            }
        })
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        val allProperties = infoProperties + dynamicProperties + unitProperties

        for (prop in allProperties) {
            val value = propertyValues[prop.id] ?: "Loading..."
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(prop.name)
                    .addText(value)
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .setTitle("All Vehicle Properties")
            .build()
    }

    private fun fetchPropertyValue(propId: Int): String {
        return try {
            // Check for area 0 (Global) or fallback to first available area if not global
            val config = carPropertyManager?.getCarPropertyConfig(propId)
            val areaIds = config?.areaIds
            val areaId = if (areaIds?.contains(0) == true) 0 else areaIds?.getOrNull(0) ?: 0
            
            val value = carPropertyManager?.getProperty<Any>(propId, areaId)?.value
            value?.toString() ?: "N/A"
        } catch (e: SecurityException) {
            "Denied"
        } catch (e: Exception) {
            "N/A"
        }
    }

    data class PropertyInfo(val name: String, val id: Int)
}
