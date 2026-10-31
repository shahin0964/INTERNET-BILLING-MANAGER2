package internet.billing.manager.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.ui.theme.*

enum class BottomNavScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.GridView, Icons.Outlined.GridView),
    CUSTOMERS("Customers", Icons.Filled.People, Icons.Outlined.People),
    BILLING("Billing", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    COLLECTION("Collection", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

@Composable
fun BottomNavBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val tint = if (isSelected) AccentCyan else NavUnselected

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onScreenSelected(screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = screen.title,
                        color = tint,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
