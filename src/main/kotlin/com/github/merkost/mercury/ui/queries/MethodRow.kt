package com.github.merkost.mercury.ui.queries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.merkost.mercury.model.DaoMethod
import com.github.merkost.mercury.model.DaoMethodType
import com.github.merkost.mercury.ui.components.MercuryBadge
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MethodRow(
    method: DaoMethod,
    daoQualifiedName: String,
    isExpanded: Boolean,
    touchedEntityNames: List<String>,
    onToggleExpand: () -> Unit,
    onNavigateToSource: () -> Unit,
    onNavigateToEntity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isHovered by rememberHoverState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) colors.surfaceHover else Color.Transparent,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val isQuery = method.type == DaoMethodType.QUERY || method.type == DaoMethodType.RAW_QUERY

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MercurySize.treeRowHeight)
                .background(backgroundColor)
                .onHoverChanged { isHovered = it }
                .clickable {
                    if (isQuery && method.query != null) {
                        onToggleExpand()
                    } else {
                        onNavigateToSource()
                    }
                }
                .padding(horizontal = MercurySpacing.xl, vertical = MercurySpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = method.name,
                style = typography.titleSmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(0.35f)
            )
            Text(
                text = "\u2192 ${method.returnType}",
                style = typography.codeMedium,
                color = colors.textMuted,
                maxLines = 1,
                modifier = Modifier.weight(0.35f)
            )
            Row(
                modifier = Modifier.weight(0.3f),
                horizontalArrangement = Arrangement.End
            ) {
                val badgeText = if (method.onConflict != null) {
                    "${method.type.name} \u00B7 ${method.onConflict.name}"
                } else {
                    method.type.name
                }
                MercuryBadge(badgeText)
            }
        }

        AnimatedVisibility(
            visible = isExpanded && isQuery && method.query != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = MercurySpacing.xl, end = MercurySpacing.xl, bottom = MercurySpacing.sm)
            ) {
                SqlBlock(
                    sql = method.query ?: "",
                    modifier = Modifier.fillMaxWidth()
                )

                if (touchedEntityNames.isNotEmpty()) {
                    TouchesLine(
                        entityNames = touchedEntityNames,
                        onNavigateToEntity = onNavigateToEntity,
                        modifier = Modifier.padding(top = MercurySpacing.xs)
                    )
                }
            }
        }
    }
}

@Composable
private fun TouchesLine(
    entityNames: List<String>,
    onNavigateToEntity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Touches: ",
            style = typography.labelSmall,
            color = colors.textMuted
        )
        entityNames.forEachIndexed { index, name ->
            if (index > 0) {
                Text(
                    text = ", ",
                    style = typography.bodySmall,
                    color = colors.textMuted
                )
            }
            Text(
                text = name,
                style = typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.clickable { onNavigateToEntity(name) }
            )
        }
    }
}
