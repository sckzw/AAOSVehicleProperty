package io.github.sckzw.aaosvehicleproperty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VehiclePropertyListActivity : ComponentActivity() {
    private lateinit var provider: VehiclePropertyProvider
    private val propertyValues = mutableMapOf<Int, String>()
    private val propertyList = mutableListOf<VehiclePropertyProvider.PropertyInfo>()
    private lateinit var adapter: PropertyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        provider = VehiclePropertyProvider(this)
        propertyList.addAll(provider.allProperties)

        val listView = findViewById<ListView>(R.id.listView)
        adapter = PropertyAdapter(propertyList, propertyValues)
        listView.adapter = adapter

        provider.init(object : VehiclePropertyProvider.PropertyListener {
            override fun onPropertyUpdated(id: Int, name: String, value: String) {
                propertyValues[id] = value
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        provider.startTracking()
    }

    override fun onStop() {
        super.onStop()
        provider.stopTracking()
    }

    private class PropertyAdapter(
        private val properties: List<VehiclePropertyProvider.PropertyInfo>,
        private val values: Map<Int, String>
    ) : BaseAdapter() {

        override fun getCount(): Int = properties.size

        override fun getItem(position: Int): Any = properties[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.property_item, parent, false)
            
            val prop = properties[position]
            val nameTextView = view.findViewById<TextView>(R.id.propertyName)
            val valueTextView = view.findViewById<TextView>(R.id.propertyValue)

            nameTextView.text = prop.name
            valueTextView.text = values[prop.id] ?: "N/A"

            return view
        }
    }
}
