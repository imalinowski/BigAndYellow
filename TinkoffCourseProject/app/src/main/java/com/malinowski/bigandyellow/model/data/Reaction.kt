package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    @SerialName("user_id") var userId: Int,
    @SerialName("emoji_code") private val code: String,
    @SerialName("emoji_name") val name: String,
) {
    fun getUnicode() = processUnicode(code)
}

private const val TABLE_NAME = "UnitedReactions"

@Entity(tableName = TABLE_NAME)
@TypeConverters(ListStringConverter::class)
data class UnitedReaction(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "users_id") val usersId: MutableList<Int>,
    private val unicode: String
) {
    var messageId = 0
    fun getUnicode() = processUnicode(unicode)
}

private fun processUnicode(code: String): String {
    return try {
        val hex = code.toInt(16)
        String(Character.toChars(hex))
    } catch (e: NumberFormatException) {
        code
    }
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<UnitedReaction>>

    @Query("SELECT * FROM $TABLE_NAME WHERE messageId = :id")
    fun getByMessageId(id: Int): Single<List<UnitedReaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<UnitedReaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: UnitedReaction)

    @Delete
    fun delete(topic: UnitedReaction): Single<Int>
}


class ListStringConverter() {
    @TypeConverter
    fun listToString(value: MutableList<Int>?): String? {
        return value?.joinToString()
    }

    @TypeConverter
    fun stringToList(value: String?): MutableList<Int>? {
        return value?.replace(" ", "")?.split(",")?.map { Integer.valueOf(it) }?.toMutableList()
    }
}

val emojiMap: HashMap<String, String> = hashMapOf(
    "grinning" to "\uD83D\uDE00",
    "heart" to "❤",
    "tada" to "\uD83C\uDF89",
    "+1" to "\uD83D\uDC4D",
    "smile" to "\uD83D\uDE42",
    "expressionless" to "\uD83D\uDE11",
    "octopus" to "\uD83D\uDC19",
    "working_on_it" to "\uD83D\uDEE0",
    "stuck_out_tongue_closed_eyes" to "\uD83D\uDE1D",
    "thinking" to "\uD83E\uDD14",
    "sunglasses" to "\uD83D\uDE0E",
    "silence" to "\uD83E\uDD10",
    "money_face" to "\uD83E\uDD11",
    "smirk" to "\uD83D\uDE12",
    "hug" to "\uD83E\uDD17",
    "nerd" to "\uD83E\uDD13",
    "scream" to "\uD83D\uDE31",
    "poop" to "\uD83D\uDCA9",
    "flushed" to "\uD83D\uDE33",
    "rage" to "\uD83D\uDE21",
    "fear" to "\uD83D\uDE28",
    "pensive" to "\uD83D\uDE14",
    "nauseated" to "\uD83E\uDD22",
    "rolling_eyes" to "\uD83D\uDE44",
    "ghost" to "\uD83D\uDC7B",
    "alien" to "\uD83D\uDC7D",
    "middle_finger" to "\uD83D\uDD95",
    "jack_o_lantern" to "\uD83C\uDF83",
)