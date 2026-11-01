package internet.billing.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import internet.billing.manager.data.model.InternetPackage
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.localization.appStrings

@Composable
fun AddPackageDialog(
    packageToEdit: InternetPackage? = null,
    onDismiss: () -> Unit,
    onSave: (InternetPackage) -> Unit,
    onDelete: ((InternetPackage) -> Unit)? = null
) {
    var name by remember { mutableStateOf(packageToEdit?.name ?: "") }
    var speedText by remember { mutableStateOf(packageToEdit?.speedMbps?.toString() ?: "") }
    var priceText by remember { mutableStateOf(packageToEdit?.monthlyPrice?.toInt()?.toString() ?: "") }
    var description by remember { mutableStateOf(packageToEdit?.description ?: "Optical fiber high-speed internet") }
    var selectedColor by remember { mutableStateOf(packageToEdit?.colorHex ?: "#2979FF") }

    val colorOptions = listOf("#2979FF", "#00E676", "#7C4DFF", "#FF9100", "#00E5FF", "#FF4081")

    val dialogTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentCyan,
        unfocusedBorderColor = CardBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = AccentCyan,
        unfocusedLabelColor = TextSecondary,
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.55f else 0.85f),
        unfocusedContainerColor = BgCardLight.copy(alpha = if (ThemeManager.currentTheme.isDark) 0.35f else 0.65f),
        cursorColor = AccentCyan
    )
    val dialogTextStyle = androidx.compose.ui.text.TextStyle(
        color = TextPrimary,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium
    )

    Dialog(onDismissRequest = onDismiss) {
        InternetBillingTheme {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                contentColor = TextPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (packageToEdit == null) appStrings.addPackage else appStrings.editPackage,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = appStrings.close,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(appStrings.packageName, color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors,
                        textStyle = dialogTextStyle
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = speedText,
                            onValueChange = { speedText = it },
                            label = { Text(appStrings.speedMbps, color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = dialogTextFieldColors,
                            textStyle = dialogTextStyle
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text(appStrings.monthlyPrice, color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = dialogTextFieldColors,
                            textStyle = dialogTextStyle
                        )
                    }

                Spacer(modifier = Modifier.height(10.dp))

                Text(appStrings.accentColor, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (packageToEdit != null && onDelete != null) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = AccentRed.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = appStrings.deleteBtn,
                                color = AccentRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        Button(
                            onClick = {
                                val speed = speedText.toIntOrNull() ?: 10
                                val price = priceText.toDoubleOrNull() ?: 500.0
                                onSave(
                                    packageToEdit.copy(
                                        name = name.ifBlank { "$speed Mbps" },
                                        speedMbps = speed,
                                        monthlyPrice = price,
                                        description = description,
                                        colorHex = selectedColor
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = (speedText.toIntOrNull() ?: 0) > 0 && (priceText.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Text(
                                text = appStrings.savePackage,
                                color = BgDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("${appStrings.deletePackage}?", color = TextPrimary) },
                            text = { Text("Are you sure you want to delete ${packageToEdit.name}?", color = TextSecondary) },
                            containerColor = BgCard,
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteConfirm = false
                                        onDelete(packageToEdit)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                                ) {
                                    Text(appStrings.deleteBtn, color = Color.White)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                                    Text(appStrings.cancel, color = TextSecondary)
                                }
                            }
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val speed = speedText.toIntOrNull() ?: 10
                            val price = priceText.toDoubleOrNull() ?: 500.0
                            onSave(
                                InternetPackage(
                                    name = name.ifBlank { "$speed Mbps" },
                                    speedMbps = speed,
                                    monthlyPrice = price,
                                    description = description,
                                    colorHex = selectedColor
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        enabled = (speedText.toIntOrNull() ?: 0) > 0 && (priceText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text(
                            text = appStrings.savePackage,
                            color = BgDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

