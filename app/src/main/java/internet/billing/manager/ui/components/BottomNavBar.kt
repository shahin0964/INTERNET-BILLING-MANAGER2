package internet.billing.manager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*

enum class BottomNavScreen(
    val defaultTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    CUSTOMERS("Customers", Icons.Filled.People, Icons.Outlined.People),
    BILLING("Billing", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    COLLECTION("Collection", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

fun BottomNavScreen.getLocalizedTitle(): String = when (this) {
    BottomNavScreen.DASHBOARD -> appStrings.navDashboard
    BottomNavScreen.CUSTOMERS -> appStrings.navCustomers
    BottomNavScreen.BILLING -> appStrings.navBilling
    BottomNavScreen.COLLECTION -> appStrings.navCollection
    BottomNavScreen.MORE -> appStrings.navMore
}

@Composable
fun BottomNavBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, bottom = 8.dp, top = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                    ambientColor = Color.Black
                ),
            shape = RoundedCornerShape(32.dp),
            color = NavBg,
            tonalElevation = 8.dp,
            border = BorderStroke(
                1.3.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                        Color(0xFF2979FF).copy(alpha = 0.65f),
                        Color(0xFFA855F7).copy(alpha = 0.4f),
                        Color(0xFF00E5FF).copy(alpha = 0.4f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    val tint = if (isSelected) AccentCyan else NavUnselected
                    val title = screen.getLocalizedTitle()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    AccentCyan.copy(alpha = 0.18f),
                                                    Color(0xFF1E88E5).copy(alpha = 0.08f)
                                                )
                                            )
                                        )
                                        .border(
                                            BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                                            RoundedCornerShape(22.dp)
                                        )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onScreenSelected(screen) }
                            .padding(vertical = 5.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = title,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                color = tint,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
