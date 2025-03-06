package com.example.guesthousemain.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.guesthousemain.R
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.delay

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            // Subtle gradient from surface to background color
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Banner slider with multiple images
        BannerSlider()

        // Headline text
        Text(
            text = "Welcome to IT Ropar Guest House",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        // Description / body text
        Text(
            text = "Enjoy a comfortable stay with modern amenities, great dining, " +
                    "and friendly staff. Whether you're here for a conference, " +
                    "family vacation, or just passing through, we strive to make " +
                    "your experience memorable and relaxing.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Row of feature cards (e.g., Rooms, Events, About)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Room,
                title = "Rooms",
                description = "Comfortable rooms\nfor every budget."
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Event,
                title = "Events",
                description = "Conference halls\n& event spaces."
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Info,
                title = "About",
                description = "Learn more\nabout our story."
            )
        }

        // "Book Now" button at the bottom
        Button(
            onClick = { /* Navigate to booking or reservation screen */ },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text("Book Now")
        }
    }
}

/**
 * A sliding banner using Accompanist's HorizontalPager.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun BannerSlider() {
    // List of banner image resource IDs (replace with your actual images)
    val images = listOf(
        R.drawable.guest,
        R.drawable.lhc,
        R.drawable.guesthouse_background,
        R.drawable.ele,
        R.drawable.lib,
        R.drawable.dining_hall,
        R.drawable.ad_v
    )
    val pagerState = rememberPagerState(initialPage = 0)

    // Automatic sliding effect.
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000) // Wait for 3 seconds.
            val nextPage = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Banner Image ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }
}
/**
 * A small feature card with optional micro-animation when tapped.
 */
@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    // Animate scale on click for a micro-interaction effect.
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .scale(scale)
            .clickable {
                pressed = true
                // Perform action, e.g. navigate or show a dialog
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Icon at the top
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
