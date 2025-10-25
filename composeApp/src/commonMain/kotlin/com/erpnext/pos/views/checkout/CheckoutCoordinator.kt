package com.erpnext.pos.views.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.viewmodel.koinViewModel

class CheckoutCoordinator(val viewModel: CheckoutViewModel) {

    val screenStateFlow = viewModel.state

    fun fetchAllCustomers() {}

    fun fetchAllProducts() {}

    fun checkCredit() {}

    fun loadPosProfile() {}


}

@Composable
fun rememberCheckoutCoordinator(): CheckoutCoordinator {
    val viewModel: CheckoutViewModel = koinViewModel()

    return remember(viewModel) {
        CheckoutCoordinator(
            viewModel = viewModel
        )
    }
}