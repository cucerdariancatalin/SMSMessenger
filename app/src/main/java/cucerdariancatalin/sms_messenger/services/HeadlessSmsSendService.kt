package cucerdariancatalin.sms_messenger.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import com.klinker.android.send_message.Transaction
import cucerdariancatalin.sms_messenger.helpers.getSendMessageSettings
import cucerdariancatalin.sms_messenger.receivers.SmsStatusDeliveredReceiver
import cucerdariancatalin.sms_messenger.receivers.SmsStatusSentReceiver

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent == null) {
                return START_NOT_STICKY
            }

            val number = Uri.decode(intent.dataString!!.removePrefix("sms:").removePrefix("smsto:").removePrefix("mms").removePrefix("mmsto:").trim())
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val settings = getSendMessageSettings()
            val transaction = Transaction(this, settings)
            val message = com.klinker.android.send_message.Message(text, number)

            val smsSentIntent = Intent(this, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(this, SmsStatusDeliveredReceiver::class.java)

            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            transaction.sendNewMessage(message)
        } catch (ignored: Exception) {
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
