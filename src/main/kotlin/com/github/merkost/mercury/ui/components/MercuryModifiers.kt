package com.github.merkost.mercury.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.onHoverChanged(onHoverChange: (Boolean) -> Unit): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Enter -> onHoverChange(true)
                    PointerEventType.Exit -> onHoverChange(false)
                }
            }
        }
    }

@Composable
fun rememberHoverState(): MutableState<Boolean> = remember { mutableStateOf(false) }
