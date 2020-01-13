package com.lookie.socialdownloader.utilities

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.lookie.socialdownloader.R
import com.lookie.socialdownloader.data.room.entity.Post
import java.io.File

object SystemUtils {

  @JvmStatic
  fun setStatusBarColor(activity: Activity?, statusBarColor: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && activity != null) {
      val window = activity.window
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = ContextCompat.getColor(activity, statusBarColor)
    }
  }

  @JvmStatic
  fun isNetworkAvailable(context: Context?): Boolean {
    if (context != null) {
      val cm = context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val netInfo = cm.activeNetworkInfo
      return netInfo != null && netInfo.isConnectedOrConnecting
    }
    return false
  }

  fun getTargetSdkVersion(context: Context): Int {
    try {
      val info = context.packageManager.getPackageInfo(
        context.packageName, 0
      )
      return info.applicationInfo.targetSdkVersion
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return Build.VERSION_CODES.P
  }

  fun openInstagram(activity: Activity?, shortCode: String) {
    if (verifyInstagram(activity!!)) {
      val uri: Uri = Uri.parse("https://www.instagram.com/p/${shortCode}/")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.setPackage("com.instagram.android")
      try {
        activity.startActivity(intent)
      } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, R.string.cannot_open_insta, Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(activity, R.string.insta_not_install, Toast.LENGTH_SHORT).show()
    }
  }

  fun openProfileInstagram(activity: Activity?, username: String) {
    if (verifyInstagram(activity!!)) {
      val uri: Uri = Uri.parse("https://www.instagram.com/${username}/")
      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.setPackage("com.instagram.android")
      try {
        activity.startActivity(intent)
      } catch (e: ActivityNotFoundException) {
        Toast.makeText(activity, R.string.cannot_open_insta, Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(activity, R.string.insta_not_install, Toast.LENGTH_SHORT).show()
    }
  }

  fun repostInsta(activity: Activity?, post: Post?) {

    if (verifyInstagram(activity!!)) {

      val prefix = if (post!!.isVideo) ".mp4" else ".jpg"
      val multiMedia = post.children.edges!!.isNotEmpty()
      val shortCode = if (multiMedia) post.children.edges!![0].note!!.shortcode else post.shortcode

      // Create the URI from the media
      val mediaFile = File(
        File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
          activity.getString(R.string.app_name)
        ),
        "instagram_${shortCode}${prefix}"
      )

      // Check media exists on device
      if (!mediaFile.exists()) {
        Toast.makeText(activity, R.string.file_not_found, Toast.LENGTH_SHORT).show()
        return
      }

      val uri =
        FileProvider.getUriForFile(activity, "com.lookie.socialdownloader.provider", mediaFile)
      println("uri " + uri.path)

      val intent = Intent()

      // Create the new Intent using the 'Send' action.
      intent.action = Intent.ACTION_SEND
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

      // Add the URI to the Intent.
      intent.putExtra(Intent.EXTRA_STREAM, uri)

      // Set the MIME type
      intent.type = if (post.isVideo) "video/*" else "image/*"
      intent.setPackage("com.instagram.android")

      // Broadcast the Intent.
      try {
        activity.startActivity(intent)
      } catch (e: Exception) {
        Toast.makeText(activity, R.string.cannot_repost, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
      }
    } else {
      Toast.makeText(activity, R.string.insta_not_install, Toast.LENGTH_SHORT).show()
    }
  }

  fun shareLink(activity: Activity?, post: Post?) {
    val sendIntent = Intent(Intent.ACTION_SEND)
    sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.instagram.com/p/${post!!.shortcode}/")
    sendIntent.type = "text/plain"
    activity!!.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.share)))
  }

  private fun verifyInstagram(activity: Activity): Boolean {
    return try {
      activity.packageManager.getApplicationInfo("com.instagram.android", 0)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
  }

  fun copyText(activity: Activity?, text: String?, resId: Int?) {
    val clipboard: ClipboardManager? =
      activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("label", text)
    clipboard!!.setPrimaryClip(clip)
    Toast.makeText(activity, resId!!, Toast.LENGTH_SHORT).show()
  }
}