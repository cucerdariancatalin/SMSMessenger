package cucerdariancatalin.sms_messenger.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.RemoteInput
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.SimpleContactsHelper
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import cucerdariancatalin.sms_messenger.extensions.*
import cucerdariancatalin.sms_messenger.helpers.REPLY
import cucerdariancatalin.sms_messenger.helpers.THREAD_ID
import cucerdariancatalin.sms_messenger.helpers.THREAD_NUMBER
import cucerdariancatalin.sms_messenger.helpers.sendMessage

class DirectReplyReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val address = intent.getStringExtra(THREAD_NUMBER)
        val threadId = intent.getLongExtra(THREAD_ID, 0L)
        var body = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(REPLY)?.toString() ?: return

        body = context.removeDiacriticsIfNeeded(body)

        if (address != null) {
            var subscriptionId: Int? = null
            val availableSIMs = context.subscriptionManagerCompat().activeSubscriptionInfoList
            if ((availableSIMs?.size ?: 0) > 1) {
                val currentSIMCardIndex = context.config.getUseSIMIdAtNumber(address)
                val wantedId = availableSIMs.getOrNull(currentSIMCardIndex)
                if (wantedId != null) {
                    subscriptionId = wantedId.subscriptionId
                }
            }

            ensureBackgroundThread {
                try {
                    context.sendMessage(body, listOf(address), subscriptionId, emptyList())
                    val message = context.getMessages(threadId, getImageResolutions = false, includeScheduledMessages = false, limit = 1).lastOrNull()
                    if (message != null) {
                        context.messagesDB.insertOrUpdate(message)
                    }
                } catch (e: Exception) {
                    context.showErrorToast(e)
                }

                val photoUri = SimpleContactsHelper(context).getPhotoUriFromPhoneNumber(address)
                val bitmap = context.getNotificationBitmap(photoUri)
                Handler(Looper.getMainLooper()).post {
                    context.notificationHelper.showMessageNotification(address, body, threadId, bitmap, sender = null, alertOnlyOnce = true)
                }

                context.markThreadMessagesRead(threadId)
                context.conversationsDB.markRead(threadId)
            }
        }
    }
}
