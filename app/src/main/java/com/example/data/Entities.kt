package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogs")
data class Dog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val breed: String,
    val age: Int, // in years (or months)
    val gender: String, // "Male" or "Female"
    val location: String, // neighborhood
    val distance: Double, // in km
    val imageUrl: String,
    val bio: String,
    val ownerName: String,
    val ownerAvatarUrl: String,
    val isUserDog: Boolean = false,
    val isMatched: Boolean = false
)

@Entity(tableName = "swipes")
data class DogSwipe(
    @PrimaryKey val dogId: Int,
    val liked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meetups")
data class Meetup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dogId: Int,
    val dogName: String,
    val dogImageUrl: String,
    val date: String,
    val time: String,
    val location: String,
    val note: String,
    val status: String // "Pending", "Confirmed", "Declined"
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dogId: Int, // The match ID this message belongs to
    val text: String,
    val senderName: String, // Sender name (e.g. user dog name vs matched dog name)
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
