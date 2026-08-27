package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.SkyNetBlack
import com.example.ui.theme.SkyNetPurpleMuted
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = stringResource(
        id = R.string.admob_banner_ad_unit_id
    )
) {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    val adRequest = remember {
        AdRequest.Builder().build()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SkyNetBlack)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {

        if (isInspectionMode) {

            Text(
                text = "AdMob Banner Preview",
                color = SkyNetPurpleMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(vertical = 12.dp)
            )

        } else {

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admob_banner_view"),

                factory = { ctx ->
                    try {
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            this.adUnitId = adUnitId
                            loadAd(adRequest)
                        }
                    } catch (e: Exception) {
                        android.view.View(ctx)
                    }
                },

                update = { adView ->
                    // No update required
                }
            )
        }
    }
}
