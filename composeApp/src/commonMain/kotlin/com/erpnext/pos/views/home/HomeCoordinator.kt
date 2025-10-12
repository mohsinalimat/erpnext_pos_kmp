package com.erpnext.pos.views.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.remoteSource.dto.POSOpeningEntryDto
import org.koin.compose.koinInject

class HomeCoordinator(
    val viewModel: HomeViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun loadUserInfo() {
        return viewModel.loadUserInfo()
    }

    fun initialState() {
        viewModel.resetToInitialState()
    }

    fun loadPOSProfile() {
        return viewModel.loadPOSProfile()
    }

    fun logout() {
        return viewModel.logout()
    }

    fun onError(error: String) {
        viewModel.onError(error)
    }

    fun onPosSelected(pos: POSProfileBO) {
        viewModel.onPosSelected(pos)
    }

    fun openCashbox(entry: POSOpeningEntryDto) {
        viewModel.openCashbox(entry)
    }

    fun closeCashbox() {
        viewModel.closeCashbox()
    }

    suspend fun isCashboxOpen(): Boolean {
        return viewModel.isCashboxOpen()
    }
}

@Composable
fun rememberHomeCoordinator(): HomeCoordinator {
    val viewModel: HomeViewModel = koinInject()

    return remember(viewModel) {
        HomeCoordinator(
            viewModel = viewModel
        )
    }
}