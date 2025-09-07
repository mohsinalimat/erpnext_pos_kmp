package com.erpnext.pos.views.inventory

import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.ItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InventoryViewModel(
    private val navManager: NavigationManager,
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow = _stateFlow.asStateFlow()

    fun fetchAllItems(): List<ItemDto> {
        return emptyList()
    }

    fun getItemDetail(itemId: String): ItemDto? {
        return null
    }

    fun refresh() {}

    fun onError(message: String) {}
}