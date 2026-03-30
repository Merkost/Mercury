package com.github.merkost.mercury.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object MercurySpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

object MercurySize {
    val borderWidth = 1.dp
    val borderWidthFocus = 2.dp
    val radiusSm = 4.dp
    val radiusMd = 6.dp
    val radiusLg = 8.dp
    val iconSm = 14.dp
    val iconMd = 16.dp
    val iconLg = 20.dp
    val treeIndent = 16.dp
    val treeRowHeight = 28.dp
    val entityCardMinWidth = 180.dp
    val entityCardMaxWidth = 280.dp
    val entityCardPadding = 12.dp
    val entityFieldHeight = 24.dp
    val relationshipLineWidth = 1.dp
    val relationshipLineWidthHighlight = 2.dp
    val tabHeight = 36.dp
    val tabIndicatorHeight = 2.dp
    val searchHeight = 32.dp
}

object MercuryElevation {
    val none = 0.dp
    val subtle = 1.dp
    val raised = 3.dp
    val overlay = 8.dp
}

object MercuryMotion {
    val durationFast = 100
    val durationNormal = 200
    val durationSlow = 350
    val easingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easingDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)
    val easingAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}
