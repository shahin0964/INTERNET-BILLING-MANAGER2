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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val tint = if (isSelected) AccentCyan else NavUnselected
                val title = screen.getLocalizedTitle()

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onScreenSelected(screen) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        color = tint,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
