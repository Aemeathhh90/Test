package com.kakaanime.app.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun KakaSkeleton(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 120.dp
) {
    val transition = rememberInfiniteTransition(label = "kaka-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kaka-skeleton-alpha"
    )

    Box(
        modifier = modifier
            .height(height)
            .alpha(alpha)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
    )
}

@Composable
fun KakaSkeletonColumn(
    modifier: Modifier = Modifier,
    rows: Int = 3
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(rows) { index ->
            KakaSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (index == 0) 0.dp else 8.dp),
                height = if (index == 0) 180.dp else 72.dp
            )
        }
    }
}
