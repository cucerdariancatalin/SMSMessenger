package cucerdariancatalin.sms_messenger.extensions

import android.text.format.DateFormat
import java.util.*

fun Date.format(pattern: String): String {
    return DateFormat.format(pattern, this).toString()
}
