package com.erpnext.pos.views

import androidx.compose.foundation.layout.FlowRow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erpnext.pos.domain.models.BalanceDetailsBO
import com.erpnext.pos.localSource.dao.CashboxDao
import com.erpnext.pos.localSource.dao.POSProfileDao
import com.erpnext.pos.localSource.entities.BalanceDetailsEntity
import com.erpnext.pos.localSource.entities.CashboxEntity
import com.erpnext.pos.localSource.entities.POSProfileEntity
import com.erpnext.pos.remoteSource.dto.BalanceDetailsDto
import com.erpnext.pos.remoteSource.dto.POSOpeningEntryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.koin.core.option.viewModelScopeFactory

sealed interface CashBoxState {
    object Closed : CashBoxState
    data class Opened(val posProfileName: String, val warehouse: String? = null) : CashBoxState
}

//TODO: Incluir el sharedpref para poder manejar ahi el cashboxId o cargar al iniciar app
// Y dejarlo global
class CashBoxManager(
    private val profileDao: POSProfileDao,
    private val cashboxDao: CashboxDao
) {

    private val _cashboxState = MutableStateFlow<CashBoxState>(CashBoxState.Closed)
    val cashboxState = _cashboxState.asStateFlow()

    suspend fun openCashBox(entry: POSOpeningEntryDto) {
        val cashbox = CashboxEntity(
            0,
            entry.posProfile,
            entry.company,
            entry.periodStartDate,
            entry.user,
            true,
            true
        )
        val details =
            entry.balanceDetails.map {
                BalanceDetailsEntity(
                    0,
                    0,
                    it.modeOfPayment,
                    it.openingAmount
                )
            }
        cashboxDao.insert(cashbox, details)
        profileDao.updateProfileState(entry.posProfile, true)
        _cashboxState.update { CashBoxState.Opened(entry.posProfile) }

        // Intentar Sync
        /*try {
            val dto = entry.toDto(details)  // Convierte a DTO
            val erpName = repo.createPOSOpeningEntry(dto)
            dao.markAsSynced(entry.localId, erpName)
        } catch (e: Exception) {
            // Maneja offline: Enqueue WorkManager para retry
        }*/
    }

    suspend fun closeCashBox() { //closeEntry: POSClosingEntryDto) {
        /*_cashboxState.update { CashBoxState.Closed }
        val entity = cashboxDao.get(cashboxId).first()
        cashboxDao.updateState(entity)*/
    }

    suspend fun isCashBoxOpen(): Boolean {
        val profile = profileDao.getActiveProfile()
        if (profile != null) {
            _cashboxState.update { CashBoxState.Opened(profile.profileName, profile.warehouse) }
        } else {
            _cashboxState.update { CashBoxState.Closed }
        }

        return _cashboxState.value is CashBoxState.Opened
    }
}