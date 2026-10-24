package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TailorCardBorder
import com.example.ui.theme.TailorCardDark
import com.example.ui.theme.TailorDarkBg
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun GarmentGuideScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TailorDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "دليل الخياطة والتفصيل المتقدم",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "الذوق الرفيع: وضعيات الكاميرا الدقيقة، مقاسات الثوب الخليجي، والبدلات العالمية",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TailorSurfaceDark,
            contentColor = GoldPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("وضعية الكاميرا A") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("مقاسات الثوب") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("مقاسات البدلة") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> APoseGuideSection()
            1 -> ThobeGuideSection()
            2 -> SuitGuideSection()
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun APoseGuideSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideCard(
            title = "١. وضعية الوقوف القياسية (وضعية A)",
            icon = Icons.Default.AccessibilityNew,
            color = NeonYellow,
            content = "يقف الزبون مستقيماً مواجهاً لعدسة الكاميرا مباشرة مع مباعدة القدمين بعرض الكتفين والذراعين مائلين للخارج بزاوية 20° إلى 30° عن الجسم لمنع تداخل نقاط المعصم ومحيط الخصر."
        )

        GuideCard(
            title = "٢. مسافة وتأطير الكاميرا (2.5 – 3.0 أمتار)",
            icon = Icons.Default.Straighten,
            color = NeonCyan,
            content = "ثبت الهاتف بارتفاع الخصر (حوالي 1 متر عن الأرض) وعلى بعد 2.5 إلى 3 أمتار حتى يظهر جسم العميل بالكامل من الرأس حتى الكعبين داخل المستطيل الإرشادي."
        )

        GuideCard(
            title = "٣. المعايرة الذكية لدقة متناهية",
            icon = Icons.Default.Tune,
            color = NeonGreen,
            content = "اضغط على زر 'معايرة' وأدخل طول العميل الحقيقي بالسنتيمتر (مثلاً 175 سم). يقوم محرك الرؤية الرياضي باستخراج معامل مقياس البكسل بالملليمتر بدقة متناهية."
        )

        GuideCard(
            title = "٤. نوعية الملابس المناسبة للقياس",
            icon = Icons.Default.Info,
            color = GoldPrimary,
            content = "يُستحسن أن يرتدي الزبون ملابس مفصلة على الجسم (تيشيرت خفيف وبنطال قطني) وتجنب الجاكيتات الفضفاضة لضمان التقاط مفاصل الجسم الحقيقية."
        )
    }
}

@Composable
private fun ThobeGuideSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "دليل مقاسات الثوب السعودي والخليجي",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GoldPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "المقاسات التقليدية المعتمدة بحسب طول القامة الإجمالي وانسدال الثوب من الكتف:",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ThobeRow("طول القامة (سم)", "رقم مقاس الثوب", "طول الكم (إنش/سم)")
            ThobeRow("160 - 165 سم", "52", "23 - 24 إنش (58 - 61 سم)")
            ThobeRow("165 - 170 سم", "54", "24 - 25 إنش (61 - 63 سم)")
            ThobeRow("170 - 175 سم", "56", "25 - 26 إنش (63 - 66 سم)")
            ThobeRow("175 - 180 سم", "58", "26 - 27 إنش (66 - 68 سم)")
            ThobeRow("180 - 185 سم", "60", "27 - 28 إنش (68 - 71 سم)")
            ThobeRow("185+ سم", "62", "28+ إنش (71+ سم)")
        }
    }
}

@Composable
private fun ThobeRow(col1: String, col2: String, col3: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = col1, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = col2, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, modifier = Modifier.weight(0.8f))
        Text(text = col3, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun SuitGuideSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "جدول مقاسات الجاكيت والبدلة الرسمية العالمية",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GoldPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ThobeRow("محيط الصدر", "الجاكيت الأمريكي/البريطاني", "الجاكيت الأوروبي")
            ThobeRow("92 - 96 سم", "38 R", "48")
            ThobeRow("97 - 101 سم", "40 R", "50")
            ThobeRow("102 - 106 سم", "42 R", "52")
            ThobeRow("107 - 111 سم", "44 R", "54")
            ThobeRow("112 - 116 سم", "46 R", "56")

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "إضافات الراحة في التفصيل (Ease Allowances):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• القصة الضيقة (Slim Fit): إضافة 4 إلى 6 سم لمحيط الصدر والخصر\n• القصة المعتدلة (Regular Fit): إضافة 8 إلى 10 سم لحرية الحركة\n• القصة الواسعة والتقليدية: إضافة 12 إلى 15 سم لراحة تامة",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GuideCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
            }
        }
    }
}
