package io.github.sckzw.aaosvehicleproperty

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import java.lang.Exception

class VehiclePropertyProvider(private val context: Context) {

    interface PropertyListener {
        fun onPropertyUpdated(id: Int, name: String, value: String)
    }

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private val propertyValues = mutableMapOf<Int, String>()
    private var listener: PropertyListener? = null

    val infoProperties = listOf(
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

    val dynamicProperties = listOf(
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
        PropertyInfo("Accel Pedal %", VehiclePropertyIds.ACCELERATOR_PEDAL_COMPRESSION_PERCENTAGE),
        PropertyInfo("Brake Pedal %", VehiclePropertyIds.BRAKE_PEDAL_COMPRESSION_PERCENTAGE),
        PropertyInfo("Brake Fluid Low", VehiclePropertyIds.BRAKE_FLUID_LEVEL_LOW),
        PropertyInfo("Brake Pad Wear %", VehiclePropertyIds.BRAKE_PAD_WEAR_PERCENTAGE),
        PropertyInfo("Steering Angle", VehiclePropertyIds.PERF_STEERING_ANGLE),
        PropertyInfo("Tire Pressure", VehiclePropertyIds.TIRE_PRESSURE),
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
        PropertyInfo("Inst Fuel Econ", VehiclePropertyIds.INSTANTANEOUS_FUEL_ECONOMY),
        PropertyInfo("Inst EV Efficiency", VehiclePropertyIds.INSTANTANEOUS_EV_EFFICIENCY),
        PropertyInfo("Outside Temp", VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE),
        PropertyInfo("Night Mode", VehiclePropertyIds.NIGHT_MODE),
        PropertyInfo("Turn Signal Light State", VehiclePropertyIds.TURN_SIGNAL_LIGHT_STATE),
        PropertyInfo("Turn Signal Switch", VehiclePropertyIds.TURN_SIGNAL_SWITCH),
        PropertyInfo("Wipers State", VehiclePropertyIds.WINDSHIELD_WIPERS_STATE),
        PropertyInfo("Wipers Switch", VehiclePropertyIds.WINDSHIELD_WIPERS_SWITCH),
        PropertyInfo("Horn Engaged", VehiclePropertyIds.VEHICLE_HORN_ENGAGED),
        PropertyInfo("AD Level", VehiclePropertyIds.VEHICLE_DRIVING_AUTOMATION_CURRENT_LEVEL)
    )

    val unitProperties = listOf(
        PropertyInfo("Dist Units", VehiclePropertyIds.DISTANCE_DISPLAY_UNITS),
        PropertyInfo("Fuel Vol Units", VehiclePropertyIds.FUEL_VOLUME_DISPLAY_UNITS),
        PropertyInfo("Tire Press Units", VehiclePropertyIds.TIRE_PRESSURE_DISPLAY_UNITS),
        PropertyInfo("Speed Units", VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS),
        PropertyInfo("EV Batt Units", VehiclePropertyIds.EV_BATTERY_DISPLAY_UNITS),
        PropertyInfo("Fuel Cons Units", VehiclePropertyIds.FUEL_CONSUMPTION_UNITS_DISTANCE_OVER_VOLUME)
    )

    val allProperties = infoProperties + dynamicProperties + unitProperties

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            val formatted = formatPropertyValue(value.value)
            propertyValues[value.propertyId] = formatted
            val propName = allProperties.find { it.id == value.propertyId }?.name ?: "Unknown"
            listener?.onPropertyUpdated(value.propertyId, propName, formatted)
        }
        override fun onErrorEvent(propertyId: Int, areaId: Int) {}
    }

    fun init(propertyListener: PropertyListener) {
        this.listener = propertyListener
        try {
            car = Car.createCar(context)
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            
            allProperties.forEach { prop ->
                val value = fetchPropertyValue(prop.id)
                propertyValues[prop.id] = value
                listener?.onPropertyUpdated(prop.id, prop.name, value)
            }
        } catch (_: Exception) {}
    }

    fun startTracking() {
        dynamicProperties.forEach { prop ->
            try {
                carPropertyManager?.registerCallback(
                    propertyCallback, prop.id, CarPropertyManager.SENSOR_RATE_NORMAL
                )
            } catch (_: Exception) {}
        }
    }

    fun stopTracking() {
        try {
            carPropertyManager?.unregisterCallback(propertyCallback)
        } catch (_: Exception) {}
    }

    fun getPropertyValue(propId: Int): String {
        return propertyValues[propId] ?: "N/A"
    }

    private fun fetchPropertyValue(propId: Int): String {
        return try {
            val config = carPropertyManager?.getCarPropertyConfig(propId) ?: return "N/A"
            val areaId = if (config.areaIds.contains(0)) 0 else config.areaIds.getOrNull(0) ?: 0
            val propertyValue = carPropertyManager?.getProperty<Any>(propId, areaId)
            formatPropertyValue(propertyValue?.value)
        } catch (_: SecurityException) {
            "Denied"
        } catch (_: Exception) {
            "N/A"
        }
    }

    private fun formatPropertyValue(value: Any?): String {
        if (value == null) return "N/A"
        return when (value) {
            is Array<*> -> value.joinToString(", ")
            is IntArray -> value.joinToString(", ")
            is FloatArray -> value.joinToString(", ")
            is LongArray -> value.joinToString(", ")
            else -> value.toString()
        }
    }

    data class PropertyInfo(val name: String, val id: Int)
}
