package internet.billing.manager.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.ui.localization.AppLanguage
import internet.billing.manager.ui.localization.LanguageManager
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*

@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = LanguageManager.currentLanguage

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgCard,
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appStrings.selectLanguageTitle,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = appStrings.selectLanguageSubtitle,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LanguageOptionCard(
                        language = AppLanguage.BANGLA,
                        badgeText = "বাং",
                        primaryText = "বাংলা (Bangla)",
                        secondaryText = "বাংলাদেশের স্থানীয় ভাষা",
                        isSelected = currentLang == AppLanguage.BANGLA,
                        onClick = {
                            LanguageManager.setLanguage(AppLanguage.BANGLA, context)
                            Toast.makeText(context, "ভাষা পরিবর্তন করা হয়েছে (বাংলা)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    LanguageOptionCard(
                        language = AppLanguage.ENGLISH,
                        badgeText = "EN",
                        primaryText = "English",
                        secondaryText = "Standard International English",
                        isSelected = currentLang == AppLanguage.ENGLISH,
                        onClick = {
                            LanguageManager.setLanguage(AppLanguage.ENGLISH, context)
                            Toast.makeText(context, "Language changed to English", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text(
                        text = appStrings.confirmBtn,
                        color = OnAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: AppLanguage,
    badgeText: String,
    primaryText: String,
    secondaryText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.12f) else BgCardLight,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) AccentCyan else CardBorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge / Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AccentCyan.copy(alpha = 0.25f)
                        else CardBorderSubtle.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = if (isSelected) AccentCyan else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = secondaryText,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Radio / Check Circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentCyan else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else CardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = OnAccent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
