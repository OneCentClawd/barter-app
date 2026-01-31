package com.barter.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 全局认证事件
 */
object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: AuthEvent) {
        _events.emit(event)
    }
}

sealed class AuthEvent {
    object TokenExpired : AuthEvent()  // Token 过期或被踢下线
}
