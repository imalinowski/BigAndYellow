package com.malinowski.bigandyellow.model.db.dao

import androidx.room.*
import com.malinowski.bigandyellow.model.data.REACTIONS_TABLE
import com.malinowski.bigandyellow.model.data.Reaction
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ReactionDao {
    @Query("SELECT * FROM $REACTIONS_TABLE")
    fun getAll(): Single<List<Reaction>>

    @Query("SELECT * FROM $REACTIONS_TABLE WHERE message_id = :id")
    fun getByMessageId(id: Int): Single<List<Reaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reaction: List<Reaction>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reaction: Reaction): Completable

    @Delete
    fun delete(reaction: Reaction): Single<Int>

    @Query("DELETE FROM $REACTIONS_TABLE WHERE message_id = :id")
    fun deleteByMessageId(id: Int): Completable
}