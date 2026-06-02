package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DogDao {
    @Query("SELECT * FROM dogs WHERE isUserDog = 0")
    fun getAllOtherDogs(): Flow<List<Dog>>

    @Query("SELECT * FROM dogs WHERE isUserDog = 1 LIMIT 1")
    fun getUserDog(): Flow<Dog?>

    @Query("SELECT * FROM dogs WHERE id = :id LIMIT 1")
    fun getDogByIdFlow(id: Int): Flow<Dog?>

    @Query("SELECT * FROM dogs WHERE id = :id LIMIT 1")
    suspend fun getDogById(id: Int): Dog?

    @Query("SELECT * FROM dogs WHERE isMatched = 1")
    fun getMatchedDogs(): Flow<List<Dog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDog(dog: Dog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDogs(dogs: List<Dog>)

    @Update
    suspend fun updateDog(dog: Dog)

    @Query("UPDATE dogs SET isMatched = :matched WHERE id = :id")
    suspend fun setMatchedStatus(id: Int, matched: Boolean)

    @Query("DELETE FROM dogs")
    suspend fun clearAllDogs()
}

@Dao
interface SwipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipe(swipe: DogSwipe)

    @Query("SELECT * FROM swipes")
    fun getAllSwipes(): Flow<List<DogSwipe>>

    @Query("DELETE FROM swipes")
    suspend fun deleteAllSwipes()

    @Query("DELETE FROM swipes WHERE dogId = :dogId")
    suspend fun deleteSwipeByDogId(dogId: Int)
}

@Dao
interface MeetupDao {
    @Query("SELECT * FROM meetups ORDER BY id DESC")
    fun getAllMeetups(): Flow<List<Meetup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetup(meetup: Meetup): Long

    @Update
    suspend fun updateMeetup(meetup: Meetup)

    @Query("UPDATE meetups SET status = :status WHERE id = :id")
    suspend fun updateMeetupStatus(id: Int, status: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE dogId = :dogId ORDER BY timestamp ASC")
    fun getMessagesForDog(dogId: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE dogId = :dogId")
    suspend fun deleteMessagesForDog(dogId: Int)
}
