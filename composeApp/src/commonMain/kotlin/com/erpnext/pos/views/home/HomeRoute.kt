package com.erpnext.pos.views.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    coordinator: HomeCoordinator = rememberHomeCoordinator()
) {
    val uiState by coordinator.screenStateFlow.collectAsState(HomeState.Loading)
    val actions = rememberHomeActions(coordinator)

    HomeScreen(uiState, actions)
}

@Composable
fun rememberHomeActions(coordinator: HomeCoordinator) : HomeAction {
    return remember(coordinator) {
        HomeAction(
            loadUserInfo = coordinator::loadUserInfo,
            loadPOSProfile = coordinator::loadPOSProfile,
            onLogout = coordinator::logout,
            onError = coordinator::onError,
            openCashbox = coordinator::onPosSelected
        )
    }
}