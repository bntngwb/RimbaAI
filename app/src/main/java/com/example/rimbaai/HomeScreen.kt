package com.example.rimbaai

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rimbaai.ui.theme.RimbaAITheme

// Data class untuk item ensiklopedia
data class WildlifeEncyclopediaItem(
    val id: String,
    val name: String,
    val species: String,
    val imageUrl: String,
    var isFavorite: Boolean = false,
    val region: String,
    val conservationStatus: String
)

// Data class untuk Tombol Fitur
data class FeatureButtonInfo(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToIdentification: () -> Unit,
    onNavigateToEncyclopedia: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val accentColor = Color(0xFFFF9800)
    val textPrimaryColor = Color(0xFF273240)
    val screenHorizontalPadding = 20.dp

    val encyclopediaItems = remember {
        listOf<WildlifeEncyclopediaItem>(
            WildlifeEncyclopediaItem("1", "Harimau Sumatera", "Panthera tigris sumatrae", "rimba_placeholder_tiger", true, "Sumatera, Indonesia", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("2", "Orangutan Kalimantan", "Pongo pygmaeus", "rimba_placeholder_orangutan", false, "Kalimantan, Indonesia", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("3", "Badak Jawa", "Rhinoceros sondaicus", "rimba_placeholder_rhino", false, "Taman Nasional Ujung Kulon, Indonesia", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("4", "Komodo", "Varanus komodoensis", "rimba_placeholder_komodo", true, "Pulau Komodo Flores, Indonesia", "Terancam (Endangered)"),
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Rimba AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = textPrimaryColor
                    )
                },
                actions = {
                    IconButton(onClick = { /*TODO: Aksi Search di HomeScreen*/ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = textPrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = lightBackgroundColor
                )
            )
        },
        containerColor = lightBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = screenHorizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            MainHeroCard(
                title = "Temukan Dunia Satwa\ndi Sekitarmu!",
                buttonText = "Mulai Identifikasi",
                accentColor = accentColor,
                onClick = onNavigateToIdentification,
                backgroundImageResId = null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                title = "Fitur Utama",
                modifier = Modifier
                    .padding(bottom = 12.dp)
            )

            val featureButtons = listOf(
                FeatureButtonInfo("Jurnal Saya", Icons.Filled.Pets, Color(0xFFE0F7FA), Color(0xFF00796B)) { /* TODO: Aksi Jurnal & Navigasi */ },
                FeatureButtonInfo("Jurnal Satwa", Icons.AutoMirrored.Filled.MenuBook, Color(0xFFFFF3E0), Color(0xFFF57C00), onClick = onNavigateToEncyclopedia),
                FeatureButtonInfo("Kuis Edukatif", Icons.Filled.HelpOutline, Color(0xFFE8F5E9), Color(0xFF388E3C), onClick = onNavigateToQuiz),
                FeatureButtonInfo("Tanya Rimba AI", Icons.Filled.QuestionAnswer, Color(0xFFF3E5F5), Color(0xFF7B1FA2), onClick = onNavigateToChatbot)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                featureButtons.forEach { feature ->
                    StyledFeatureButton(
                        info = feature,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                title = "Jelajahi Satwa",
                showViewAll = true,
                onViewAllClick = onNavigateToEncyclopedia
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(encyclopediaItems) { item ->
                    WildlifeItemCard( // Pemanggilan WildlifeItemCard
                        item = item,
                        accentColor = accentColor,
                        onToggleFavorite = {
                            println("Favorite toggled for ${item.name} in HomeScreen (placeholder)")
                        },
                        onCardClick = {
                            println("Card ${item.name} clicked from HomeScreen")
                        }
                        // Parameter isSimpleView tidak lagi diperlukan
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, showViewAll: Boolean = false, onViewAllClick: (() -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF273240)
        )
        if (showViewAll && onViewAllClick != null) {
            Text(
                text = "Lihat Semua",
                fontSize = 14.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeroCard(
    title: String,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundImageResId: Int? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundImageResId != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(backgroundImageResId)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Latar belakang hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.rimba_placeholder_default),
                    error = painterResource(id = R.drawable.rimba_placeholder_default)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = accentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledFeatureButton(
    info: FeatureButtonInfo,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = info.onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = info.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.title,
                modifier = Modifier.size(32.dp),
                tint = info.contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = info.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = info.contentColor
            )
        }
    }
}

// InfoRow mungkin tidak lagi digunakan jika WildlifeItemCard selalu simpel.
// Namun, EncyclopediaScreen mungkin masih membutuhkannya untuk dialog detail,
// jadi kita biarkan kecuali jika Anda memindahkannya ke EncyclopediaScreen.kt.
@Composable
fun InfoRow(label: String, value: String, labelColor: Color = Color(0xFF273240), valueColor: Color = Color(0xFF7B8794), valueFontWeight: FontWeight = FontWeight.Normal) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = valueFontWeight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildlifeItemCard(
    item: WildlifeEncyclopediaItem,
    accentColor: Color,
    onToggleFavorite: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
    // Parameter isSimpleView dihapus
) {
    val context = LocalContext.current
    val textPrimaryColor = Color(0xFF273240)
    val textSecondaryColor = Color(0xFF7B8794)

    val imageModelData: Any = remember(item.imageUrl) {
        val resId = context.resources.getIdentifier(item.imageUrl, "drawable", context.packageName)
        if (resId != 0) resId else R.drawable.rimba_placeholder_default
    }

    Card(
        onClick = onCardClick,
        modifier = modifier
            .width(160.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModelData)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.rimba_placeholder_default),
                    error = painterResource(id = R.drawable.rimba_placeholder_default),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) accentColor else Color.White
                    )
                }
            }

            // Kolom untuk informasi teks (selalu versi ringkas)
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp) // Sesuaikan tinggi minimum jika perlu
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.region,
                    fontSize = 12.sp,
                    color = textSecondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun HomeScreenPreview() {
    RimbaAITheme {
        HomeScreen(
            onNavigateToIdentification = {},
            onNavigateToEncyclopedia = {},
            onNavigateToChatbot = {},
            onNavigateToQuiz = {}
        )
    }
}

// Preview untuk WildlifeItemCard sekarang hanya akan menampilkan versi ringkas
@Preview(showBackground = true)
@Composable
fun WildlifeItemCardPreview() { // Nama preview disederhanakan
    RimbaAITheme {
        WildlifeItemCard(
            item = WildlifeEncyclopediaItem("1", "Harimau Sumatera", "Panthera tigris sumatrae", "rimba_placeholder_tiger", true, "Pulau Sumatera, Indonesia", "Kritis (Critically Endangered)"),
            accentColor = Color.Magenta,
            onToggleFavorite = {},
            onCardClick = {}
            // isSimpleView tidak lagi diperlukan
        )
    }
}
