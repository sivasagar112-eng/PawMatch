package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PawMatchRepository(private val db: AppDatabase) {
    val otherDogs: Flow<List<Dog>> = db.dogDao().getAllOtherDogs()
    val userDog: Flow<Dog?> = db.dogDao().getUserDog()
    val matchedDogs: Flow<List<Dog>> = db.dogDao().getMatchedDogs()
    val meetups: Flow<List<Meetup>> = db.meetupDao().getAllMeetups()
    val allSwipes: Flow<List<DogSwipe>> = db.swipeDao().getAllSwipes()

    fun getMessagesForDog(dogId: Int): Flow<List<Message>> {
        return db.messageDao().getMessagesForDog(dogId)
    }

    suspend fun getDogById(dogId: Int): Dog? = db.dogDao().getDogById(dogId)

    suspend fun insertSwipe(dogId: Int, liked: Boolean) {
        db.swipeDao().insertSwipe(DogSwipe(dogId, liked))
        if (liked) {
            // Trigger a match! Let's update matched state on the dog
            db.dogDao().setMatchedStatus(dogId, true)
            
            // Auto inject intro message from the match dog
            val dog = getDogById(dogId)
            if (dog != null) {
                db.messageDao().insertMessage(
                    Message(
                        dogId = dogId,
                        text = "Hi! I saw your pup's profile and think they'd make a perfect partner for playdates, let's schedule a meetup!",
                        senderName = dog.name,
                        isFromUser = false
                    )
                )
            }
        }
    }

    suspend fun insertMessage(dogId: Int, text: String, senderName: String, isFromUser: Boolean) {
        db.messageDao().insertMessage(
            Message(
                dogId = dogId,
                text = text,
                senderName = senderName,
                isFromUser = isFromUser
            )
        )
    }

    suspend fun createMeetup(meetup: Meetup): Long {
        return db.meetupDao().insertMeetup(meetup)
    }

    suspend fun updateMeetupStatus(meetupId: Int, status: String) {
        db.meetupDao().updateMeetupStatus(meetupId, status)
    }

    suspend fun saveUserDog(dog: Dog) {
        // Find if old user dog exists
        val current = db.dogDao().getUserDog().first()
        if (current != null) {
            db.dogDao().updateDog(dog.copy(id = current.id, isUserDog = true))
        } else {
            db.dogDao().insertDog(dog.copy(id = 0, isUserDog = true))
        }
    }

    suspend fun prepopulateIfEmpty() {
        val currentOthers = db.dogDao().getAllOtherDogs().first()
        if (currentOthers.isEmpty()) {
            val defaultDogs = listOf(
                Dog(
                    name = "Luna",
                    breed = "Golden Retriever",
                    age = 1,
                    gender = "Female",
                    location = "Marine Drive",
                    distance = 1.2,
                    imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600",
                    bio = "Loves morning beach jogs and premium organic salmon biscuits. Looking for a friendly playdate partner!",
                    ownerName = "Anita S.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150"
                ),
                Dog(
                    name = "Kaiser",
                    breed = "Husky",
                    age = 3,
                    gender = "Male",
                    location = "Bandra West",
                    distance = 2.4,
                    imageUrl = "https://images.unsplash.com/photo-1531804055935-76f44d7c3621?auto=format&fit=crop&q=80&w=600",
                    bio = "Vigorous runner with striking ice blue eyes. Confident, friendly and pedigree champion.",
                    ownerName = "Vikram R.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=150"
                ),
                Dog(
                    name = "Biscuit",
                    breed = "Beagle",
                    age = 2,
                    gender = "Male",
                    location = "Juhu",
                    distance = 3.1,
                    imageUrl = "https://images.unsplash.com/photo-1505628346881-b72b27e84530?auto=format&fit=crop&q=80&w=600",
                    bio = "Expert digger and professional cuddle. Knows 15 different tricks, highly affectionate!",
                    ownerName = "Rohan M.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=150"
                ),
                Dog(
                    name = "Athena",
                    breed = "German Shepherd",
                    age = 3,
                    gender = "Female",
                    location = "Colaba",
                    distance = 4.8,
                    imageUrl = "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&q=80&w=600",
                    bio = "Loyal protector with premium certification. Highly active, loves sniffing courses and swimming.",
                    ownerName = "Siddharth K.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=150"
                ),
                Dog(
                    name = "Winston",
                    breed = "French Bulldog",
                    age = 2,
                    gender = "Male",
                    location = "Worli Sea Face",
                    distance = 1.9,
                    imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&q=80&w=600",
                    bio = "Calm, couch potato who loves posing and modeling. Gourmet treat enthusiast.",
                    ownerName = "Kiara D.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=150"
                ),
                Dog(
                    name = "Mochi",
                    breed = "Pembroke Corgi",
                    age = 1,
                    gender = "Female",
                    location = "Powai",
                    distance = 5.2,
                    imageUrl = "https://images.unsplash.com/photo-1612536057832-2ff7eed58194?auto=format&fit=crop&q=80&w=600",
                    bio = "Short legs, giant heart! Loves ball chasing and makes adorable puppy eyes.",
                    ownerName = "Neha S.",
                    ownerAvatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=150"
                )
            )
            db.dogDao().insertDogs(defaultDogs)
        }

        // We also create a default user dog so the app is immediately alive and has beautiful context!
        val currentUserDog = db.dogDao().getUserDog().first()
        if (currentUserDog == null) {
            val firstUserDog = Dog(
                name = "Bruno",
                breed = "Golden Retriever",
                age = 2,
                gender = "Male",
                location = "Altamount Road",
                distance = 0.0,
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600",
                bio = "Majestic Golden Retriever looking for neighbors of the same breed for healthy run-offs and play sessions.",
                ownerName = "Siva S.",
                ownerAvatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=150",
                isUserDog = true
            )
            db.dogDao().insertDog(firstUserDog)
        }
    }

    suspend fun deleteAllSwipes() {
        db.swipeDao().deleteAllSwipes()
    }

    suspend fun deleteConversation(dogId: Int) {
        db.dogDao().setMatchedStatus(dogId, false)
        db.swipeDao().deleteSwipeByDogId(dogId)
        db.messageDao().deleteMessagesForDog(dogId)
    }

    suspend fun reloadMoreDogs() {
        // Clear all swipes so previously swiped-left dogs can be swiped again (excluding matched ones)
        db.swipeDao().deleteAllSwipes()

        // Insert new randomized high-quality dog profiles to guarantee fresh matches
        val dogNames = listOf("Cooper", "Bella", "Zoe", "Rocky", "Teddy", "Daisy", "Mocha", "Ziggy", "Waffles", "Coco")
        val breeds = listOf("Cocker Spaniel", "Labrador", "Poodle", "Chow Chow", "Dachshund", "Border Collie", "Shih Tzu", "Pug")
        val bios = listOf(
            "Enjoys afternoon naps, warm pet beds, and premium organic biscuits.",
            "Super friendly agility champion! Loves dog-runs and interactive fetch.",
            "Gentle soul looking for dynamic Same-Breed companions.",
            "High energy buddy who is fully vaccinated and pedigree certified.",
            "A professional cuddle champion who is great with other dogs!"
        )
        val images = listOf(
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=600",
            "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&q=80&w=600",
            "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&q=80&w=600",
            "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&q=80&w=600",
            "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?auto=format&fit=crop&q=80&w=600"
        )
        val locations = listOf("Bandra West", "Juhu Beach", "Colaba", "Khar", "Marine Drive", "Powai", "Worli Sea Face")
        val owners = listOf("Siddharth M.", "Neha K.", "Pooja R.", "Arjun V.", "Rohan S.")

        val rand = java.util.Random()
        val newDogs = (1..4).map { i ->
            Dog(
                name = dogNames.random(),
                breed = breeds.random(),
                age = (1..5).random(),
                gender = if (rand.nextBoolean()) "Male" else "Female",
                location = locations.random(),
                distance = Math.round((0.5 + rand.nextDouble() * 5.0) * 10.0) / 10.0,
                imageUrl = images.random(),
                bio = bios.random(),
                ownerName = owners.random(),
                ownerAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150"
            )
        }
        db.dogDao().insertDogs(newDogs)
    }
}
