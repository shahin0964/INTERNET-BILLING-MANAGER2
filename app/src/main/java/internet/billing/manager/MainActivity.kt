package internet.billing.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import internet.billing.manager.ui.screens.MainScreen
import internet.billing.manager.ui.theme.InternetBillingTheme
import internet.billing.manager.ui.viewmodel.BillingViewModel
import internet.billing.manager.ui.viewmodel.BillingViewModelFactory

class MainActivity : ComponentActivity() {

    private val billingViewModel: BillingViewModel by viewModels {
        BillingViewModelFactory((application as InternetBillingApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternetBillingTheme {
                MainScreen(viewModel = billingViewModel)
            }
        }
    }
}
