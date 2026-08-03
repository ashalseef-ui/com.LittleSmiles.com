package com.LittleSmiles.com.ui.features.auth

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.repository.PremiumProduct
import com.LittleSmiles.com.core.domain.repository.PremiumProductId
import com.LittleSmiles.com.core.domain.repository.PurchaseResult
import com.LittleSmiles.com.ui.components.ParentalGateDialog
import com.LittleSmiles.com.ui.components.ParentalGateStrength
import com.LittleSmiles.com.ui.theme.ErrorRed
import com.LittleSmiles.com.ui.theme.RainbowGreen
import com.LittleSmiles.com.ui.theme.RainbowOrange
import com.LittleSmiles.com.ui.theme.SkyBlueDark
import com.LittleSmiles.com.ui.theme.SlateBorder
import com.LittleSmiles.com.ui.theme.SlateText
import com.LittleSmiles.com.ui.theme.SuccessGreen

@Composable
fun UpgradeScreen(
    onPurchased: () -> Unit,
    onContinueFree: () -> Unit,
    onLoginForTrial: (() -> Unit)? = null,
    viewModel: UpgradeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val isGuest by viewModel.isGuest.collectAsState()

    var showParentalGate by remember { mutableStateOf(false) }
    var pendingProduct by remember { mutableStateOf<PremiumProductId?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.bootstrap()
    }

    LaunchedEffect(purchaseState) {
        when (val state = purchaseState) {
            is PurchaseResult.Success -> {
                statusMessage = "Premium unlocked — thank you!"
                onPurchased()
                viewModel.clearPurchaseState()
            }
            is PurchaseResult.Cancelled -> {
                statusMessage = null
                viewModel.clearPurchaseState()
            }
            is PurchaseResult.NotConfigured -> {
                statusMessage =
                    "Create Play products premium_monthly, premium_yearly, premium_lifetime, then reopen."
                viewModel.clearPurchaseState()
            }
            is PurchaseResult.Error -> {
                statusMessage = state.message
                viewModel.clearPurchaseState()
            }
            null -> Unit
        }
    }

    if (showParentalGate) {
        ParentalGateDialog(
            strength = ParentalGateStrength.STRICT,
            onDismiss = {
                showParentalGate = false
                pendingProduct = null
            },
            onSuccess = {
                showParentalGate = false
                val product = pendingProduct
                pendingProduct = null
                val activity = context as? Activity
                if (product != null && activity != null) {
                    viewModel.purchase(activity, product)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F9FF))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = RainbowOrange,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "See What's Coming",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = SkyBlueDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We're working on new games! Premium access with 20+ more activities and Magic Markers is coming in the next update.",
            textAlign = TextAlign.Center,
            color = SlateText,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        BenefitRow("14-Day Free Trial (Coming Soon)")
        BenefitRow("All learning games & Magic Markers")
        BenefitRow("Ad-free, kid-safe experience")
        BenefitRow("Progress synced for your family")

        /*
        if (isGuest && onLoginForTrial != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onLoginForTrial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RainbowGreen)
            ) {
                Text("Start 14-Day Free Trial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                text = "Full access for 14 days after you create an account",
                fontSize = 12.sp,
                color = SlateText,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        */

        Spacer(modifier = Modifier.height(32.dp))
        
        // Coming Soon Card
        Card(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, RainbowOrange),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "PREMIUM COMING SOON",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = RainbowOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Get ready for full access! A 14-day free trial and flexible subscription plans will be available very soon.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = SlateText
                )
            }
        }

        /*
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Go Premium",
            fontWeight = FontWeight.ExtraBold,
            color = SkyBlueDark,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))

        products.forEach { product ->
            ProductCard(
                product = product,
                enabled = purchaseState == null,
                onClick = {
                    pendingProduct = product.id
                    showParentalGate = true
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        TextButton(
            onClick = { viewModel.restore() },
            enabled = purchaseState == null
        ) {
            Text("Restore purchases", color = SkyBlueDark, fontWeight = FontWeight.Bold)
        }
        */

        statusMessage?.let { message ->
            Text(
                text = message,
                color = if (message.contains("thank", ignoreCase = true)) SuccessGreen else ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "We're loving Little Buds Academy for early learning — tracing, colors, shapes & more! " +
                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                    )
                }
                context.startActivity(Intent.createChooser(share, "Share Little Buds Academy"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = SkyBlueDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share with other parents", color = SkyBlueDark, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onContinueFree) {
            Text("Keep playing free games", color = SlateText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProductCard(
    product: PremiumProduct,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val border = if (product.isSuggested) {
        BorderStroke(2.dp, RainbowOrange)
    } else {
        BorderStroke(1.dp, SlateBorder)
    }
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (product.isSuggested) Color(0xFFFFF7ED) else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.id.title, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    if (product.isSuggested) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = RainbowOrange, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "BEST VALUE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(product.id.subtitle, fontSize = 13.sp, color = SlateText)
            }
            Text(
                text = product.priceLabel,
                fontWeight = FontWeight.Bold,
                color = SkyBlueDark,
                fontSize = 15.sp
            )
        }
    }
}

/** Soft replacement for the old hard lockout screen. */
@Composable
fun TrialExpiredScreen(onUpgrade: () -> Unit, onLogout: () -> Unit) {
    UpgradeScreen(
        onPurchased = onUpgrade,
        onContinueFree = onUpgrade,
        onLoginForTrial = null
    )
}
