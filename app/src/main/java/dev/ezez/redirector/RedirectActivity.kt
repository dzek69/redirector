package dev.ezez.redirector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class RedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri != null) {
            handleUri(uri)
        }
        finish()
    }

    private fun handleUri(uri: Uri) {
        if (isYouTubeUri(uri)) {
            // Try ReVanced first, then official YouTube
            if (!openInPackage(uri, "app.revanced.android.youtube")) {
                openInPackage(uri, "com.google.android.youtube")
            }
        } else {
            val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
            val savedBrowser = sharedPrefs.getString("browser_package", "com.vivaldi.browser")
            openInPackage(uri, savedBrowser!!)
        }
    }

    private fun isYouTubeUri(uri: Uri): Boolean {
        if (uri.scheme == "vnd.youtube") return true
        val host = uri.host?.lowercase() ?: return false
        return host.endsWith("youtube.com") || host == "youtu.be"
    }

    private fun openInPackage(uri: Uri, packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("RedirectActivity", "Failed to open $packageName")
            false
        }
    }
}
