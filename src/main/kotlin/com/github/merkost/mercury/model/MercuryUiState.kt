package com.github.merkost.mercury.model

sealed interface MercuryUiState {
    data object Loading : MercuryUiState
    data class Populated(val schema: RoomSchema) : MercuryUiState
    data object Empty : MercuryUiState
    data class Error(val message: String) : MercuryUiState
    data class Refreshing(val currentSchema: RoomSchema) : MercuryUiState
}
