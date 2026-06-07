package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Screen definition
sealed interface PawMatchScreen {
    object Splash : PawMatchScreen
    object OnboardingCarousel : PawMatchScreen
    object ProfileSetup : PawMatchScreen
    object MainHub : PawMatchScreen // Hosts standard bottom navigation tabs
}

// Sub-tabs in Main Hub
enum class PawMatchTab {
    Discover,
    Matches,
    MapNearby,
    Meetups
}

class PawMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PawMatchRepository

    // Native route states
    private val _currentScreen = MutableStateFlow<PawMatchScreen>(PawMatchScreen.Splash)
    val currentScreen: StateFlow<PawMatchScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(PawMatchTab.Discover)
    val currentTab: StateFlow<PawMatchTab> = _currentTab.asStateFlow()

    // Filter states for Map & Discover
    val filterBreedQuery = MutableStateFlow("")
    val filterRadius = MutableStateFlow(10f) // in km

    // Onboarding carousel step (0, 1, 2)
    private val _onboardingStep = MutableStateFlow(0)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Core domain collections
    val userDog: StateFlow<Dog?>
    val matchedDogs: StateFlow<List<Dog>>
    val allOtherDogsListCombined: StateFlow<List<Dog>>
    val meetups: StateFlow<List<Meetup>>

    // Filters other dogs reactively based on Breed query
    val filteredOtherDogs: StateFlow<List<Dog>>

    // Navigation Contexts (Details / Chats)
    private val _selectedDogForDetail = MutableStateFlow<Dog?>(null)
    val selectedDogForDetail: StateFlow<Dog?> = _selectedDogForDetail.asStateFlow()

    private val _selectedDogForChat = MutableStateFlow<Dog?>(null)
    val selectedDogForChat: StateFlow<Dog?> = _selectedDogForChat.asStateFlow()

    private val _selectedDogForMeetupRequest = MutableStateFlow<Dog?>(null)
    val selectedDogForMeetupRequest: StateFlow<Dog?> = _selectedDogForMeetupRequest.asStateFlow()

    // Messages flow linked dynamic to chat selection
    private val _activeChatMessages = MutableStateFlow<List<Message>>(emptyList())
    val activeChatMessages: StateFlow<List<Message>> = _activeChatMessages.asStateFlow()

    // Match Celebration overlay trigger
    private val _celebrationMatch = MutableStateFlow<Dog?>(null)
    val celebrationMatch: StateFlow<Dog?> = _celebrationMatch.asStateFlow()

    // Map Specific layout states
    private val _selectedMapDogPinId = MutableStateFlow<Int?>(null)
    val selectedMapDogPinId: StateFlow<Int?> = _selectedMapDogPinId.asStateFlow()

    private val _isMapViewLayout = MutableStateFlow(true) // Switch between Map and list layout
    val isMapViewLayout: StateFlow<Boolean> = _isMapViewLayout.asStateFlow()

    // Profile Setup Temp state
    val setupName = MutableStateFlow("")
    val setupBreed = MutableStateFlow("")
    val setupAge = MutableStateFlow("")
    val setupGender = MutableStateFlow("Male")
    val setupLocation = MutableStateFlow("")
    val setupBio = MutableStateFlow("")
    val setupImageUrl = MutableStateFlow("https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600")

    // Real API Backend variables
    val dailyPupFact = MutableStateFlow("Loading real-time dog facts from our REST api backend...")
    val isFetchingImage = MutableStateFlow(false)
    val breedApiStatus = MutableStateFlow("")

    // Onboarding Title/Subtitle resources
    val onboardingSlides = listOf(
        OnboardingSlide(
            title = "Find Same Breed",
            desc = "Discovery tailored specifically for purebred companionship matching nearby breed advocates.",
            iconName = "pets"
        ),
        OnboardingSlide(
            title = "Connect With Owners",
            desc = "Build dynamic social links with dog parents sharing your high-end lifestyle standards.",
            iconName = "diversity_1"
        ),
        OnboardingSlide(
            title = "Plan Elite Meetups",
            desc = "Co-author premium playdates, private park strolls and healthy agility course events.",
            iconName = "calendar_month"
        )
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PawMatchRepository(database)

        userDog = repository.userDog.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        matchedDogs = repository.matchedDogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allOtherDogsListCombined = repository.otherDogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        meetups = repository.meetups.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Reactive sorting and filtering
        val swipedDogIds = repository.allSwipes.map { swipes ->
            swipes.map { it.dogId }.toSet()
        }

        filteredOtherDogs = combine(
            allOtherDogsListCombined,
            filterBreedQuery,
            filterRadius,
            swipedDogIds
        ) { list, query, radius, swipedIds ->
            list.filter {
                val matchesQuery = if (query.isEmpty()) {
                    true
                } else {
                    it.breed.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
                }
                val matchesRadius = it.distance <= radius
                // Exclude matches that are already confirmed & dogs that are already swiped
                matchesQuery && matchesRadius && !it.isMatched && !swipedIds.contains(it.id)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Fetch dynamic backend data on startup
        fetchBackendData()

        // Persistent State survival pipeline using Android SharedPreferences
        val sharedPrefs = application.getSharedPreferences("pawmatch_prefs", android.content.Context.MODE_PRIVATE)

        // Restore screen state
        val savedScreen = sharedPrefs.getString("current_screen", "Splash")
        _currentScreen.value = when (savedScreen) {
            "OnboardingCarousel" -> PawMatchScreen.OnboardingCarousel
            "ProfileSetup" -> PawMatchScreen.ProfileSetup
            "MainHub" -> PawMatchScreen.MainHub
            else -> PawMatchScreen.Splash
        }

        // Restore setup form states
        setupName.value = sharedPrefs.getString("setup_name", "") ?: ""
        setupBreed.value = sharedPrefs.getString("setup_breed", "") ?: ""
        setupAge.value = sharedPrefs.getString("setup_age", "") ?: ""
        setupGender.value = sharedPrefs.getString("setup_gender", "Male") ?: "Male"
        setupLocation.value = sharedPrefs.getString("setup_location", "") ?: ""
        setupBio.value = sharedPrefs.getString("setup_bio", "") ?: ""
        setupImageUrl.value = sharedPrefs.getString("setup_image_url", "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600") ?: "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600"

        // Auto-save form inputs and active navigation states reactively
        viewModelScope.launch {
            _currentScreen.collect { screen ->
                val name = when (screen) {
                    is PawMatchScreen.Splash -> "Splash"
                    is PawMatchScreen.OnboardingCarousel -> "OnboardingCarousel"
                    is PawMatchScreen.ProfileSetup -> "ProfileSetup"
                    is PawMatchScreen.MainHub -> "MainHub"
                }
                sharedPrefs.edit().putString("current_screen", name).apply()
            }
        }
        viewModelScope.launch {
            setupName.collect { sharedPrefs.edit().putString("setup_name", it).apply() }
        }
        viewModelScope.launch {
            setupBreed.collect { sharedPrefs.edit().putString("setup_breed", it).apply() }
        }
        viewModelScope.launch {
            setupAge.collect { sharedPrefs.edit().putString("setup_age", it).apply() }
        }
        viewModelScope.launch {
            setupGender.collect { sharedPrefs.edit().putString("setup_gender", it).apply() }
        }
        viewModelScope.launch {
            setupLocation.collect { sharedPrefs.edit().putString("setup_location", it).apply() }
        }
        viewModelScope.launch {
            setupBio.collect { sharedPrefs.edit().putString("setup_bio", it).apply() }
        }
        viewModelScope.launch {
            setupImageUrl.collect { sharedPrefs.edit().putString("setup_image_url", it).apply() }
        }

        // Listen for database profile updates to populate form ONLY ONCE on startup
        viewModelScope.launch {
            userDog.collect { dog ->
                if (dog != null && sharedPrefs.getString("setup_db_populated", "false") == "false") {
                    setupName.value = dog.name
                    setupBreed.value = dog.breed
                    setupAge.value = dog.age.toString()
                    setupGender.value = dog.gender
                    setupLocation.value = dog.location
                    setupBio.value = dog.bio
                    setupImageUrl.value = dog.imageUrl
                    sharedPrefs.edit().putString("setup_db_populated", "true").apply()
                }
            }
        }
    }

    // Navigation and Flow Management
    fun navigateTo(screen: PawMatchScreen) {
        _currentScreen.value = screen
    }

    fun navigateToTab(tab: PawMatchTab) {
        _currentTab.value = tab
        // Clear full screen detail overlays on tab switch
        _selectedDogForDetail.value = null
        _selectedDogForChat.value = null
        _selectedDogForMeetupRequest.value = null
    }

    // Onboarding Steps
    fun nextOnboarding() {
        if (_onboardingStep.value < onboardingSlides.size - 1) {
            _onboardingStep.value += 1
        } else {
            navigateTo(PawMatchScreen.ProfileSetup)
        }
    }

    fun skipOnboarding() {
        navigateTo(PawMatchScreen.ProfileSetup)
    }

    // Finish sign up and dog profile setup
    fun saveProfileAndComplete() {
        viewModelScope.launch {
            val ageInt = setupAge.value.toIntOrNull() ?: 2
            val newDog = Dog(
                id = 0,
                name = setupName.value,
                breed = setupBreed.value,
                age = ageInt,
                gender = setupGender.value,
                location = setupLocation.value,
                distance = 0.0,
                imageUrl = setupImageUrl.value,
                bio = setupBio.value,
                ownerName = "Me (Siva S.)",
                ownerAvatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=150",
                isUserDog = true
            )
            repository.saveUserDog(newDog)
            navigateTo(PawMatchScreen.MainHub)
        }
    }

    // Swipe card events
    val isRefreshingStack = MutableStateFlow(false)

    fun triggerAutoReload() {
        viewModelScope.launch {
            isRefreshingStack.value = true
            kotlinx.coroutines.delay(1800) // Premium 1.8-second loading experience
            repository.reloadMoreDogs()
            isRefreshingStack.value = false
        }
    }

    fun swipeRight(dog: Dog) {
        viewModelScope.launch {
            repository.insertSwipe(dogId = dog.id, liked = true)
            // Trigger beautiful celebration overlay on a like transition
            _celebrationMatch.value = dog
        }
    }

    fun swipeLeft(dog: Dog) {
        viewModelScope.launch {
            repository.insertSwipe(dogId = dog.id, liked = false)
        }
    }

    fun deleteConversation(dog: Dog) {
        viewModelScope.launch {
            repository.deleteConversation(dog.id)
        }
    }

    fun dismissCelebration() {
        _celebrationMatch.value = null
        // Auto transfer to chat portal of new match for seamless UX
        navigateToTab(PawMatchTab.Matches)
    }

    // Detail modal overlay
    fun openDogDetail(dog: Dog) {
        _selectedDogForDetail.value = dog
    }

    fun closeDogDetail() {
        _selectedDogForDetail.value = null
    }

    // Chat portal orchestration
    fun openChat(dog: Dog) {
        _selectedDogForChat.value = dog
        // Bind reactivity to database message flows
        viewModelScope.launch {
            repository.getMessagesForDog(dog.id).collect { messagesList ->
                _activeChatMessages.value = messagesList
            }
        }
    }

    fun closeChat() {
        _selectedDogForChat.value = null
    }

    fun sendChatMessage(text: String) {
        val activeDog = _selectedDogForChat.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertMessage(
                dogId = activeDog.id,
                text = text,
                senderName = "Me",
                isFromUser = true
            )
            
            // Trigger smart automated mock replies relative to dog name/personality
            simulateMockResponse(activeDog, text)
        }
    }

    private suspend fun simulateMockResponse(matchedDog: Dog, triggerMessageText: String) {
        // Simple delay to make chat feel realistic
        kotlinx.coroutines.delay(1200)
        
        val replyText = when {
            triggerMessageText.contains("hi", ignoreCase = true) || triggerMessageText.contains("hello", ignoreCase = true) -> {
                "Hi there! ${matchedDog.name} would absolutely love to play. Where are you guys thinking of doing playdates?"
            }
            triggerMessageText.contains("meetup", ignoreCase = true) || triggerMessageText.contains("park", ignoreCase = true) || triggerMessageText.contains("date", ignoreCase = true) -> {
                "That sounds amazing! Feel free to send over a Meetup scheduling request with the location details, and we'll confirm it."
            }
            else -> {
                "Woof! That sounds fantastic. ${matchedDog.name} is super energetic today, let's lock in a time to play!"
            }
        }

        repository.insertMessage(
            dogId = matchedDog.id,
            text = replyText,
            senderName = matchedDog.name,
            isFromUser = false
        )
    }

    // Meetup Scheduling flows
    fun openMeetupSetup(dog: Dog) {
        _selectedDogForMeetupRequest.value = dog
    }

    fun closeMeetupSetup() {
        _selectedDogForMeetupRequest.value = null
    }

    fun scheduleMeetup(date: String, time: String, location: String, note: String) {
        val targetDog = _selectedDogForMeetupRequest.value ?: return
        viewModelScope.launch {
            val newMeetup = Meetup(
                dogId = targetDog.id,
                dogName = targetDog.name,
                dogImageUrl = targetDog.imageUrl,
                date = date,
                time = time,
                location = location,
                note = note,
                status = "Pending"
            )
            repository.createMeetup(newMeetup)
            _selectedDogForMeetupRequest.value = null
            
            // Auto notify owner and mock transition past few seconds
            navigateToTab(PawMatchTab.Meetups)
            
            // Update meetup status to confirmed automatically after 4 seconds to simulate response!
            val recentMeetups = repository.meetups.first()
            val latest = recentMeetups.firstOrNull { it.dogId == targetDog.id }
            if (latest != null) {
                kotlinx.coroutines.delay(4000)
                repository.updateMeetupStatus(latest.id, "Confirmed")
            }
        }
    }

    fun respondToMeetup(meetupId: Int, accept: Boolean) {
        viewModelScope.launch {
            val status = if (accept) "Confirmed" else "Declined"
            repository.updateMeetupStatus(meetupId, status)
        }
    }

    // Toggle Map and nearby list views
    fun setMapPinSelection(dogId: Int?) {
        _selectedMapDogPinId.value = dogId
    }

    fun toggleLayoutView() {
        _isMapViewLayout.value = !_isMapViewLayout.value
    }

    // --------------------------------------------------------------------------------------------
    // REAL BACKEND API INTEGRATIONS (Retrofit + REST endpoints)
    // --------------------------------------------------------------------------------------------
    fun fetchBackendData() {
        viewModelScope.launch {
            try {
                // Fetch dynamic dog fact from real REST backend
                val response = RetrofitClient.dogFactsApiService.getDogFacts(1)
                if (response.isSuccessful && response.body()?.success == true) {
                    val fact = response.body()?.facts?.firstOrNull()
                    if (!fact.isNullOrBlank()) {
                        dailyPupFact.value = fact
                    }
                }
            } catch (e: Exception) {
                dailyPupFact.value = "Active dogs require regular exercise and pedigree care to maintain peak cardiovascular fitness!"
            }
        }
    }

    fun fetchRandomBreedImage(rawBreed: String) {
        if (rawBreed.isBlank()) return
        val formattedBreed = rawBreed.trim().lowercase()
            .replace(" ", "/") // Handle sub-breeds like 'collie/border' or fallback to split
        
        // Find single term
        val apiBreed = if (formattedBreed.contains("/")) {
            formattedBreed.split("/").first() // e.g., 'golden' for 'golden retriever' or 'german' for 'german shepherd'
        } else {
            formattedBreed
        }

        viewModelScope.launch {
            isFetchingImage.value = true
            breedApiStatus.value = "Connecting to Dog CEO API..."
            try {
                // Query real REST backend
                val response = RetrofitClient.dogApiService.getRandomBreedImage(apiBreed)
                if (response.isSuccessful) {
                    val url = response.body()?.message
                    if (!url.isNullOrBlank()) {
                        setupImageUrl.value = url
                        setupBreed.value = rawBreed.trim()
                        breedApiStatus.value = "Success! Loaded live photo for $rawBreed."
                    } else {
                        breedApiStatus.value = "No dynamic image found. Using default."
                    }
                } else {
                    // Try general fallback image query or search term
                    val generalResponse = RetrofitClient.dogApiService.getRandomBreedImage("retriever")
                    if (generalResponse.isSuccessful) {
                        setupImageUrl.value = generalResponse.body()?.message ?: setupImageUrl.value
                        breedApiStatus.value = "Loaded general retriever portrait fallback."
                    } else {
                        breedApiStatus.value = "Backend returned error code ${response.code()}."
                    }
                }
            } catch (e: Exception) {
                breedApiStatus.value = "Backend Offline: Using cached preset photo."
            } finally {
                isFetchingImage.value = false
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val desc: String,
    val iconName: String
)
