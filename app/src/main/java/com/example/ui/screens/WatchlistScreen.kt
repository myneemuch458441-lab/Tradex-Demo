package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetCategory
import com.example.data.model.MarketAsset
import com.example.data.model.OrderType
import com.example.ui.components.AssetCard
import com.example.ui.theme.*

@Composable
fun WatchlistScreen(
    assets: List<MarketAsset>,
    allAssets: List<MarketAsset>,
    selectedCategory: AssetCategory,
    onCategorySelected: (AssetCategory) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    showFavoritesOnly: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    onAssetClick: (MarketAsset) -> Unit,
    onBuyClick: (MarketAsset) -> Unit,
    onSellClick: (MarketAsset) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("watchlist_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_asset_input"),
            placeholder = {
                Text("स्टॉक या इंडेक्स खोजें (Search Reliance, Nifty...)", fontSize = 13.sp, color = TextMuted)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorites filter pill
            FilterChip(
                selected = showFavoritesOnly,
                onClick = onToggleFavoritesOnly,
                label = { Text("पसंदीदा (⭐)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (showFavoritesOnly) AccentGold else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGold,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = showFavoritesOnly,
                    borderColor = if (showFavoritesOnly) AccentGold else DarkBorder
                )
            )

            // Category pills
            AssetCategory.values().forEach { category ->
                val isSelected = !showFavoritesOnly && selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text("${category.displayName} (${category.hindiName})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AccentCyan,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AccentCyan else DarkBorder
                    )
                )
            }
        }

        // Top Movers Glance (Gainers & Losers)
        if (searchQuery.isBlank() && !showFavoritesOnly && selectedCategory == AssetCategory.ALL && allAssets.isNotEmpty()) {
            val topGainer = allAssets.maxByOrNull { it.changePercent }
            val topLoser = allAssets.minByOrNull { it.changePercent }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                topGainer?.let { gainer ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAssetClick(gainer) },
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("टॉप गेनर", fontSize = 9.sp, color = TextMuted)
                                Text(gainer.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                "+${String.format("%.2f", gainer.changePercent)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BullGreen
                            )
                        }
                    }
                }

                topLoser?.let { loser ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAssetClick(loser) },
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("टॉप लूजर", fontSize = 9.sp, color = TextMuted)
                                Text(loser.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                "${String.format("%.2f", loser.changePercent)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearRed
                            )
                        }
                    }
                }
            }
        }

        // Watchlist Items
        if (assets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showFavoritesOnly) "कोई पसंदीदा स्टॉक नहीं जोड़ा गया" else "कोई स्टॉक नहीं मिला",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showFavoritesOnly) "स्टार आइकन दबाकर पसंदीदा जोड़ें" else "कृपया दूसरा नाम खोजें",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(assets, key = { it.symbol }) { asset ->
                    AssetCard(
                        asset = asset,
                        onClick = { onAssetClick(asset) },
                        onBuyClick = { onBuyClick(asset) },
                        onSellClick = { onSellClick(asset) },
                        onFavoriteToggle = { onFavoriteToggle(asset.symbol) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
