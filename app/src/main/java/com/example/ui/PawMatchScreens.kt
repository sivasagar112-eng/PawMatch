package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.Context
import com.example.data.Dog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// --------------------------------------------------------------------------------------------
// MAIN HUB SCREEN - Bottom Navigation Host
// --------------------------------------------------------------------------------------------
@Composable
fun MainHubScreen(viewModel: PawMatchViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Discover,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Discover) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Discover") },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Matches,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Matches) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Matches") },
                    label = { Text("Matches") }
                )
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.MapNearby,
                    onClick = { viewModel.navigateToTab(PawMatchTab.MapNearby) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Meetups,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Meetups) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Meetups") },
                    label = { Text("Meetups") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                PawMatchTab.Discover -> DiscoverTabScreen(viewModel)
                PawMatchTab.Matches -> MatchesTabScreen(viewModel)
                PawMatchTab.MapNearby -> MapNearbyTabScreen(viewModel)
                PawMatchTab.Meetups -> MeetupsTabScreen(viewModel)
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// TAB SCREENS
// --------------------------------------------------------------------------------------------

@Composable
fun DiscoverTabScreen(viewModel: PawMatchViewModel) {
    val filteredDogs by viewModel.filteredOtherDogs.collectAsStateWithLifecycle()
    
    if (filteredDogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = SoftBrown
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No dogs to discover", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        // TODO: Implement swipe card stack UI
        Text("Discover Tab", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun MatchesTabScreen(viewModel: PawMatchViewModel) {
    val matchedDogs by viewModel.matchedDogs.collectAsStateWithLifecycle()
    
    if (matchedDogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Terracotta
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No matches yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(matchedDogs.size) { index ->
                // TODO: Implement match card UI
                Text("Match: ${matchedDogs[index].name}")
            }
        }
    }
}

@Composable
fun MapNearbyTabScreen(viewModel: PawMatchViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Map Nearby Tab", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun MeetupsTabScreen(viewModel: PawMatchViewModel) {
    val meetups by viewModel.meetups.collectAsStateWithLifecycle()
    
    if (meetups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = GoldAccent
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No meetups scheduled", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(meetups.size) { index ->
                // TODO: Implement meetup card UI
                Text("Meetup: ${meetups[index].dogName}")
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// MATCH CELEBRATION OVERLAY
// --------------------------------------------------------------------------------------------

@Composable
fun MatchCelebrationOverlay(dog: Dog, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000)
        isVisible = false
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { isVisible = false; onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "IT'S A MATCH!",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Terracotta,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You and ${dog.name} liked each other!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AsyncImage(
                    model = dog.imageUrl,
                    contentDescription = dog.name,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { isVisible = false; onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "Start Chatting",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                    )
                }
            }
        }
    }
}
