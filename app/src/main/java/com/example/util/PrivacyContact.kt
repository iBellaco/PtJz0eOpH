package com.example.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.R

/** Launch directly: package visibility can hide an installed mail app from resolveActivity. */
object PrivacyContact {
    const val EMAIL = "DevWildRiftCoach@gmail.com"

    fun open(context: Context) {
        val subject = context.getString(R.string.legal_contact_subject)
        val intent = Intent(Intent.ACTION_SENDTO,
            Uri.parse("mailto:$EMAIL?subject=${Uri.encode(subject)}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            copyAddress(context)
        } catch (_: SecurityException) {
            copyAddress(context)
        }
    }

    private fun copyAddress(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Coach", EMAIL))
        Toast.makeText(context, R.string.legal_contact_copied, Toast.LENGTH_LONG).show()
    }
}
