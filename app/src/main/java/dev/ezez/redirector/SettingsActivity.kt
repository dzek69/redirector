package dev.ezez.redirector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {

    private lateinit var listView: ListView
    private val browsers = mutableListOf<BrowserInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get primary color from theme
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48) // Increased padding
        }

        val title = TextView(this).apply {
            text = "Select Default Browser"
            textSize = 24f
            setTextColor(primaryColor) // Use theme color
            setPadding(0, 0, 0, 48)
        }
        layout.addView(title)

        listView = ListView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        layout.addView(listView)

        val testLink = TextView(this).apply {
            text = "Test browser: dzek.eu"
            setTextColor(primaryColor) // Use theme color
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 48) // Increased bottom padding to move it away from the edge
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dzek.eu"))
                startActivity(intent)
            }
        }
        layout.addView(testLink)

        setContentView(layout)
        loadBrowsers()
    }

    private fun loadBrowsers() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedBrowser = sharedPrefs.getString("browser_package", null)

        browsers.clear()
        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            if (packageName == "dev.ezez.redirector") continue
            
            browsers.add(BrowserInfo(
                name = info.loadLabel(pm).toString(),
                packageName = packageName,
                icon = info.loadIcon(pm),
                isSelected = packageName == savedBrowser
            ))
        }

        val adapter = object : ArrayAdapter<BrowserInfo>(this, android.R.layout.simple_list_item_single_choice, browsers) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as CheckedTextView
                view.text = getItem(position)?.name
                return view
            }
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        
        val selectedIndex = browsers.indexOfFirst { it.isSelected }
        if (selectedIndex != -1) {
            listView.setItemChecked(selectedIndex, true)
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selected = browsers[position]
            sharedPrefs.edit().putString("browser_package", selected.packageName).apply()
            Toast.makeText(this, "Default browser set to: ${selected.name}", Toast.LENGTH_SHORT).show()
        }
    }

    data class BrowserInfo(
        val name: String,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable,
        val isSelected: Boolean
    )
}
