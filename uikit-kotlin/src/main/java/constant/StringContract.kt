package constant

import com.cometchat.pro.constants.CometChatConstants
import java.util.*

class StringContract {
    object Sounds {
        var enableMessageSounds = true

        var enableCallSounds = true
    }

    object AppInfo {
        var AUTH_KEY = "";
    }
    object IntentStrings {
        const val INTENT_MEDIA_MESSAGE = "intent_media_message"
        const val IMAGE_TYPE = "image/*"
        const val UID = "uid"
        const val AVATAR = "avatar"
        const val STATUS = "status"
        const val NAME = "name"
        const val TYPE = "type"
        const val GUID = "guid"
        const val tabBar = "tabBar"
        @JvmField
        val EXTRA_MIME_DOC = arrayOf("text/plane", "text/html", "application/pdf", "application/msword", "application/vnd.ms.excel", "application/mspowerpoint", "application/zip")
        const val MEMBER_COUNT = "member_count"
        const val GROUP_MEMBER = "group_members"
        const val GROUP_NAME = "group_name"
        const val MEMBER_SCOPE = "member_scope"
        const val GROUP_OWNER = "group_owner"
        const val ID = "id"
        const val IS_ADD_MEMBER = "is_add_member"
        const val IS_BLOCKED_BY_ME = "is_blocked_by_me"
        const val SESSION_ID = "sessionId"
        const val INCOMING = "incoming"
        const val FROM_CALL_LIST = "from_call_list"
        const val JOIN_ONGOING = "join_ongoing_call"
        const val MESSAGE_TYPE_IMAGE_NAME = "file_name"
        const val MESSAGE_TYPE_IMAGE_URL = "file_url"
        const val MESSAGE_TYPE_IMAGE_MIME_TYPE = "file_mime"
        const val MESSAGE_TYPE_IMAGE_EXTENSION = "file_extension"
        const val MESSAGE_TYPE_IMAGE_SIZE = "file_size"
        const val SHOW_MODERATORLIST = "is_moderator"
        const val GROUP_DESC = "group_description"
        const val GROUP_PASSWORD = "group_password"
        const val GROUP_TYPE = "group_type"

        const val MESSAGE_CATEGORY = "message_category"
        const val MESSAGE_TYPE = "message_type"
        const val TEXTMESSAGE = "text_message"
        const val SENTAT = "sent_at"

        const val LOCATION = "LOCATION"
        const val CUSTOM_MESSAGE = "custom_message"
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"

        const val PARENT_ID = "parent_id"
        const val REPLY_COUNT = "reply_count"
        const val CONVERSATION_NAME = "conversation_name"

        const val STICKERS = "Sticker"
        const val REACTION_INFO = "reaction_info"

        const val WHITEBOARD = "extension_whiteboard"
        const val WRITEBOARD = "extension_document"
        const val URL = "url"

        const val MEETING = "meeting"
    }

    object Tab {
        const val Conversation = "conversations"
        const val User = "users"
        const val Group = "groups"
    }

    object RequestCode {
        const val GALLERY = 1
        const val CAMERA = 2
        const val FILE = 25
        const val BLOCK_USER = 7
        const val DELETE_GROUP = 8
        const val AUDIO = 3
        const val READ_STORAGE = 1
        const val RECORD = 3
        const val LOCATION = 14
    }

    object MapUrl {
        const val MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=380x220&markers=color:red|"
        const val MAP_ACCESS_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXX"
    }

    object MessageRequest {

        var messageTypesForUser: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //Custom Messages
                IntentStrings.LOCATION,
                IntentStrings.STICKERS,
                IntentStrings.WHITEBOARD,
                IntentStrings.WRITEBOARD
        ))
        var messageTypesForGroup: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.MESSAGE_TYPE_CUSTOM,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_TEXT,
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_FILE,
                //For Group Actions
                CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
                //Custom Messages
                IntentStrings.LOCATION,
                IntentStrings.STICKERS,
                IntentStrings.WHITEBOARD,
                IntentStrings.WRITEBOARD,
                IntentStrings.MEETING
        ))

        var messageCategoriesForGroup: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL,
                CometChatConstants.CATEGORY_ACTION))


        var messageCategoriesForUser: List<String> = ArrayList(Arrays.asList(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                CometChatConstants.CATEGORY_CALL))
    }
}