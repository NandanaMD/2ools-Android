package com.twotools.app.features.vault

data class VaultEntry(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val username: String = "",
    val password: String,
    val category: String = "Website",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

enum class VaultCategory(val label: String) {
    WEBSITE("Website"),
    APP("App"),
    EMAIL("Email"),
    WIFI("Wi-Fi"),
    FINANCE("Banking / Card"),
    NOTE("Secret Note")
}
