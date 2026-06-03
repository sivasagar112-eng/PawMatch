package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.draw.clipToBounds
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.data.Dog
import com.example.data.Meetup
import com.example.data.Message
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
// OSMDroid real map imports
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
// Location permission imports
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawMatchApp(viewModel: PawMatchViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val celebrationMatch by viewModel.celebrationMatch.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Core screen dispatch rendering
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is PawMatchScreen.Splash -> SplashScreen(viewModel)
                is PawMatchScreen.OnboardingCarousel -> OnboardingCarouselScreen(viewModel)
                is PawMatchScreen.ProfileSetup -> DogProfileSetupScreen(viewModel)
                is PawMatchScreen.MainHub -> MainHubScreen(viewModel)
            }
        }

        // It's a Match celebration overlay pops up automatically over any screen on like triggers!
        celebrationMatch?.let { matchedDog ->
            MatchCelebrationOverlay(
                dog = matchedDog,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}

// --------------------------------------------------------------------------------------------
// 1. SPLASH SCREEN
// --------------------------------------------------------------------------------------------
@Composable
fun SplashScreen(viewModel: PawMatchViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(WarmCream, Soapstone)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative abstract circular background lines matching modern premium styling
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Terracotta.copy(alpha = 0.05f),
                radius = 450.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.2f)
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.03f),
                radius = 300.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.8f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High fidelity gold-embossed dog paw logo symbol
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(GoldAccent, Terracotta)
                            ),
                            alpha = 0.15f
                        )
                    }
                    .padding(20.dp)
            ) {
                // Paw icon vector drawing via Standard Vector API
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "PawMatch Logo Icon",
                    tint = Terracotta,
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Premium Bold Italics Serif Branding Title
            Text(
                text = "PawMatch",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = WarmBrown,
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Premium Sub-tagline
            Text(
                text = "Find your pup's perfect match nearby",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = SoftBrown,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Dynamic Get Started CTA Pill
            Button(
                onClick = { viewModel.navigateTo(PawMatchScreen.OnboardingCarousel) },
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(9999.dp),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 18.dp),
                modifier = Modifier
                    .shadow(12.dp, shape = CircleShape, ambientColor = SlateShadow, spotColor = SlateShadow)
                    .height(56.dp)
            ) {
                Text(
                    text = "ENTER PAWMATCH ELITE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Proceed arrow",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 2. ONBOARDING CAROUSEL SCREEN
// --------------------------------------------------------------------------------------------
@Composable
fun OnboardingCarouselScreen(viewModel: PawMatchViewModel) {
    val currentStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val slide = viewModel.onboardingSlides[currentStep]

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary skip button
                TextButton(
                    onClick = { viewModel.skipOnboarding() },
                    colors = ButtonDefaults.textButtonColors(contentColor = SoftBrown),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Interactive Progress dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    viewModel.onboardingSlides.forEachIndexed { i, _ ->
                        val active = i == currentStep
                        val width by animateDpAsState(targetValue = if (active) 24.dp else 8.dp, label = "dotWidth")
                        val color = if (active) Terracotta else Soapstone

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Primary active matching next button
                Button(
                    onClick = { viewModel.nextOnboarding() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmBrown),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = if (currentStep == viewModel.onboardingSlides.lastIndex) "SETUP" else "NEXT",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White, letterSpacing = 1.sp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large elegant graphics overlay placeholder mimicking high-end mockups
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .drawBehind {
                            drawCircle(
                                color = Soapstone,
                                radius = 100.dp.toPx()
                            )
                        }
                ) {
                    // Modern styled adaptive icons for onboarding slides
                    val icon = when (slide.iconName) {
                        "pets" -> Icons.Default.Pets
                        "diversity_1" -> Icons.Default.FavoriteBorder
                        else -> Icons.Default.DateRange
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = slide.title,
                        tint = GoldAccent,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Premium Bold Typography Headline
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.headlineLarge.copy(color = WarmBrown),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Clear informative body text
                Text(
                    text = slide.desc,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = SoftBrown,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// IMAGE CROP AND GALLERY SAVE HELPERS
// --------------------------------------------------------------------------------------------
private fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap): Uri? {
    val filename = "PawMatch_${System.currentTimeMillis()}.jpg"
    val resolver = context.contentResolver
    
    // Method A: Modern API 29+ Scope Storage Pathway
    try {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/PawMatch")
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            resolver.openOutputStream(imageUri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            
            // Broadcast scan file to notify Android Media Scanner
            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            context.sendBroadcast(mediaScanIntent)
            
            // Explicitly scan the newly created file via MediaScannerConnection
            try {
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(imageUri.toString()),
                    arrayOf("image/jpeg")
                ) { _, _ -> }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            
            return imageUri
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }

    // Method B: Robust Legacy / General Fallback Compatibility Pathway (creates a copy straight in System media pictures directory)
    try {
        val savedUriStr = android.provider.MediaStore.Images.Media.insertImage(
            resolver,
            bitmap,
            filename,
            "PawMatch Profile Photo"
        )
        if (!savedUriStr.isNullOrEmpty()) {
            val legacyUri = Uri.parse(savedUriStr)
            
            val scanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = legacyUri
            context.sendBroadcast(scanIntent)
            
            return legacyUri
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    
    return null
}

private fun convertToSoftwareBitmap(bitmap: Bitmap): Bitmap {
    if (bitmap.isRecycled) return bitmap
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && 
        bitmap.config == Bitmap.Config.HARDWARE) {
        try {
            val softwareCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            if (softwareCopy != null) return softwareCopy
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        try {
            val softwareBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(softwareBitmap)
            val paint = android.graphics.Paint()
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return softwareBitmap
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
    if (!bitmap.isMutable) {
        try {
            val softwareCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            if (softwareCopy != null) return softwareCopy
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
    return bitmap
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val isLocalFile = uri.scheme == "file"
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        if (isLocalFile) {
            val path = uri.path
            if (path != null) {
                android.graphics.BitmapFactory.decodeFile(path, options)
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
            }
        }
        
        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) {
            val loaded = if (isLocalFile) {
                val path = uri.path
                if (path != null) android.graphics.BitmapFactory.decodeFile(path) else null
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }
            return if (loaded != null) convertToSoftwareBitmap(loaded) else null
        }
        
        // Target a maximum of 1080px for profile pics to avoid OOM crashes
        val maxDimension = 1080
        var inSample = 1
        if (width > maxDimension || height > maxDimension) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while ((halfWidth / inSample) >= maxDimension && (halfHeight / inSample) >= maxDimension) {
                inSample *= 2
            }
        }
        
        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        
        var decoded = if (isLocalFile) {
            val path = uri.path
            if (path != null) android.graphics.BitmapFactory.decodeFile(path, decodeOptions) else null
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }
        
        if (decoded != null) {
            decoded = convertToSoftwareBitmap(decoded)
        }
        decoded
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}

/**
 * Copies any content:// or media/picker URI into our app's cache directory
 * and returns a FileProvider URI the system crop app can read.
 * This is required for Android photo picker URIs (content://media/picker_...)
 * which third-party apps (including the system crop tool) cannot access directly.
 */
private fun copyUriToCache(context: android.content.Context, sourceUri: Uri): Uri? {
    return try {
        val cacheFile = File(
            context.externalCacheDir ?: context.cacheDir,
            "gallery_pick_${System.currentTimeMillis()}.jpg"
        )
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
        }
        if (!cacheFile.exists() || cacheFile.length() == 0L) return null
        val authority = "${context.packageName}.fileprovider"
        androidx.core.content.FileProvider.getUriForFile(context, authority, cacheFile)
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}

private fun cropBitmap(
    bitmap: Bitmap,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float
): Bitmap {
    return try {
        val safeInput = convertToSoftwareBitmap(bitmap)
        var source = safeInput
        if (rotation != 0f) {
            try {
                val matrix = android.graphics.Matrix().apply { postRotate(rotation) }
                source = Bitmap.createBitmap(safeInput, 0, 0, safeInput.width, safeInput.height, matrix, true)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        val w = source.width
        val h = source.height
        val minDim = minOf(w, h)

        val sizeTemp = (minDim / zoom).toInt()
        val cropSize = sizeTemp.coerceIn(minOf(50, minDim), minDim)

        val centerX = w / 2
        val centerY = h / 2

        val scaleFactor = minDim.toFloat() / 260f
        val pixelShiftX = (-offsetX * scaleFactor).toInt()
        val pixelShiftY = (-offsetY * scaleFactor).toInt()

        val targetStartX = (centerX - cropSize / 2 + pixelShiftX).coerceIn(0, maxOf(0, w - cropSize))
        val targetStartY = (centerY - cropSize / 2 + pixelShiftY).coerceIn(0, maxOf(0, h - cropSize))

        try {
            Bitmap.createBitmap(source, targetStartX, targetStartY, cropSize, cropSize)
        } catch (t: Throwable) {
            t.printStackTrace()
            val fallbackStartX = (centerX - cropSize / 2).coerceIn(0, maxOf(0, w - cropSize))
            val fallbackStartY = (centerY - cropSize / 2).coerceIn(0, maxOf(0, h - cropSize))
            Bitmap.createBitmap(source, fallbackStartX, fallbackStartY, cropSize, cropSize)
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        bitmap
    }
}

// --------------------------------------------------------------------------------------------
// 3. DOG PROFILE SETUP SCREEN
// --------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DogProfileSetupScreen(viewModel: PawMatchViewModel) {
    val setupName by viewModel.setupName.collectAsStateWithLifecycle()
    val setupBreed by viewModel.setupBreed.collectAsStateWithLifecycle()
    val setupAge by viewModel.setupAge.collectAsStateWithLifecycle()
    val setupGender by viewModel.setupGender.collectAsStateWithLifecycle()
    val setupLocation by viewModel.setupLocation.collectAsStateWithLifecycle()
    val setupBio by viewModel.setupBio.collectAsStateWithLifecycle()
    val setupImageUrl by viewModel.setupImageUrl.collectAsStateWithLifecycle()
    val userDog by viewModel.userDog.collectAsStateWithLifecycle()

    var showPhotoPickerSelectionDialog by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf(false) }
    var cropSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropOutputFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraFilePath by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as? androidx.activity.ComponentActivity

    // --- CROP INTENT RESULT LAUNCHER ---
    // Receives the result from the OS built-in crop activity.
    val cropIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val outputPath = cropOutputFilePath
                val outputFile = outputPath?.let { File(it) }
                if (outputFile != null && outputFile.exists() && outputFile.length() > 0) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(outputFile.absolutePath)
                            if (bitmap != null) {
                                try { saveBitmapToGallery(context, convertToSoftwareBitmap(bitmap)) } catch (t: Throwable) { t.printStackTrace() }
                            }
                            val fileUri = Uri.fromFile(outputFile).toString()
                            withContext(Dispatchers.Main) {
                                viewModel.setupImageUrl.value = fileUri
                                Toast.makeText(context, "Profile photo cropped & saved!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error saving cropped photo.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    val extras = result.data?.extras
                    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        extras?.getParcelable("data", Bitmap::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras?.getParcelable("data") as? Bitmap
                    }
                    if (bitmap != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val safe = convertToSoftwareBitmap(bitmap)
                                val file = File(context.cacheDir, "crop_result_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { safe.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                                try { saveBitmapToGallery(context, safe) } catch (t: Throwable) { t.printStackTrace() }
                                val fileUri = Uri.fromFile(file).toString()
                                withContext(Dispatchers.Main) {
                                    viewModel.setupImageUrl.value = fileUri
                                    Toast.makeText(context, "Profile photo saved!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }
                    }
                }
            }
            // If RESULT_CANCELED (user pressed X in crop), just return to profile screen silently
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    // Launches the OS crop intent. Falls back to our custom dialog if no crop app is available.
    fun launchCropIntent(sourceUri: Uri) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val storageDir = context.externalCacheDir ?: context.cacheDir
            val outputFile = File(storageDir, "crop_output_${System.currentTimeMillis()}.jpg")
            cropOutputFilePath = outputFile.absolutePath
            val outputUri = androidx.core.content.FileProvider.getUriForFile(context, authority, outputFile)

            val cropIntent = android.content.Intent("com.android.camera.action.CROP").apply {
                setDataAndType(sourceUri, "image/*")
                putExtra("crop", "true")
                putExtra("aspectX", 1)
                putExtra("aspectY", 1)
                putExtra("outputX", 800)
                putExtra("outputY", 800)
                putExtra("scale", true)
                putExtra("scaleUpIfNeeded", true)
                putExtra("return-data", false)
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputUri)
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.name)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            // Grant permissions to all apps that can handle the crop intent
            val resInfoList = context.packageManager.queryIntentActivities(
                cropIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
            )

            if (resInfoList.isEmpty()) {
                // No system crop app found - fall back to our built-in crop dialog
                Toast.makeText(context, "Opening crop tool...", Toast.LENGTH_SHORT).show()
                coroutineScope.launch(Dispatchers.IO) {
                    val bitmap = try {
                        context.contentResolver.openInputStream(sourceUri)?.use {
                            android.graphics.BitmapFactory.decodeStream(it)
                        }
                    } catch (t: Throwable) { null }
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            cropSourceBitmap = bitmap
                            showCropDialog = true
                        } else {
                            Toast.makeText(context, "Could not load photo for cropping.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return
            }

            for (resolveInfo in resInfoList) {
                val pkg = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkg, sourceUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                context.grantUriPermission(pkg, outputUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            cropIntentLauncher.launch(cropIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Crop failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- GALLERY PHOTO PICKER ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                Toast.makeText(context, "Opening crop tool...", Toast.LENGTH_SHORT).show()
                // Copy to cache on a background thread so crop app has a FileProvider URI it can read.
                // Android photo picker URIs (content://media/picker_...) are not accessible
                // to third-party apps, so we must copy the bytes into our own cache first.
                coroutineScope.launch(Dispatchers.IO) {
                    val cachedUri = copyUriToCache(context, uri)
                    withContext(Dispatchers.Main) {
                        if (cachedUri != null) {
                            launchCropIntent(cachedUri)
                        } else {
                            Toast.makeText(context, "Could not read selected photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    )

    // --- CAMERA LAUNCHER ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            try {
                val path = tempCameraFilePath
                val file = path?.let { File(it) }
                if (success && file != null && file.exists()) {
                    val authority = "${context.packageName}.fileprovider"
                    val contentUri = try {
                        androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                    } catch (e: Exception) { null }
                    if (contentUri != null) {
                        // Save original (uncropped) camera photo to gallery immediately
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                                if (bitmap != null) {
                                    saveBitmapToGallery(context, convertToSoftwareBitmap(bitmap))
                                }
                            } catch (t: Throwable) { t.printStackTrace() }
                        }
                        Toast.makeText(context, "Opening crop tool...", Toast.LENGTH_SHORT).show()
                        launchCropIntent(contentUri)
                    } else {
                        Toast.makeText(context, "Failed to process captured photo.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User pressed X / cancelled camera - return to profile screen silently
                    tempCameraFilePath = null
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    )

    fun launchCamera() {
        try {
            val storageDir = context.externalCacheDir ?: context.cacheDir
            val file = File(storageDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            tempCameraFilePath = file.absolutePath
            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            // Grant write permission to all camera apps
            try {
                val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val resInfoList = context.packageManager.queryIntentActivities(cameraIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    context.grantUriPermission(resolveInfo.activityInfo.packageName, uri,
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (ex: Exception) { ex.printStackTrace() }
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to launch Camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(context, "Camera permission is required to click a photo.", Toast.LENGTH_LONG).show()
            }
        }
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    if (showPhotoPickerSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerSelectionDialog = false },
            title = {
                Text(
                    text = "Select Profile Photo",
                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Customize your dog's profile picture using device photography or photo gallery.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
                    )
                    
                    // Option 1: Click a photo
                    Button(
                        onClick = {
                            showPhotoPickerSelectionDialog = false
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Soapstone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = WarmBrown)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Click a photo", color = WarmBrown, fontWeight = FontWeight.SemiBold)
                    }

                    // Option 2: Upload a photo
                    Button(
                        onClick = {
                            showPhotoPickerSelectionDialog = false
                            photoPickerLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Soapstone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = WarmBrown)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Upload a photo", color = WarmBrown, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoPickerSelectionDialog = false }) {
                    Text("Cancel", color = Terracotta)
                }
            },
            containerColor = WarmCream,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showCropDialog && cropSourceBitmap != null) {
        val bitmapToCrop = cropSourceBitmap!!
        var zoom by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        var rotation by remember { mutableStateOf(0f) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCropDialog = false },
            title = {
                Text(
                    text = "Crop Profile Photo",
                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Drag the photo to pan, and use the zoom slider to scale perfectly into the frame.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown),
                        textAlign = TextAlign.Center
                    )

                    // Image Canvas Container
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Soapstone)
                            .border(1.dp, WarmCream, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clipToBounds()
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmapToCrop.asImageBitmap(),
                                contentDescription = "Original photo to crop",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = zoom
                                        scaleY = zoom
                                        translationX = offsetX
                                        translationY = offsetY
                                        rotationZ = rotation
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            offsetX += dragAmount.x
                                            offsetY += dragAmount.y
                                        }
                                    },
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Stencil Overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasSize = size
                            val circleRadius = 110.dp.toPx()
                            val centerOffset = Offset(canvasSize.width / 2, canvasSize.height / 2)

                            // Robust gray mask cutout using EvenOdd fill type
                            val maskPath = Path().apply {
                                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasSize.width, canvasSize.height))
                                addOval(androidx.compose.ui.geometry.Rect(centerOffset, circleRadius))
                            }
                            drawPath(maskPath, color = Color.Black.copy(alpha = 0.5f))

                            // Draw high contrast circle border
                            drawCircle(
                                color = Terracotta,
                                radius = circleRadius,
                                center = centerOffset,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }
                    }

                    // Zoom control Slider with percentage metrics
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZOOM: ${(zoom * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SoftBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            // Rotation button
                            IconButton(
                                onClick = { rotation = (rotation + 90f) % 360f },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate 90 degrees",
                                    tint = Terracotta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Slider(
                            value = zoom,
                            onValueChange = { zoom = it },
                            valueRange = 1f..3f,
                            colors = SliderDefaults.colors(
                                thumbColor = Terracotta,
                                activeTrackColor = Terracotta,
                                inactiveTrackColor = Soapstone
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Helper to reset positioning
                    TextButton(
                        onClick = {
                            zoom = 1f
                            offsetX = 0f
                            offsetY = 0f
                            rotation = 0f
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SoftBrown),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Position", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSaving,
                    onClick = {
                        isSaving = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // 1. Convert source bitmap to software securely
                                val safeBitmapToCrop = convertToSoftwareBitmap(bitmapToCrop)
                                
                                // 2. Perform crop
                                val rawCropped = cropBitmap(safeBitmapToCrop, zoom, offsetX, offsetY, rotation)
                                val cropped = convertToSoftwareBitmap(rawCropped)
                                
                                // 3. Save cropped result to device gallery with robust fallback
                                val savedUri = try {
                                    saveBitmapToGallery(context, cropped)
                                } catch (t: Throwable) {
                                    t.printStackTrace()
                                    null
                                }
                                
                                // 4. Save to app cache
                                val file = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
                                val out = FileOutputStream(file)
                                cropped.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                out.flush()
                                out.close()
                                
                                // 5. Update view model setup image URL and handle UI changes on main thread
                                val fileUriStr = Uri.fromFile(file).toString()
                                withContext(Dispatchers.Main) {
                                    viewModel.setupImageUrl.value = fileUriStr
                                    if (savedUri != null) {
                                        Toast.makeText(context, "Cropped profile photo saved to gallery successfully!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Profile photo updated & cached locally!", Toast.LENGTH_SHORT).show()
                                    }
                                    showCropDialog = false
                                    isSaving = false
                                }
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error saving cropped photo: ${t.message}", Toast.LENGTH_SHORT).show()
                                    showCropDialog = false
                                    isSaving = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(99.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSaving) "SAVING..." else "SAVE PHOTO", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCropDialog = false }
                ) {
                    Text("Cancel", color = SoftBrown)
                }
            },
            containerColor = WarmCream,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillModifierWithNavigationBars()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { viewModel.saveProfileAndComplete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, CircleShape, ambientColor = SlateShadow, spotColor = SlateShadow)
                ) {
                    Text(
                        text = "SAVE PROFILE & DISCOVER",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (userDog != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(PawMatchScreen.MainHub) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close setup screen link",
                            tint = WarmBrown
                        )
                    }
                }
            }

            Text(
                text = "Tell Us About Your Pup",
                style = MaterialTheme.typography.headlineLarge.copy(color = WarmBrown),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "This sets up your premium listing context so nearby matched dogs can connect cleanly.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Substantive modern upload dog photo mock visual
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(128.dp)
                ) {
                    // Profile Photo Circle
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(Soapstone)
                            .border(2.dp, GoldAccent, CircleShape)
                            .clickable { showPhotoPickerSelectionDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(setupImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Standard dog avatar preset",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Elegant small edit camera badge on the bottom right of the circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Terracotta)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { showPhotoPickerSelectionDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Upload Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input fields
            PawMatchInput(
                label = "DOG NAME",
                value = setupName,
                onValueChange = { viewModel.setupName.value = it },
                placeholder = "e.g. Bruno"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PawMatchInput(
                label = "BREED",
                value = setupBreed,
                onValueChange = { viewModel.setupBreed.value = it },
                placeholder = "e.g. Golden Retriever"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    PawMatchInput(
                        label = "AGE (YEARS)",
                        value = setupAge,
                        onValueChange = { viewModel.setupAge.value = it },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Number
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    // Standard gender selector buttons
                    Column {
                        Text(
                            text = "GENDER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SoftBrown,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Soapstone)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isMale = setupGender.equals("Male", ignoreCase = true)
                            Button(
                                onClick = { viewModel.setupGender.value = "Male" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMale) Terracotta else Color.Transparent,
                                    contentColor = if (isMale) Color.White else SoftBrown
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Male", style = MaterialTheme.typography.labelLarge)
                            }
                            Button(
                                onClick = { viewModel.setupGender.value = "Female" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isMale) Terracotta else Color.Transparent,
                                    contentColor = if (!isMale) Color.White else SoftBrown
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Female", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PawMatchInput(
                label = "LOCATION (NEIGHBORHOOD)",
                value = setupLocation,
                onValueChange = { viewModel.setupLocation.value = it },
                placeholder = "e.g. Juhu, Mumbai"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PawMatchInput(
                label = "BIO / DESCRIPTION",
                value = setupBio,
                onValueChange = { viewModel.setupBio.value = it },
                placeholder = "Introduce your pup...",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PawMatchInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = SoftBrown,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown.copy(alpha = 0.5f))) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WarmBrown,
                unfocusedTextColor = WarmBrown,
                focusedContainerColor = Soapstone,
                unfocusedContainerColor = Soapstone,
                focusedBorderColor = Terracotta,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            )
        )
    }
}

// --------------------------------------------------------------------------------------------
// 4. MAIN HUB COMPONENT LAYOUT
// --------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHubScreen(viewModel: PawMatchViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDogDetail by viewModel.selectedDogForDetail.collectAsStateWithLifecycle()
    val selectedDogChat by viewModel.selectedDogForChat.collectAsStateWithLifecycle()
    val selectedDogMeetupSetup by viewModel.selectedDogForMeetupRequest.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            // Elegant premium bar with matching active indicators
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillModifierWithNavigationBars()
                    .border(1.dp, Soapstone, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Tab: Discover (Swipe Feed)
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Discover,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Discover) },
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Discover Feed Icon") },
                    label = { Text("Discover") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Terracotta,
                        selectedTextColor = Terracotta,
                        unselectedIconColor = SoftBrown,
                        unselectedTextColor = SoftBrown,
                        indicatorColor = Terracotta.copy(alpha = 0.1f)
                    )
                )

                // Tab: Map
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.MapNearby,
                    onClick = { viewModel.navigateToTab(PawMatchTab.MapNearby) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map Tab Icon") },
                    label = { Text("Nearby") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Terracotta,
                        selectedTextColor = Terracotta,
                        unselectedIconColor = SoftBrown,
                        unselectedTextColor = SoftBrown,
                        indicatorColor = Terracotta.copy(alpha = 0.1f)
                    )
                )

                // Tab: Matches
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Matches,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Matches) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Matches List Icon") },
                    label = { Text("Matches") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Terracotta,
                        selectedTextColor = Terracotta,
                        unselectedIconColor = SoftBrown,
                        unselectedTextColor = SoftBrown,
                        indicatorColor = Terracotta.copy(alpha = 0.1f)
                    )
                )

                // Tab: Meetups / Schedule
                NavigationBarItem(
                    selected = currentTab == PawMatchTab.Meetups,
                    onClick = { viewModel.navigateToTab(PawMatchTab.Meetups) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Scheduler Tab Icon") },
                    label = { Text("Meetups") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Terracotta,
                        selectedTextColor = Terracotta,
                        unselectedIconColor = SoftBrown,
                        unselectedTextColor = SoftBrown,
                        indicatorColor = Terracotta.copy(alpha = 0.1f)
                    )
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
            // Screen switching mechanism based on current active bottom tab selection
            when (currentTab) {
                PawMatchTab.Discover -> DiscoverFeedTab(viewModel)
                PawMatchTab.Matches -> MatchesTab(viewModel)
                PawMatchTab.MapNearby -> MapNearbyTab(viewModel)
                PawMatchTab.Meetups -> MeetupsTab(viewModel)
            }

            // Overlay Details Screen layer over the tab for frictionless transitions
            selectedDogDetail?.let { dog ->
                DogDetailOverlayScreen(
                    dog = dog,
                    onClose = { viewModel.closeDogDetail() },
                    onSendRequest = {
                        viewModel.closeDogDetail()
                        viewModel.openMeetupSetup(dog)
                    }
                )
            }

            // Overlay In-App Chat screen layer
            selectedDogChat?.let { dog ->
                InAppChatOverlayScreen(
                    dog = dog,
                    viewModel = viewModel,
                    onClose = { viewModel.closeChat() }
                )
            }

            // Overlay Scheduling form
            selectedDogMeetupSetup?.let { dog ->
                MeetupSetupOverlayScreen(
                    dog = dog,
                    onClose = { viewModel.closeMeetupSetup() },
                    onSchedule = { date, time, location, note ->
                        viewModel.scheduleMeetup(date, time, location, note)
                    }
                )
            }
        }
    }
}

// Helper spacer wrapper
fun Modifier.fillModifierWithNavigationBars(): Modifier = this.navigationBarsPadding()

// --------------------------------------------------------------------------------------------
// 4A. TAB: DISCOVER SWIPE FEED
// --------------------------------------------------------------------------------------------
@Composable
fun DiscoverFeedTab(viewModel: PawMatchViewModel) {
    val otherDogs by viewModel.filteredOtherDogs.collectAsStateWithLifecycle()
    val allOtherDogs by viewModel.allOtherDogsListCombined.collectAsStateWithLifecycle()
    val userDog by viewModel.userDog.collectAsStateWithLifecycle()
    val dailyPupFact by viewModel.dailyPupFact.collectAsStateWithLifecycle()
    val isRefreshingStack by viewModel.isRefreshingStack.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // App header bar branding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PawMatch Elite",
                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Pedigree compatible matches",
                    style = MaterialTheme.typography.labelSmall.copy(color = SoftBrown)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // User dog avatar thumbnail
            userDog?.let {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Soapstone)
                        .border(1.5.dp, GoldAccent, CircleShape)
                        .clickable { viewModel.navigateTo(PawMatchScreen.ProfileSetup) }
                ) {
                    AsyncImage(
                        model = it.imageUrl,
                        contentDescription = "User dog profile thumb",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }


        if (isRefreshingStack) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Terracotta,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Finding More Pups...",
                        style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Refreshing the stack to load newly available pedigree matches nearby.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else if (otherDogs.isEmpty()) {
            LaunchedEffect(Unit) {
                viewModel.triggerAutoReload()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "No more dogs icon",
                        tint = GoldAccent,
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You've Swiped All Local Dogs!",
                        style = MaterialTheme.typography.headlineMedium.copy(color = WarmBrown),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reloading next match profiles automatically...",
                        style = MaterialTheme.typography.bodyLarge.copy(color = SoftBrown),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Renders card stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Swipeable Card implementation
                val topDog = otherDogs.firstOrNull()

                // States to handle high-performance dragging without excessive frame allocation or thread blocking
                var dragX by remember(topDog?.id) { mutableStateOf(0f) }
                var dragY by remember(topDog?.id) { mutableStateOf(0f) }
                var isDragging by remember(topDog?.id) { mutableStateOf(false) }

                val animOffsetX = remember(topDog?.id) { Animatable(0f) }
                val animOffsetY = remember(topDog?.id) { Animatable(0f) }

                val offsetX = if (isDragging) dragX else animOffsetX.value
                val offsetY = if (isDragging) dragY else animOffsetY.value

                val coroutineScope = rememberCoroutineScope()

                if (topDog != null) {
                    // Bottom card hint
                    if (otherDogs.size > 1) {
                    val nextDog = otherDogs[1]
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, bottom = 4.dp)
                            .padding(horizontal = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = nextDog.imageUrl,
                                contentDescription = nextDog.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Top active swipe card
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .padding(bottom = 16.dp)
                        .pointerInput(topDog) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    coroutineScope.launch {
                                        animOffsetX.snapTo(dragX)
                                        animOffsetY.snapTo(dragY)
                                        dragX = 0f
                                        dragY = 0f
                                        launch { animOffsetX.animateTo(0f) }
                                        launch { animOffsetY.animateTo(0f) }
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val currentDragX = dragX
                                    val currentDragY = dragY
                                    if (currentDragX > 180f) {
                                        coroutineScope.launch {
                                            animOffsetX.snapTo(currentDragX)
                                            animOffsetY.snapTo(currentDragY)
                                            dragX = 0f
                                            dragY = 0f
                                            animOffsetX.animateTo(600f, animationSpec = tween(200))
                                            viewModel.swipeRight(topDog)
                                        }
                                    } else if (currentDragX < -180f) {
                                        coroutineScope.launch {
                                            animOffsetX.snapTo(currentDragX)
                                            animOffsetY.snapTo(currentDragY)
                                            dragX = 0f
                                            dragY = 0f
                                            animOffsetX.animateTo(-600f, animationSpec = tween(200))
                                            viewModel.swipeLeft(topDog)
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            animOffsetX.snapTo(currentDragX)
                                            animOffsetY.snapTo(currentDragY)
                                            dragX = 0f
                                            dragY = 0f
                                            launch {
                                                animOffsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                            launch {
                                                animOffsetY.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragX += dragAmount.x
                                    dragY += dragAmount.y
                                }
                            )
                        },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = topDog.imageUrl,
                            contentDescription = topDog.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Elegant Glassmorphic Gradient Bottom overlay for profile elements
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Dog Name & Breed badges row with constraints and Info button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${topDog.name}, ${topDog.age} yrs",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            lineHeight = 30.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (topDog.gender == "Male") {
                                        Icon(
                                            Icons.Default.Male,
                                            contentDescription = "Male gender icon",
                                            tint = Color(0xFF63B3ED),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Female,
                                            contentDescription = "Female gender icon",
                                            tint = Color(0xFFF687B3),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Dedicated Visual Details Info Button to resolve clickable gesture conflicts
                                    IconButton(
                                        onClick = { viewModel.openDogDetail(topDog) },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "View Details",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Breed Tag Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Terracotta.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = topDog.breed,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    // Nearby Distance indicator
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pin Icon",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${topDog.location} (${topDog.distance} km away)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Simple premium preview bio text
                                Text(
                                    text = topDog.bio,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.85f)),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                }
            }

            // Standard elegant Swipe CTA actions dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Swipe Left button
                IconButton(
                    onClick = {
                        val firstDog = otherDogs.firstOrNull()
                        if (firstDog != null) {
                            viewModel.swipeLeft(firstDog)
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Soapstone, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Swipe Left Button",
                        tint = Terracotta,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Swiper Star (Supermatch detailing view trigger first)
                IconButton(
                    onClick = {
                        val firstDog = otherDogs.firstOrNull()
                        if (firstDog != null) {
                            viewModel.openDogDetail(firstDog)
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Soapstone, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Review Detail Info Button",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Swipe Right button
                IconButton(
                    onClick = {
                        val firstDog = otherDogs.firstOrNull()
                        if (firstDog != null) {
                            viewModel.swipeRight(firstDog)
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Soapstone, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Swipe Right Match Button",
                        tint = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 4B. TAB: MAP NEARBY VIEW
// --------------------------------------------------------------------------------------------
@Composable
fun MapNearbyTab(viewModel: PawMatchViewModel) {
    val otherDogs by viewModel.allOtherDogsListCombined.collectAsStateWithLifecycle()
    val filterRadius by viewModel.filterRadius.collectAsStateWithLifecycle()
    val feedQuery by viewModel.filterBreedQuery.collectAsStateWithLifecycle()
    val isMapMode by viewModel.isMapViewLayout.collectAsStateWithLifecycle()
    val selectedPinId by viewModel.selectedMapDogPinId.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // High fidelity Map Action filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = feedQuery,
                onValueChange = { viewModel.filterBreedQuery.value = it },
                placeholder = { Text("Filter Breed (e.g. Retriever, Corgi)", style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown.copy(alpha = 0.5f))) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Filter icon", tint = SoftBrown) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WarmBrown,
                    unfocusedTextColor = WarmBrown,
                    focusedContainerColor = Soapstone,
                    unfocusedContainerColor = Soapstone,
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Dynamic Distance radial Filter slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MAX DISTANCE: ${filterRadius.roundToInt()} km",
                        style = MaterialTheme.typography.labelSmall.copy(color = SoftBrown, fontWeight = FontWeight.Bold)
                    )
                    Slider(
                        value = filterRadius,
                        onValueChange = { viewModel.filterRadius.value = it },
                        valueRange = 2f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = Terracotta,
                            activeTrackColor = Terracotta,
                            inactiveTrackColor = Soapstone
                        )
                    )
                }

                // Premium visual toggle slider button
                Button(
                    onClick = { viewModel.toggleLayoutView() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmBrown),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isMapMode) Icons.Default.List else Icons.Default.Map,
                        contentDescription = "Map and List Layout toggle switcher",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isMapMode) "List" else "Map", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                }
            }
        }

        Divider(color = Soapstone, thickness = 1.dp)

        // Switch panel layout rendering Map vs List
        if (isMapMode) {
            RealNearbyMap(modifier = Modifier.fillMaxWidth().weight(1f))
        
        } else {
            // LazyColumn layout mode listing dogs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(otherDogs) { dog ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openDogDetail(dog) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = dog.imageUrl,
                                    contentDescription = dog.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${dog.name}, ${dog.age} yrs",
                                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                                )
                                Text(
                                    text = dog.breed,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Terracotta)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.openMeetupSetup(dog) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Soapstone)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Schedule meetup direct button",
                                    tint = Terracotta
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 4B-MAP. REAL OSMDROID NATIVE MAP COMPOSABLE
// --------------------------------------------------------------------------------------------
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RealNearbyMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val hasLocation = locationPermissions.permissions.any { it.status.isGranted }

    Box(modifier = modifier) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                // Configure OSMDroid with a valid user-agent
                Configuration.getInstance().apply {
                    userAgentValue = ctx.packageName
                    osmdroidBasePath = ctx.cacheDir
                    osmdroidTileCache = java.io.File(ctx.cacheDir, "osmdroid-tiles")
                }

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    isTilesScaledToDpi = true

                    // Default center: Mumbai, India
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(19.0400, 72.8350))

                    // Compass overlay
                    val compass = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                    compass.enableCompass()
                    overlays.add(compass)

                    // My-location dot + arrow overlay (shows live GPS position)
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    locationOverlay.enableMyLocation()
                    if (hasLocation) {
                        locationOverlay.enableFollowLocation()
                    }
                    overlays.add(locationOverlay)
                }
            },
            update = { mapView ->
                // Resume/pause handled by lifecycle
            },
            modifier = Modifier.fillMaxSize()
        )

        // Permission request prompt overlay shown when location is not granted
        if (!hasLocation) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .background(
                        color = Color(0xFF24150E).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📍 Enable Location",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Allow location access to show your position on the map.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { locationPermissions.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE05A36)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Location Permission", color = Color.White)
                }
            }
        }

        // "My Location" FAB button
        FloatingActionButton(
            onClick = { locationPermissions.launchMultiplePermissionRequest() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF24150E),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location"
            )
        }
    }
}

// --------------------------------------------------------------------------------------------
// 4C. TAB: MATCHES

// --------------------------------------------------------------------------------------------
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MatchesTab(viewModel: PawMatchViewModel) {
    val matches by viewModel.matchedDogs.collectAsStateWithLifecycle()
    var dogToDelete by remember { mutableStateOf<Dog?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Elite Matches",
                style = MaterialTheme.typography.headlineLarge.copy(color = WarmBrown),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Start high-end conversations with neighbor dog owners who matched with your pup.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
            )
        }

        Divider(color = Soapstone, thickness = 1.dp)

        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "No matches heart placeholder",
                        tint = SoftBrown.copy(alpha = 0.4f),
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Match History Is Quiet",
                        style = MaterialTheme.typography.headlineMedium.copy(color = WarmBrown),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Swipe right on dogs who share similar traits to begin playdate schedules.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = SoftBrown),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(matches) { dog ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { viewModel.openChat(dog) },
                                onLongClick = { dogToDelete = dog }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = dog.imageUrl,
                                    contentDescription = dog.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${dog.name} & ${dog.ownerName}",
                                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                                )
                                Text(
                                    text = "Ready to plan meetups!",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Terracotta, fontWeight = FontWeight.Bold)
                                )
                            }

                            // Interactive chat button
                            IconButton(
                                onClick = { viewModel.openChat(dog) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = GoldAccent.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Open Chat Room Button",
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dogToDelete != null) {
        val targetDog = dogToDelete!!
        AlertDialog(
            onDismissRequest = { dogToDelete = null },
            title = {
                Text(
                    text = "Delete Conversation?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = WarmBrown)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end your match and permanently delete all conversation history with ${targetDog.name}?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversation(targetDog)
                        dogToDelete = null
                    }
                ) {
                    Text("Delete", color = Terracotta, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dogToDelete = null }) {
                    Text("Cancel", color = SoftBrown)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// --------------------------------------------------------------------------------------------
// 4D. TAB: MEETUPS PORTAL
// --------------------------------------------------------------------------------------------
@Composable
fun MeetupsTab(viewModel: PawMatchViewModel) {
    val meetups by viewModel.meetups.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Elite Playdates",
                style = MaterialTheme.typography.headlineLarge.copy(color = WarmBrown),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Monitor confirmed, pending, and complete schedules to verify dates.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
            )
        }

        Divider(color = Soapstone, thickness = 1.dp)

        if (meetups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar placeholder",
                        tint = SoftBrown.copy(alpha = 0.4f),
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Scheduled Meetups",
                        style = MaterialTheme.typography.headlineMedium.copy(color = WarmBrown),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start chat timelines or review details on any matched dogs to schedule confirmed events.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = SoftBrown),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(meetups) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = item.dogImageUrl,
                                        contentDescription = item.dogName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Playdate with ${item.dogName}",
                                        style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                                    )
                                    Text(
                                        text = "${item.date} @ ${item.time}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
                                    )
                                }

                                // Elegant Status Badge
                                val color = when (item.status) {
                                    "Confirmed" -> Color(0xFF48BB78)
                                    "Declined" -> Terracotta
                                    else -> GoldAccent
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Soapstone, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location Pin icon",
                                    tint = Terracotta,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.location,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = WarmBrown)
                                )
                            }

                            if (item.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "\"${item.note}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SoftBrown,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }

                            // Dynamic simulation accept/decline tools for pending requests!
                            if (item.status == "Pending") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.respondToMeetup(item.id, accept = false) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Decline")
                                    }
                                    Button(
                                        onClick = { viewModel.respondToMeetup(item.id, accept = true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarmBrown),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirm")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 5. DETAIL SCREEN MODAL OVERLAY
// --------------------------------------------------------------------------------------------
@Composable
fun DogDetailOverlayScreen(
    dog: Dog,
    onClose: () -> Unit,
    onSendRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(WarmCream)
                .clickable(enabled = false, onClick = {}) // Block tap propagates
        ) {
            // Big layout card with profile details
            Box(
                modifier = Modifier
                    .fillModifierWithNavigationBars()
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    // Profile Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        AsyncImage(
                            model = dog.imageUrl,
                            contentDescription = dog.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Close Float Button
                        IconButton(
                            onClick = onClose,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close detailed overlay window")
                        }

                        // Gradient fade-bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, WarmCream)
                                    )
                                )
                        )
                    }

                    // Content details
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = dog.name,
                                style = MaterialTheme.typography.displayLarge.copy(color = WarmBrown)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dog.breed,
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Text(
                            text = "${dog.gender} Â· ${dog.age} yrs old Â· ${dog.location}",
                            style = MaterialTheme.typography.bodyLarge.copy(color = SoftBrown, fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Dog Bio
                        Text(
                            text = "Abilities & Quirks",
                            style = MaterialTheme.typography.headlineMedium.copy(color = WarmBrown, fontSize = 20.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dog.bio,
                            style = MaterialTheme.typography.bodyLarge.copy(color = WarmBrown.copy(alpha = 0.85f), lineHeight = 24.sp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Owner Card detail
                        Text(
                            text = "Matched Parent",
                            style = MaterialTheme.typography.headlineMedium.copy(color = WarmBrown, fontSize = 20.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.dp, Soapstone, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Soapstone)
                            ) {
                                AsyncImage(
                                    model = dog.ownerAvatarUrl,
                                    contentDescription = dog.ownerName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = dog.ownerName,
                                    style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                                )
                                Text(
                                    text = "Premium Member",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Setup Schedule Action Button Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onSendRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Meetup Request Icon Setup Calendar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCHEDULE PLAYDATE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 6. DETAILED REAL-TIME IN-APP CHAT TIMELINE WINDOW OVERLAY
// --------------------------------------------------------------------------------------------
@Composable
fun InAppChatOverlayScreen(
    dog: Dog,
    viewModel: PawMatchViewModel,
    onClose: () -> Unit
) {
    val messages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    var rawTextValue by remember { mutableStateOf("") }

    val userDog by viewModel.userDog.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header bar detailing matching canine profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back screen button icon chevron", tint = WarmBrown)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Soapstone)
                ) {
                    AsyncImage(
                        model = dog.imageUrl,
                        contentDescription = dog.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = dog.name,
                        style = MaterialTheme.typography.titleLarge.copy(color = WarmBrown)
                    )
                    Text(
                        text = "Active Conversation",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Schedule Meetup Shortcut right from inside chat
                IconButton(
                    onClick = {
                        viewModel.closeChat()
                        viewModel.openMeetupSetup(dog)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Terracotta.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Add meetup shortcut", tint = Terracotta)
                }
            }

            Divider(color = Soapstone, thickness = 1.dp)

            // Dynamic Scrollable Chat Bubble timeline list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.isFromUser
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .background(if (isMe) Terracotta else Color.White)
                                .border(1.dp, Soapstone, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (isMe) Color.White else WarmBrown,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }

            // Standard bottom message typing dock controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rawTextValue,
                    onValueChange = { rawTextValue = it },
                    placeholder = { Text("Write high-end note...", style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown.copy(alpha = 0.5f))) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WarmBrown,
                        unfocusedTextColor = WarmBrown,
                        focusedContainerColor = Soapstone,
                        unfocusedContainerColor = Soapstone,
                        focusedBorderColor = Terracotta,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (rawTextValue.isNotBlank()) {
                            viewModel.sendChatMessage(rawTextValue)
                            rawTextValue = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Terracotta),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message bubble timeline controller button button button",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 7. MEETUP REQUEST SCHEDULING BUILD OVERLAY SCREEN
// --------------------------------------------------------------------------------------------
@Composable
fun MeetupSetupOverlayScreen(
    dog: Dog,
    onClose: () -> Unit,
    onSchedule: (date: String, time: String, location: String, note: String) -> Unit
) {
    var dateVal by remember { mutableStateOf("July 12, 2026") }
    var timeVal by remember { mutableStateOf("4:30 PM") }
    var locationVal by remember { mutableStateOf("Carter Road Promenade, Bandra") }
    var noteVal by remember { mutableStateOf("Let's do a quick meet and greet by the sea-face so our dogs can socialize!") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(WarmCream)
                .clickable(enabled = false, onClick = {})
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Plan Playdate",
                    style = MaterialTheme.typography.headlineLarge.copy(color = WarmBrown)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss Scheduling window")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Propose a luxury neighborhood play session with ${dog.name} & ${dog.ownerName}",
                style = MaterialTheme.typography.bodyMedium.copy(color = SoftBrown)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Propose setup form
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    PawMatchInput(
                        label = "DATE",
                        value = dateVal,
                        onValueChange = { dateVal = it },
                        placeholder = "July 12, 2026"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    PawMatchInput(
                        label = "TIME",
                        value = timeVal,
                        onValueChange = { timeVal = it },
                        placeholder = "4:30 PM"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PawMatchInput(
                label = "LOCATION / PRK / MEADOW",
                value = locationVal,
                onValueChange = { locationVal = it },
                placeholder = "Carter Road Promenade"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PawMatchInput(
                label = "NOTE TO OWNER",
                value = noteVal,
                onValueChange = { noteVal = it },
                placeholder = "Say something sweet...",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSchedule(dateVal, timeVal, locationVal, noteVal) },
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, CircleShape, ambientColor = SlateShadow, spotColor = SlateShadow)
            ) {
                Text(
                    text = "SEND PLAYDATE PROPOSAL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------------------
// 8. LUXURIOUS "IT'S A MATCH!" CELEBRATION FULL SCREEN OVERLAY POP
// --------------------------------------------------------------------------------------------
@Composable
fun MatchCelebrationOverlay(
    dog: Dog,
    onDismiss: () -> Unit
) {
    // Beautiful full screen layout celebration with floating particles and animated circles
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Soapstone
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating ambient love circles behind photos
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Terracotta.copy(alpha = 0.08f),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.42f)
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.04f),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.42f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Match heart symbol icon",
                tint = Terracotta,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "It's a Match!",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = WarmBrown,
                    fontSize = 44.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Both your dog and ${dog.name} think they would make a compatible pair nearby!",
                style = MaterialTheme.typography.bodyLarge.copy(color = SoftBrown, lineHeight = 24.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Overlapping matching visual portraits
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // User's Golden Retriever Match portrait
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .shadow(8.dp, CircleShape)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=200",
                        contentDescription = "User default pup profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width((-24).dp)) // Overlap offset value

                // Matched dog's portrait
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .shadow(8.dp, CircleShape)
                ) {
                    AsyncImage(
                        model = dog.imageUrl,
                        contentDescription = dog.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Actions Docks
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(9999.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, CircleShape, ambientColor = SlateShadow, spotColor = SlateShadow)
            ) {
                Text(
                    text = "START CHATTING NOW",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDismiss) {
                Text(
                    text = "KEEP DISCOVERING",
                    style = MaterialTheme.typography.labelLarge.copy(color = SoftBrown, letterSpacing = 2.sp)
                )
            }
        }
    }
}
