package cucerdariancatalin.sms_messenger.receivers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.klinker.android.send_message.DeliveredReceiver
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import cucerdariancatalin.sms_messenger.extensions.messagesDB
import cucerdariancatalin.sms_messenger.extensions.updateMessageStatus
import cucerdariancatalin.sms_messenger.helpers.refreshMessages

class SmsStatusDeliveredReceiver : DeliveredReceiver() {

    override fun onMessageStatusUpdated(context: Context, intent: Intent, receiverResultCode: Int) {
        if (intent.extras?.containsKey("message_uri") == true) {
            val uri = Uri.parse(intent.getStringExtra("message_uri"))
            val messageId = uri?.lastPathSegment?.toLong() ?: 0L
            ensureBackgroundThread {
                val status = Telephony.Sms.STATUS_COMPLETE
                context.updateMessageStatus(messageId, status)
                val updated = context.messagesDB.updateStatus(messageId, status)
                if (updated == 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        ensureBackgroundThread {
                            context.messagesDB.updateStatus(messageId, status)
                        }
                    }, 2000)
                }

                refreshMessages()
            }
        }
    }
}
