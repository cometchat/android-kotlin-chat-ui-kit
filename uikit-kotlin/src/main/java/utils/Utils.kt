package utils

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.*
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.helpers.Logger
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import constant.StringContract
import org.json.JSONObject
import screen.CometChatCallActivity
import screen.CometChatStartCallActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

public class Utils {
    companion object{
        private const val TAG = "Utils"

        fun removeEmojiAndSymbol(content: String): String? {
            var utf8tweet = ""
            try {
                val utf8Bytes = content.toByteArray(charset("UTF-8"))
                utf8tweet = String(utf8Bytes, (charset("UTF-8")))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val unicodeOutliers = Pattern.compile(
                    "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE or
                            Pattern.CASE_INSENSITIVE)
            val unicodeOutlierMatcher = unicodeOutliers.matcher(utf8tweet)
            utf8tweet = unicodeOutlierMatcher.replaceAll(" ")
            return utf8tweet
        }

        fun isDarkMode(context: Context): Boolean {
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightMode == Configuration.UI_MODE_NIGHT_YES
        }

        fun softTransition(`$this$softTransition`: Float, compareWith: Float, allowedDiff: Float, scaleFactor: Float): Float {
            return if (scaleFactor == 0.0f) {
                `$this$softTransition`
            } else {
                var result = `$this$softTransition`
                val diff: Float
                if (compareWith > `$this$softTransition`) {
                    if (compareWith / `$this$softTransition` > allowedDiff) {
                        diff = `$this$softTransition`.coerceAtLeast(compareWith) - `$this$softTransition`.coerceAtMost(compareWith)
                        result = `$this$softTransition` + diff / scaleFactor
                    }
                } else if (`$this$softTransition` > compareWith && `$this$softTransition` / compareWith > allowedDiff) {
                    diff = `$this$softTransition`.coerceAtLeast(compareWith) - `$this$softTransition`.coerceAtMost(compareWith)
                    result = `$this$softTransition` - diff / scaleFactor
                }
                result
            }
        }

        fun getAudioManager(context: Context): AudioManager? {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        fun dpToPixel(dp: Float, resources: Resources): Float {
            val density = resources.displayMetrics.density
            return dp * density
        }

        fun initiatecall(context: Context, recieverID: String?, receiverType: String?, callType: String?) {
            val call = Call(recieverID!!, receiverType, callType)
            val jsonObject = JSONObject()
            try {
                jsonObject.put("bookingId", 6)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            call.metadata = jsonObject
            CometChat.initiateCall(call, object : CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    Utils.startCallIntent(context, call.callReceiver as User, call.type, true, call.sessionId)
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "onError: " + e.message)
                    Snackbar.make((context as Activity).window.decorView.rootView, context.getResources().getString(R.string.call_initiate_error) + ":" + e.message, Snackbar.LENGTH_LONG).show()
                }
            })
        }

        fun convertTimeStampToDurationTime(var0: Long): String? {
            val var2 = var0 / 1000L
            val var4 = var2 / 60L % 60L
            val var6 = var2 / 60L / 60L % 24L
            return if (var6 == 0L) String.format(Locale.getDefault(), "%02d:%02d", var4, var2 % 60L) else String.format(Locale.getDefault(), "%02d:%02d:%02d", var6, var4, var2 % 60L)
        }

        fun getDateId(var0: Long): String? {
            val var2 = Calendar.getInstance(Locale.ENGLISH)
            var2.timeInMillis = var0
            return DateFormat.format("ddMMyyyy", var2).toString()
        }

        fun getDate(var0: Long): String? {
            val var2 = Calendar.getInstance(Locale.ENGLISH)
            var2.timeInMillis = var0
            return DateFormat.format("dd/MM/yyyy", var2).toString()
        }

        fun userSort(userList: List<User>): List<User> {
            Collections.sort(userList, Comparator<User?> { user: User?, user1: User? -> user!!.name.toLowerCase().compareTo(user1!!.name.toLowerCase()) })
            return userList
        }

        fun changeToolbarFont(toolbar: MaterialToolbar): TextView? {
            for (i in 0 until toolbar.childCount) {
                val view = toolbar.getChildAt(i)
                if (view is TextView) {
                    return view
                }
            }
            return null
        }

        fun getFileSize(fileSize: Int): String? {
            return if (fileSize > 1024) {
                if (fileSize > 1024 * 1024) {
                    (fileSize / (1024 * 1024)).toString() + " MB"
                } else {
                    (fileSize / 1024).toString() + " KB"
                }
            } else {
                "$fileSize B"
            }
        }

        fun getLastMessage(context: Context, lastMessage: BaseMessage): String? {
            var message: String? = null
            if (lastMessage.deletedAt == 0L) {
                when (lastMessage.category) {
                    CometChatConstants.CATEGORY_MESSAGE -> if (lastMessage is TextMessage) {
                        message = lastMessage.text
                        if (isLoggedInUser(lastMessage.getSender()))
                            if (isExtensionEnabled("profanity-filter")) message = "You: " + Extensions.getProfanityFilter(lastMessage) else "You: " + message
                        else if (isExtensionEnabled("profanity-filter")) message = lastMessage.sender.name + ": " + Extensions.getProfanityFilter(lastMessage) else lastMessage.sender.name + ": " + message

                    } else if (lastMessage is MediaMessage) {
                        message = if (isLoggedInUser(lastMessage.getSender())) "You sent a " + lastMessage.getType() else "You received a " + lastMessage.getType()
                    }
                    CometChatConstants.CATEGORY_CUSTOM -> message = if (isLoggedInUser(lastMessage.sender)) "You sent a " + lastMessage.type else "You received a " + lastMessage.type
                    CometChatConstants.CATEGORY_ACTION -> message = (lastMessage as Action).message
                    CometChatConstants.CATEGORY_CALL -> message = "Call Message"
                    else -> message = "Tap to start conversation"
                }
                return message
            } else return context.getString(R.string.message_deleted)
        }

        fun isLoggedInUser(user: User): Boolean {
            return user.uid == CometChat.getLoggedInUser().uid
        }

        /**
         * This method is used to convert user to group member. This method is used when we tries to add
         * user in a group or update group member scope.
         * @param user is object of User
         * @param isScopeUpdate is boolean which help us to check if scope is updated or not.
         * @param newScope is a String which contains newScope. If it is empty then user is added as participant.
         * @return GroupMember
         *
         * @see User
         *
         * @see GroupMember
         */
        fun UserToGroupMember(user: User, isScopeUpdate: Boolean, newScope: String?): GroupMember? {
            val groupMember: GroupMember
            groupMember = if (isScopeUpdate) GroupMember(user.uid, newScope) else GroupMember(user.uid, CometChatConstants.SCOPE_PARTICIPANT)
            groupMember.avatar = user.avatar
            groupMember.name = user.name
            groupMember.status = user.status
            return groupMember
        }

        fun getMessageDate(timestamp: Long): String? {
            return SimpleDateFormat("dd/MM/yyyy hh:mm a").format(Date(timestamp * 1000))
        }

        fun getHeaderDate(textView: TextView?, timestamp: Long): String? {
            val messageTimestamp = Calendar.getInstance()
            messageTimestamp.timeInMillis = timestamp
            val now = Calendar.getInstance()
            //        if (now.get(5) == messageTimestamp.get(5)) {
            return DateFormat.format("hh:mm a", messageTimestamp).toString()
//        } else {
//            return now.get(5) - messageTimestamp.get(5) == 1 ? "Yesterday " + DateFormat.format("hh:mm a", messageTimestamp).toString() : DateFormat.format("d MMMM", messageTimestamp).toString() + " " + DateFormat.format("hh:mm a", messageTimestamp).toString();
//        }
        }

        fun getHeaderDate(timestamp: Long): String? {
            val messageTimestamp = Calendar.getInstance()
            messageTimestamp.timeInMillis = timestamp
            val now = Calendar.getInstance()
            //        if (now.get(5) == messageTimestamp.get(5)) {
            return DateFormat.format("hh:mm a", messageTimestamp).toString()
//        } else {
//            return now.get(5) - messageTimestamp.get(5) == 1 ? "Yesterday " + DateFormat.format("hh:mm a", messageTimestamp).toString() : DateFormat.format("d MMMM", messageTimestamp).toString() + " " + DateFormat.format("hh:mm a", messageTimestamp).toString();
//        }
        }

        fun getLastMessageDate(timestamp: Long): String? {
            val lastMessageTime = SimpleDateFormat("h:mm a").format(Date(timestamp * 1000))
            val lastMessageDate = SimpleDateFormat("dd/MM/yyyy").format(Date(timestamp * 1000))
            val lastMessageWeek = SimpleDateFormat("EEE").format(Date(timestamp * 1000))
            val currentTimeStamp = System.currentTimeMillis()
            val diffTimeStamp = currentTimeStamp - timestamp * 1000
            Log.e(TAG, "getLastMessageDate: " + 24 * 60 * 60 * 1000)
            return if (diffTimeStamp < 24 * 60 * 60 * 1000) {
                lastMessageTime
            } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {
                "Yesterday"
            } else if (diffTimeStamp < 7 * 24 * 60 * 60 * 1000) {
                lastMessageWeek
            } else {
                lastMessageDate
            }
        }

        fun getReceiptDate(timestamp: Long): String? {
            val lastMessageTime = SimpleDateFormat("h:mm a").format(Date(timestamp * 1000))
            val lastMessageDate = SimpleDateFormat("dd/MM h:mm a").format(Date(timestamp * 1000))
            val lastMessageWeek = SimpleDateFormat("EEE h:mm a").format(Date(timestamp * 1000))
            val currentTimeStamp = System.currentTimeMillis()
            val diffTimeStamp = currentTimeStamp - timestamp * 1000
            Log.e(TAG, "getLastMessageDate: " + 24 * 60 * 60 * 1000)
            return if (diffTimeStamp < 24 * 60 * 60 * 1000) {
                lastMessageTime
            } else if (diffTimeStamp < 48 * 60 * 60 * 1000) {
                "Yesterday"
            } else if (diffTimeStamp < 7 * 24 * 60 * 60 * 1000) {
                lastMessageWeek
            } else {
                lastMessageDate
            }
        }

        fun checkDirExistence(context: Context, type: String): Boolean? {
            val audioDir = File(Environment.getExternalStorageDirectory().toString() + "/" +
                    context.resources.getString(R.string.app_name) + "/" + type + "/")
            return audioDir.isDirectory
        }

        fun makeDirectory(context: Context, type: String) {
            val audioDir = Environment.getExternalStorageDirectory().toString() + "/" +
                    context.resources.getString(R.string.app_name) + "/" + type + "/"
            Utils.createDirectory(audioDir)
        }

        fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
                for (permission in permissions) {
                    Logger.error(TAG, " hasPermissions() : Permission : " + permission
                            + "checkSelfPermission : " + ActivityCompat.checkSelfPermission(context, permission))
                    if (ActivityCompat.checkSelfPermission(context, permission) !=
                            PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String?>?): String? {
            var cursor: Cursor? = null
            val column = MediaStore.Files.FileColumns.DATA
            val projection = arrayOf(
                    column
            )
            try {
                cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                        null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, e.message)
            } finally {
                cursor?.close()
            }
            return null
        }

        fun getImagePathFromUri(context: Context?, aUri: Uri?): String? {
            var imagePath: String? = null
            if (aUri == null) {
                return imagePath
            }
            if (DocumentsContract.isDocumentUri(context, aUri)) {
                val documentId = DocumentsContract.getDocumentId(aUri)
                if ("com.android.providers.media.documents" == aUri.authority) {
                    val id = DocumentsContract.getDocumentId(aUri)
                    if (id != null && id.startsWith("raw:")) {
                        return id.substring(4)
                    }
                    val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id!!))
                        try {
                            val path = getDataColumn(context!!, contentUri, null, null)
                            if (path != null) {
                                return path
                            }
                        } catch (e: java.lang.Exception) {
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName: String = Utils.getFileName(context!!, aUri)!!
                    val cacheDir: File = Utils.getDocumentCacheDir(context!!)!!
                    val file: File = Utils.generateFileName(fileName, cacheDir)!!
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        Utils.saveFileFromUri(context, aUri, destinationPath)
                    }
                    imagePath = destinationPath
                } else if ("com.android.providers.downloads.documents" == aUri.authority) {
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(documentId))
                    imagePath = Utils.getImagePath(contentUri, null!!, context!!)
                }
            } else if ("content".equals(aUri.scheme, ignoreCase = true)) {
                imagePath = Utils.getImagePath(aUri, null!!, context!!)
            } else if ("file".equals(aUri.scheme, ignoreCase = true)) {
                imagePath = aUri.path
            }
            return imagePath
        }

        private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
            var inputStream: InputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                inputStream!!.read(buf)
                do {
                    bos.write(buf)
                } while (inputStream.read(buf) != -1)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun generateFileName(name: String?, directory: File?): File? {
            var name = name ?: return null
            var file = File(directory, name)
            if (file.exists()) {
                var fileName = name
                var extension = ""
                val dotIndex = name.lastIndexOf('.')
                if (dotIndex > 0) {
                    fileName = name.substring(0, dotIndex)
                    extension = name.substring(dotIndex)
                }
                var index = 0
                while (file.exists()) {
                    index++
                    name = "$fileName($index)$extension"
                    file = File(directory, name)
                }
            }
            try {
                if (!file.createNewFile()) {
                    return null
                }
            } catch (e: IOException) {
                Log.w(TAG, e)
                return null
            }
            return file
        }

        fun getDocumentCacheDir(context: Context): File? {
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        fun getPath(context: Context, folder: String): String? {
            return Environment.getExternalStorageDirectory().toString() + "/" +
                    context.resources.getString(R.string.app_name) + "/" + folder + "/"
        }

        fun getPath(context: Context?, uri: Uri): String? {
            val absolutePath = getImagePathFromUri(context, uri)
            return absolutePath ?: uri.toString()
        }

        fun getName(filename: String?): String? {
            if (filename == null) {
                return null
            }
            val index = filename.lastIndexOf('/')
            return filename.substring(index + 1)
        }


        fun getFileName(mediaFile: String): String? {
            val t1 = mediaFile.substring(mediaFile.lastIndexOf("/")).split("_").toTypedArray()
            return t1[2]
        }

        fun getFileName(context: Context, uri: Uri): String? {
            val mimeType = context.contentResolver.getType(uri)
            var filename: String? = null
            if (mimeType == null && context != null) {
                val path = getPath(context, uri)
                filename = if (path == null) {
                    getName(uri.toString())
                } else {
                    val file = File(path)
                    file.name
                }
            } else {
                val returnCursor = context.contentResolver.query(uri, null,
                        null, null, null)
                if (returnCursor != null) {
                    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    returnCursor.moveToFirst()
                    filename = returnCursor.getString(nameIndex)
                    returnCursor.close()
                }
            }
            return filename
        }

        private fun getImagePath(aUri: Uri, aSelection: String, context: Context): String? {
            try {
                var path: String? = null
                val cursor = context.contentResolver.query(aUri, null, aSelection, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    }
                    cursor.close()
                }
                return path
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun getOutputMediaFile(context: Context): String? {
            val var0 = File(Environment.getExternalStorageDirectory(), context.resources.getString(R.string.app_name))
            return if (!var0.exists() && !var0.mkdirs()) {
                null
            } else {
                val var1 = (Environment.getExternalStorageDirectory().toString() + "/" + context.resources.getString(R.string.app_name) + "/"
                        + "audio/")
                Utils.createDirectory(var1)
                var1 + SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + ".mp3"
            }
        }

        fun createDirectory(var0: String?) {
            if (!File(var0).exists()) {
                File(var0).mkdirs()
            }
        }

//        fun checkSmartReply(lastMessage: BaseMessage?): List<String?>? {
//            if (lastMessage != null && lastMessage.sender.uid != CometChat.getLoggedInUser().uid) {
//                if (lastMessage.metadata != null) {
//                    return Utils.getSmartReplyList(lastMessage)
//                }
//            }
//            return null
//        }

//        private fun getSmartReplyList(baseMessage: BaseMessage): List<String>? {
//            val extensionList: HashMap<String, JSONObject> = Utils.extensionCheck(baseMessage)!!
//            if (extensionList != null && extensionList.containsKey("smartReply")) {
//                val replyObject = extensionList["smartReply"]
//                val replyList: MutableList<String> = ArrayList()
//                try {
//                    replyList.add(replyObject!!.getString("reply_positive"))
//                    replyList.add(replyObject.getString("reply_neutral"))
//                    replyList.add(replyObject.getString("reply_negative"))
//                } catch (e: java.lang.Exception) {
//                    Log.e(TAG, "onSuccess: " + e.message)
//                }
//                return replyList
//            }
//            return null
//        }

//        public static fun extensionCheck(baseMessage: BaseMessage): HashMap<String, JSONObject>? {
//            val metadata = baseMessage.metadata
//            val extensionMap = HashMap<String, JSONObject>()
//            try {
//                return if (metadata != null) {
//                    val injectedObject = metadata.getJSONObject("@injected")
//                    if (injectedObject != null && injectedObject.has("extensions")) {
//                        val extensionsObject = injectedObject.getJSONObject("extensions")
//                        if (extensionsObject != null && extensionsObject.has("link-preview")) {
//                            val linkPreviewObject = extensionsObject.getJSONObject("link-preview")
//                            val linkPreview = linkPreviewObject.getJSONArray("links")
//                            if (linkPreview.length() > 0) {
//                                extensionMap["linkPreview"] = linkPreview.getJSONObject(0)
//                            }
//                        }
//                        if (extensionsObject != null && extensionsObject.has("smart-reply")) {
//                            extensionMap["smartReply"] = extensionsObject.getJSONObject("smart-reply")
//                        }
//                    }
//                    extensionMap
//                } else null
//            } catch (e: java.lang.Exception) {
//                Log.e(TAG, "isLinkPreview: " + e.message)
//            }
//            return null
//        }

        fun startCallIntent(context: Context, user: User, type: String?,
                            isOutgoing: Boolean, sessionId: String) {
            val videoCallIntent = Intent(context, CometChatCallActivity::class.java)
            videoCallIntent.putExtra(StringContract.IntentStrings.NAME, user.name)
            videoCallIntent.putExtra(StringContract.IntentStrings.UID, user.uid)
            videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID, sessionId)
            videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, user.avatar)
            videoCallIntent.action = type
            videoCallIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (isOutgoing) {
                videoCallIntent.type = "outgoing"
            } else {
                videoCallIntent.type = "incoming"
            }
            context.startActivity(videoCallIntent)
        }

        fun startGroupCallIntent(context: Context, group: Group, type: String?,
                                 isOutgoing: Boolean, sessionId: String) {
            val videoCallIntent = Intent(context, CometChatCallActivity::class.java)
            videoCallIntent.putExtra(StringContract.IntentStrings.NAME, group.name)
            videoCallIntent.putExtra(StringContract.IntentStrings.UID, group.guid)
            videoCallIntent.putExtra(StringContract.IntentStrings.SESSION_ID, sessionId)
            videoCallIntent.putExtra(StringContract.IntentStrings.AVATAR, group.icon)
            videoCallIntent.action = type
            videoCallIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (isOutgoing) {
                videoCallIntent.type = "outgoing"
            } else {
                videoCallIntent.type = "incoming"
            }
            context.startActivity(videoCallIntent)
        }

        fun dpToPx(context: Context, valueInDp: Float): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return valueInDp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun getBitmapFromURL(strURL: String?): Bitmap? {
            return try {
                val url = URL(strURL)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun showCallNotifcation(context: Context, call: Call) {
            try {
                Thread {
                    val REQUEST_CODE = 12
                    val m = Date().time.toInt()
                    val GROUP_ID = "group_id"
                    var receiverName: String? = ""
                    val callType: String
                    var receiverAvatar: String? = ""
                    var receiverUid: String? = ""
                    if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER && call.sender.uid == CometChat.getLoggedInUser().uid) {
                        receiverUid = (call.callReceiver as User).uid
                        receiverName = (call.callReceiver as User).name
                        receiverAvatar = (call.callReceiver as User).avatar
                    } else if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        receiverUid = call.sender.uid
                        receiverName = call.sender.name
                        receiverAvatar = call.sender.avatar
                    } else {
                        receiverUid = (call.receiver as Group).guid
                        receiverName = (call.receiver as Group).name
                        receiverAvatar = (call.receiver as Group).icon
                    }
                    callType = if (call.type == CometChatConstants.CALL_TYPE_AUDIO) {
                        context.resources.getString(R.string.incoming_audio_call)
                    } else {
                        context.resources.getString(R.string.incoming_video_call)
                    }
                    val callIntent: Intent
                    callIntent = Intent(context, CometChatCallActivity::class.java)
                    callIntent.putExtra(StringContract.IntentStrings.NAME, receiverName)
                    callIntent.putExtra(StringContract.IntentStrings.UID, receiverUid)
                    callIntent.putExtra(StringContract.IntentStrings.SESSION_ID, call.sessionId)
                    callIntent.putExtra(StringContract.IntentStrings.AVATAR, receiverAvatar)
                    callIntent.action = call.type
                    callIntent.type = "incoming"
                    val builder = NotificationCompat.Builder(context, "2")
                            .setSmallIcon(R.drawable.cc)
                            .setContentTitle(receiverName)
                            .setContentText(callType)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setChannelId("2")
                            .setColor(context.resources.getColor(R.color.colorPrimary))
                            .setLargeIcon(getBitmapFromURL(receiverAvatar))
                            .setGroup(GROUP_ID)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    val notificationManager = NotificationManagerCompat.from(context)
                    builder.setGroup(GROUP_ID + "Call")
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                    builder.addAction(0, "Answers", PendingIntent.getBroadcast(context, REQUEST_CODE, callIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    builder.addAction(0, "Decline", PendingIntent.getBroadcast(context, 1, callIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    notificationManager.notify(5, builder.build())
                }.start()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun startCall(activity: Activity, call: Call, mainView: RelativeLayout?) {
            CometChat.startCall(activity, call.sessionId, mainView!!, object : OngoingCallListener {
                override fun onUserJoined(user: User) {
                    Log.e("onUserJoined: ", user.uid)
                }

                override fun onUserLeft(user: User) {
                    Snackbar.make(activity.window.decorView.rootView, "User Left: " + user.name, Snackbar.LENGTH_LONG).show()
                    Log.e("onUserLeft: ", user.uid)
                }

                override fun onError(e: CometChatException) {
                    Log.e("onError: ", e.message)
                }

                override fun onCallEnded(call: Call) {
                    Log.e(TAG, "onCallEnded: $call")
                    activity.finish()
                }
            })
        }

        fun joinOnGoingCall(context: Context) {
            val intent = Intent(context, CometChatCallActivity::class.java)
            intent.putExtra(StringContract.IntentStrings.JOIN_ONGOING, true)
            context.startActivity(intent)
        }

        fun getAddress(context: Context?, latitude: Double, longitude: Double): String? {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.size > 0) {
                    return addresses[0].getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun startVideoCallIntent(context: Context, sessionId: String?) {
            val intent = Intent(context, CometChatStartCallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(StringContract.IntentStrings.SESSION_ID, sessionId)
            context.startActivity(intent)
        }
    }
}