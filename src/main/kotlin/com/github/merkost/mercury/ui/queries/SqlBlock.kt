package com.github.merkost.mercury.ui.queries

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

data class SqlSegment(val text: String, val isKeyword: Boolean)

private val SQL_KEYWORDS = setOf(
    "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
    "CROSS", "NATURAL", "INSERT", "INTO", "UPDATE", "DELETE", "SET", "VALUES",
    "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "AND", "OR", "ON",
    "AS", "IN", "NOT", "LIKE", "BETWEEN", "EXISTS", "UNION", "ALL", "DISTINCT",
    "CASE", "WHEN", "THEN", "ELSE", "END", "NULL", "TRUE", "FALSE"
)

fun parseSqlSegments(sql: String): List<SqlSegment> {
    if (sql.isBlank()) return emptyList()

    val segments = mutableListOf<SqlSegment>()
    val words = sql.split(Regex("(?<=\\s)|(?=\\s)"))
    val buffer = StringBuilder()
    var bufferIsKeyword: Boolean? = null

    for (word in words) {
        val isKw = word.trim().uppercase() in SQL_KEYWORDS && word.isNotBlank()
        if (bufferIsKeyword == null) {
            bufferIsKeyword = isKw
            buffer.append(word)
        } else if (isKw == bufferIsKeyword) {
            buffer.append(word)
        } else {
            segments.add(SqlSegment(buffer.toString(), bufferIsKeyword))
            buffer.clear()
            buffer.append(word)
            bufferIsKeyword = isKw
        }
    }
    if (buffer.isNotEmpty() && bufferIsKeyword != null) {
        segments.add(SqlSegment(buffer.toString(), bufferIsKeyword))
    }
    return segments
}

@Composable
fun buildSqlAnnotatedString(sql: String): AnnotatedString {
    val colors = MercuryTheme.colors
    val segments = parseSqlSegments(sql)

    return buildAnnotatedString {
        for (segment in segments) {
            if (segment.isKeyword) {
                withStyle(SpanStyle(color = colors.textSecondary, fontWeight = FontWeight.Bold)) {
                    append(segment.text)
                }
            } else {
                withStyle(SpanStyle(color = colors.textMuted)) {
                    append(segment.text)
                }
            }
        }
    }
}

@Composable
fun SqlBlock(
    sql: String,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    val annotatedSql = buildSqlAnnotatedString(sql)

    Text(
        text = annotatedSql,
        style = typography.codeMedium,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceRecessed, RoundedCornerShape(MercurySize.radiusSm))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = MercurySpacing.sm, vertical = MercurySpacing.xs)
    )
}
