package dev.ezez.redirector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class RedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        Log.d("RedirectActivity", "Received URI: $uri")
        
        if (uri != null) {
            handleUri(uri)
        } else {
            Log.w("RedirectActivity", "No URI found in intent")
        }
        finish()
    }

    private fun handleUri(uri: Uri) {
        if (isYouTubeUri(uri)) {
            openInPackage(uri, "app.revanced.android.youtube")
        } else {
            openInPackage(uri, "com.vivaldi.browser")
        }
    }

    private fun isYouTubeUri(uri: Uri): Boolean {
        if (uri.scheme == "vnd.youtube") return true
        val host = uri.host?.lowercase() ?: return false
        return host == "youtube.com" || host.endsWith(".youtube.com") || host == "youtu.be"
    }

    private fun openInPackage(uri: Uri, packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("RedirectActivity", "Failed to open $packageName, falling back to chooser", e)
            val chooserIntent = Intent(Intent.ACTION_VIEW, uri)
            // If the target package is missing, we just try a generic view.
            // Since this app is default browser, we MUST be careful not to loop.
            // However, starting a chooser or a generic intent without package 
            // usually lets the user pick another app.
            try {
                startActivity(Intent.createChooser(chooserIntent, "Open with"))
            } catch (ex: Exception) {
                Log.e("RedirectActivity", "Absolute failure to open link", ex)
            }
        }
    }
}
