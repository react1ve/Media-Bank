package com.reactive.mediabank.screens.domain.util

sealed class OrderType {
    data object Ascending : OrderType()
    data object Descending : OrderType()
}
