package com.playtube.protube.video.music.local.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.playtube.protube.video.music.local.subscription.workers.SubscriptionExportWorker
import com.playtube.protube.video.music.local.subscription.workers.SubscriptionImportInput
import com.playtube.protube.video.music.streams.io.NoFileManagerSafeGuard
import com.playtube.protube.video.music.streams.io.StoredFileHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This class has to be created in onAttach() or onCreate().
 *
 * It contains registerForActivityResult calls and those
 * calls are only allowed before a fragment/activity is created.
 */
class SubscriptionsImportExportHelper(
    val fragment: Fragment
) {
    val context: Context = fragment.requireContext()

    companion object {
        val TAG: String =
            SubscriptionsImportExportHelper::class.java.simpleName + "@" + Integer.toHexString(
                hashCode()
            )
    }

    private val requestExportLauncher =
        fragment.registerForActivityResult(StartActivityForResult(), this::requestExportResult)
    private val requestImportLauncher =
        fragment.registerForActivityResult(StartActivityForResult(), this::requestImportResult)

    private fun requestExportResult(result: ActivityResult) {
        val uri = result.data?.data
        if (uri != null && result.resultCode == Activity.RESULT_OK) {
            persistSafUriPermission(uri, result.data?.flags ?: 0)
            SubscriptionExportWorker.Companion.schedule(context, uri)
        }
    }

    private fun requestImportResult(result: ActivityResult) {
        val uri = result.data?.data
        if (uri != null && result.resultCode == Activity.RESULT_OK) {
            persistSafUriPermission(uri, result.data?.flags ?: 0)
            ImportConfirmationDialog.show(
                fragment,
                SubscriptionImportInput.PreviousExportMode(uri.toString())
            )
        }
    }

    private fun persistSafUriPermission(uri: Uri, resultFlags: Int) {
        val takeFlags = resultFlags and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (takeFlags == 0) {
            return
        }

        try {
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (_: SecurityException) {
            // Some providers/flows don't support persistable grants; worker can still proceed
            // with already-granted temporary permissions when available.
        }
    }

    fun onExportSelected() {
        val date = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).format(Date())
        val exportName = "playtube_subscriptions_$date.json"

        NoFileManagerSafeGuard.launchSafe(
            requestExportLauncher,
            StoredFileHelper.getNewPicker(
                context,
                exportName,
                SubscriptionFragment.Companion.JSON_MIME_TYPE,
                null
            ),
            TAG,
            context
        )
    }

    fun onImportPreviousSelected() {
        NoFileManagerSafeGuard.launchSafe(
            requestImportLauncher,
            StoredFileHelper.getPicker(context, SubscriptionFragment.Companion.JSON_MIME_TYPE),
            TAG,
            context
        )
    }
}
