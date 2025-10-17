package com.erpnext.pos.views.customer

import com.erpnext.pos.domain.models.CustomerBO

sealed class CustomerState {
    object Loading : CustomerState()
    object Empty : CustomerState()
    data class Success(
        val customers: List<CustomerBO>,
        val territories: List<String?> = emptyList()
    ) : CustomerState()

    data class Error(val message: String) : CustomerState()
}

data class CustomerAction(
    val onSearchQueryChanged: (String) -> Unit = {},
    val onTerritorySelected: (String?) -> Unit = {},
    val onRefresh: () -> Unit = {},
    val checkCredit: (String, Double, (Boolean, String) -> Unit) -> Unit = { _, _, _ ->  },
    val onClearSearch: () -> Unit = {},
    val onError: (error: String) -> Unit = {},
    val fetchAll: () -> Unit = {},
    val toDetails: (String) -> Unit = {}
)