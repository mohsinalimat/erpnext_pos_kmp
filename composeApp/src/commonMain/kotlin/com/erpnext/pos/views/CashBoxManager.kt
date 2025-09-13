package com.erpnext.pos.views

import com.erpnext.pos.localSource.dao.POSProfileDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface CashBoxState {
    object Closed : CashBoxState
    data class Opened(val posProfileName: String, val warehouse: String? = null) : CashBoxState
}

class CashBoxManager(
    private val profileDao: POSProfileDao
) {

    private val _cashboxState = MutableStateFlow<CashBoxState>(CashBoxState.Closed)
    val cashboxState = _cashboxState.asStateFlow()

    fun openCashBox(posProfileName: String) {
        _cashboxState.update { CashBoxState.Opened(posProfileName) }
    }

    fun closeCashBox() {
        _cashboxState.update { CashBoxState.Closed }
    }

    fun isCashBoxOpen(): Boolean {
        return _cashboxState.value is CashBoxState.Opened
    }

    fun load() {
        val profile = profileDao.getPOSProfile()
    }
    //TODO: Agregar metodo para traer desde la DB el estado actual
}