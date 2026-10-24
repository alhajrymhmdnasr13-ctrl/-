package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.GarmentGuideScreen
import com.example.ui.screens.LiveMeasureScreen
import com.example.ui.screens.PhotoMeasureScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TailorCardBorder
import com.example.ui.theme.TailorDarkBg
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.TailorViewModel

enum class TailorTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIVE("الكاميرا الحية", Icons.Default.CenterFocusStrong),
    PHOTO("مسح صورة", Icons.Default.AddPhotoAlternate),
    CLIENTS("سجل الزبائن", Icons.Default.People),
    GUIDE("دليل التفصيل", Icons.AutoMirrored.Filled.MenuBook)
}

@Composable
fun MainScreen(
    viewModel: TailorViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(TailorTab.LIVE) }
    val selectedClient by viewModel.selectedClient.collectAsState()

    Scaffold(
        bottomBar = {
            if (selectedClient == null) {
                NavigationBar(
                    containerColor = TailorSurfaceDark,
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    TailorTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) GoldPrimary else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GoldPrimary else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = TailorDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedClient != null) {
                ClientDetailScreen(
                    client = selectedClient!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectClient(null) }
                )
            } else {
                when (currentTab) {
                    TailorTab.LIVE -> LiveMeasureScreen(viewModel = viewModel)
                    TailorTab.PHOTO -> PhotoMeasureScreen(viewModel = viewModel)
                    TailorTab.CLIENTS -> ClientsScreen(
                        viewModel = viewModel,
                        onClientSelected = { client -> viewModel.selectClient(client) }
                    )
                    TailorTab.GUIDE -> GarmentGuideScreen()
                }
            }
        }
    }
}
