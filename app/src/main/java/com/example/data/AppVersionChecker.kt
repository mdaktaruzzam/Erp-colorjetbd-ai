package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.colorjeterp.pxlmjq.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppVersionConfig(
  val latestVersionName: String = "",
  val latestVersionCode: Long = 0L,
  val minRequiredVersionName: String = "",
  val minRequiredVersionCode: Long = 0L,
  val isForceUpdate: Boolean = false,
  val releaseNotes: String = "",
  val updateUrl: String = ""
)

object AppVersionChecker {
  private const val TAG = "AppVersionChecker"
  private const val COLLECTION_CONFIG = "app_config"
  private const val DOCUMENT_VERSION = "version_info"

  /**
   * Compares two semantic version strings (e.g., "1.0.22" vs "1.0.23").
   * Returns:
   *  negative if current < remote (update available)
   *  0 if current == remote
   *  positive if current > remote
   */
  fun compareVersions(currentVersion: String, remoteVersion: String): Int {
    if (remoteVersion.isBlank()) return 0
    val currentParts = currentVersion.split(".").mapNotNull { it.trim().toIntOrNull() }
    val remoteParts = remoteVersion.split(".").mapNotNull { it.trim().toIntOrNull() }

    val maxLength = maxOf(currentParts.size, remoteParts.size)
    for (i in 0 until maxLength) {
      val c = currentParts.getOrElse(i) { 0 }
      val r = remoteParts.getOrElse(i) { 0 }
      if (c != r) {
        return c.compareTo(r)
      }
    }
    return 0
  }

  /**
   * Fetches the version configuration from Firestore.
   * Document: app_config/version_info
   */
  suspend fun fetchVersionConfig(): AppVersionConfig? {
    return try {
      val firestore = FirebaseFirestore.getInstance()
      val snapshot = firestore.collection(COLLECTION_CONFIG)
        .document(DOCUMENT_VERSION)
        .get()
        .await()

      if (snapshot != null && snapshot.exists()) {
        AppVersionConfig(
          latestVersionName = snapshot.getString("latestVersionName") ?: snapshot.getString("versionName") ?: "",
          latestVersionCode = snapshot.getLong("latestVersionCode") ?: snapshot.getLong("versionCode") ?: 0L,
          minRequiredVersionName = snapshot.getString("minRequiredVersionName") ?: "",
          minRequiredVersionCode = snapshot.getLong("minRequiredVersionCode") ?: 0L,
          isForceUpdate = snapshot.getBoolean("isForceUpdate") ?: false,
          releaseNotes = snapshot.getString("releaseNotes") ?: "A new version of ColorJet ERP is available with improvements and fixes.",
          updateUrl = snapshot.getString("updateUrl") ?: "https://play.google.com/store/apps/details?id=com.aistudio.colorjeterp.pxlmjq"
        )
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching version config from Firestore", e)
      null
    }
  }

  /**
   * Checks if an update is required or available based on versionName and versionCode.
   */
  fun isUpdateAvailable(config: AppVersionConfig): Boolean {
    val currentVersionName = BuildConfig.VERSION_NAME
    val currentVersionCode = BuildConfig.VERSION_CODE.toLong()

    val hasNewerVersionName = compareVersions(currentVersionName, config.latestVersionName) < 0
    val hasNewerVersionCode = config.latestVersionCode > 0 && config.latestVersionCode > currentVersionCode

    return hasNewerVersionName || hasNewerVersionCode
  }

  /**
   * Checks if the update is critical/mandatory.
   */
  fun isCriticalUpdate(config: AppVersionConfig): Boolean {
    if (config.isForceUpdate) return true
    
    val currentVersionName = BuildConfig.VERSION_NAME
    val currentVersionCode = BuildConfig.VERSION_CODE.toLong()

    if (config.minRequiredVersionName.isNotBlank() && compareVersions(currentVersionName, config.minRequiredVersionName) < 0) {
      return true
    }

    if (config.minRequiredVersionCode > 0 && currentVersionCode < config.minRequiredVersionCode) {
      return true
    }

    return false
  }
}

/**
 * Composable dialog to inform users of available updates.
 */
@Composable
fun AppUpdateDialog(
  config: AppVersionConfig,
  isCritical: Boolean,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = {
      if (!isCritical) {
        onDismiss()
      }
    },
    properties = DialogProperties(
      dismissOnBackPress = !isCritical,
      dismissOnClickOutside = !isCritical
    )
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("app_update_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = "Update Available",
            tint = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (isCritical) "Critical Update Required" else "New Version Available",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Current: v${BuildConfig.VERSION_NAME} → Latest: v${config.latestVersionName.ifBlank { "Latest" }}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (config.releaseNotes.isNotBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = config.releaseNotes,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(12.dp),
              textAlign = TextAlign.Start
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (!isCritical) {
            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier
                .weight(1f)
                .testTag("dismiss_update_button")
            ) {
              Text("Later")
            }
          }

          Button(
            onClick = {
              try {
                val targetUrl = config.updateUrl.ifBlank {
                  "https://play.google.com/store/apps/details?id=${context.packageName}"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                  flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
              } catch (e: Exception) {
                Log.e("AppUpdateDialog", "Unable to open update URL", e)
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("confirm_update_button")
          ) {
            Text(if (isCritical) "Update Now" else "Update")
          }
        }
      }
    }
  }
}
