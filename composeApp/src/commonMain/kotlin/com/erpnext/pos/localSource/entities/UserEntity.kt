package com.erpnext.pos.localSource.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabUser")
data class UserEntity(
    val name: String,
    val firstName: String,
    val lastName: String?,
    val username: String?,
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val language: String?,
    val enabled: Boolean
)