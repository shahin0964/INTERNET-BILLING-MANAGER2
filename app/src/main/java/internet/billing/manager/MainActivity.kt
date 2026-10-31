package internet.billing.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import internet.billing.manager.ui.screens.AnimatedSplashScreen
import internet.billing.manager.ui.screens.MainScreen
import internet.billing.manager.ui.theme.InternetBillingTheme
import internet.billing.manager.ui.theme.ThemeManager
import internet.billing.manager.ui.viewmodel.BillingViewModel
import internet.billing.manager.ui.viewmodel.BillingViewModelFactory

class MainActivity : ComponentActivity() {

    private val billingViewModel: BillingViewModel by viewModels {
        BillingViewModelFactory((application as InternetBillingApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            InternetBillingTheme {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 400),
                    label = "SplashCrossfade"
                ) { isSplash ->
                    if (isSplash) {
                        AnimatedSplashScreen(
                            onAnimationFinished = { showSplash = false }
                        )
                    } else {
                        MainScreen(viewModel = billingViewModel)
                    }
                }
            }
        }
    }
}
