package com.example.rimbaai

import androidx.compose.foundation.BorderStroke // Tidak terpakai di sini, tapi mungkin di WildlifeItemCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Tidak lagi digunakan di sini untuk WildlifeItemCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close // Untuk tombol close di dialog
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rimbaai.ui.theme.RimbaAITheme

// Composable untuk Dialog Detail Satwa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WildlifeDetailDialog(
    item: WildlifeEncyclopediaItem, // Menerima item yang akan ditampilkan
    onDismissRequest: () -> Unit
) {
    val dialogBackgroundColor = Color.White
    val textPrimaryColor = Color(0xFF273240)
    val textSecondaryColor = Color(0xFF7B8794)
    val context = LocalContext.current

    val imageModelData: Any = remember(item.imageUrl) {
        val resId = context.resources.getIdentifier(item.imageUrl, "drawable", context.packageName)
        if (resId != 0) resId else R.drawable.rimba_placeholder_default // Pastikan placeholder_default ada
    }

    // Logika pewarnaan untuk status konservasi berdasarkan gambar Anda
    val statusBackgroundColor = when (item.conservationStatus.lowercase()) {
        "kritis (critically endangered)" -> Color(0xFFFADBD8) // Krem kemerahan muda
        "terancam punah" -> Color(0xFFF5B7B1) // Merah muda lebih pekat
        "terancam (endangered)" -> Color(0xFFFAE5D3) // Oranye/kuning muda
        else -> Color(0xFFE8F6F3) // Default jika status lain
    }
    val statusTextColor = when (item.conservationStatus.lowercase()) {
        "kritis (critically endangered)" -> Color(0xFFB03A2E) // Merah tua
        "terancam punah" -> Color(0xFF922B21) // Merah lebih tua
        "terancam (endangered)" -> Color(0xFFB9770E) // Oranye/coklat tua
        else -> Color(0xFF1E8449) // Default
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor),
            modifier = Modifier
                .fillMaxWidth(0.9f) // Dialog tidak terlalu lebar
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bagian Gambar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                    // Tombol Close di pojok kanan atas gambar
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bagian Informasi Teks
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp) // Padding lebih merata
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp) // Jarak antar baris info
                ) {
                    Text(
                        text = item.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        textAlign = TextAlign.Start, // Rata kiri untuk nama
                        modifier = Modifier.fillMaxWidth()
                    )

                    DetailInfoRowDialog(label = "Wilayah:", value = item.region, valueColor = textSecondaryColor)
                    DetailInfoRowDialog(label = "Nama Latin:", value = item.species, valueColor = textSecondaryColor)

                    // Status Konservasi dengan Latar Belakang Berwarna
                    Text(
                        text = "Status:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimaryColor
                    )
                    Box(
                        modifier = Modifier
                            .wrapContentWidth() // Lebar box menyesuaikan teks status
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBackgroundColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp), // Padding lebih kecil untuk status
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.conservationStatus.uppercase(),
                            fontSize = 12.sp, // Font status lebih kecil
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Composable helper untuk baris info di dialog detail
@Composable
fun DetailInfoRowDialog(label: String, value: String, labelColor: Color = Color(0xFF273240), valueColor: Color = Color(0xFF7B8794)) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp, // Font label lebih kecil
            fontWeight = FontWeight.Normal, // Tidak bold
            color = labelColor.copy(alpha = 0.7f) // Label sedikit lebih transparan
        )
        Text(
            text = value,
            fontSize = 14.sp, // Font value sedikit lebih besar dari label
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaScreen(
    onNavigateBack: () -> Unit
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val accentColor = Color(0xFFFF9800)
    val textPrimaryColor = Color(0xFF273240)
    val searchBarBackgroundColor = Color.White

    var searchQuery by remember { mutableStateOf("") }

    // State untuk dialog detail
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedWildlifeItem by remember { mutableStateOf<WildlifeEncyclopediaItem?>(null) }


    val initialItems = remember {
        listOf(
            WildlifeEncyclopediaItem("1", "Harimau \nSumatera", "Panthera tigris sumatrae", "rimba_placeholder_tiger", true, "Hutan Lindung Sangir, Sumatera", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("2", "Orangutan Kalimantan", "Pongo pygmaeus", "rimba_placeholder_orangutan", false, "Taman Nasional Tanjung Puting, Kalimantan", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("3", "Badak Jawa", "Rhinoceros sondaicus", "rimba_placeholder_rhino", false, "Taman Nasional Ujung Kulon, Banten", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("4", "Komodo", "Varanus komodoensis", "rimba_placeholder_komodo", true, "Pulau Komodo Flores, Nusa Tenggara Timur", "Terancam (Endangered)"),
            WildlifeEncyclopediaItem("5", "Elang Jawa", "Nisaetus bartelsi", "rimba_placeholder_eagle", false, "Taman Nasional Alas Purwo, Banyuwangi", "Terancam (Endangered)"),
            WildlifeEncyclopediaItem("6", "Bekantan", "Nasalis larvatus", "rimba_placeholder_bekantan", false, "Taman Nasional Sebangau, Kalimantan", "Terancam (Endangered)"),
            WildlifeEncyclopediaItem("7", "Anoa", "Bubalus depressicornis", "rimba_placeholder_anoa", false, "Taman Nasional Biau, Sulawesi", "Terancam (Endangered)"), // Nama disingkat
            WildlifeEncyclopediaItem("8", "Kura-kura Rote", "Chelodina mccordi", "rimba_placeholder_kurarote", false, "Pulau Rote, Nusa Tenggara Timur, Indonesia.", "Kritis (Critically Endangered)"), // Nama disingkat
            WildlifeEncyclopediaItem("9", "Pesut Mahakam", "Orcaella brevirostris", "rimba_placeholder_pesut", false, "Sungai Mahakam, Kalimantan Timur, Indonesia, serta beberapa sistem sungai air tawar dan estuari di Asia Tenggara.", "Kritis (Critically Endangered)"),
            WildlifeEncyclopediaItem("10", "Cendrawasih", "Paradisaea rubra", "rimba_placeholder_cendrawasih", false, "Taman Nasional Lorentz, Papua Barat", "Hampir Terancam (Near Threatened)")
        )
    }
    val encyclopediaItemsState: SnapshotStateList<WildlifeEncyclopediaItem> = remember { initialItems.toMutableStateList() }

    val onToggleFavorite = { itemId: String, isCurrentlyFavorite: Boolean ->
        val itemIndex = encyclopediaItemsState.indexOfFirst { it.id == itemId }
        if (itemIndex != -1) {
            val oldItem = encyclopediaItemsState[itemIndex]
            encyclopediaItemsState[itemIndex] = oldItem.copy(isFavorite = !isCurrentlyFavorite)
        }
    }

    val filteredItems = remember(searchQuery, encyclopediaItemsState) {
        val listToFilter = encyclopediaItemsState.toList()
        if (searchQuery.isBlank()) {
            listToFilter
        } else {
            listToFilter.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.species.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ensiklopedia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = textPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = textPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Cari satwa...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = searchBarBackgroundColor,
                    unfocusedContainerColor = searchBarBackgroundColor,
                    disabledContainerColor = searchBarBackgroundColor,
                )
            )

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada satwa ditemukan.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        WildlifeItemCard( // Memanggil WildlifeItemCard dari HomeScreen.kt
                            item = item,
                            accentColor = accentColor,
                            onToggleFavorite = { onToggleFavorite(item.id, item.isFavorite) },
                            onCardClick = { // Menggunakan parameter onCardClick
                                selectedWildlifeItem = item
                                showDetailDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Tampilkan dialog detail jika showDetailDialog adalah true dan ada item yang dipilih
        if (showDetailDialog && selectedWildlifeItem != null) {
            WildlifeDetailDialog(
                item = selectedWildlifeItem!!,
                onDismissRequest = { showDetailDialog = false }
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun EncyclopediaScreenPreview() {
    RimbaAITheme {
        EncyclopediaScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun WildlifeDetailDialogPreviewKritis() {
    RimbaAITheme {
        WildlifeDetailDialog(
            item = WildlifeEncyclopediaItem(
                id = "1",
                name = "Harimau Sumatera", // Nama lebih pendek untuk preview
                species = "Panthera tigris sumatrae",
                imageUrl = "rimba_placeholder_tiger",
                isFavorite = true,
                region = "Pulau Sumatera, Indonesia", // Wilayah lebih pendek
                conservationStatus = "Kritis (Critically Endangered)"
            ),
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun WildlifeDetailDialogPreviewTerancamPunah() { // Menambahkan preview untuk Terancam Punah
    RimbaAITheme {
        WildlifeDetailDialog(
            item = WildlifeEncyclopediaItem(
                id = "custom",
                name = "Hewan Fiktif",
                species = "Fictitious animalus",
                imageUrl = "rimba_placeholder_default", // Menggunakan placeholder default
                isFavorite = false,
                region = "Imajinasi",
                conservationStatus = "Terancam Punah" // Status baru untuk tes warna
            ),
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun WildlifeDetailDialogPreviewTerancam() {
    RimbaAITheme {
        WildlifeDetailDialog(
            item = WildlifeEncyclopediaItem(
                id = "4",
                name = "Komodo",
                species = "Varanus komodoensis",
                imageUrl = "rimba_placeholder_komodo",
                isFavorite = false,
                region = "Pulau Komodo, NTT", // Wilayah lebih pendek
                conservationStatus = "Terancam (Endangered)"
            ),
            onDismissRequest = {}
        )
    }
}
