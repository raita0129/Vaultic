package com.raita.vaultic.domain.model

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val note: String = ""
)
