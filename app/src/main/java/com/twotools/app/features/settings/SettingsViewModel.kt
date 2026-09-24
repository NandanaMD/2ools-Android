package com.twotools.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.storage.StorageManager
import com.twotools.app.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val storageManager: StorageManager
) : ViewModel() {

    val hapticFeedbackEnabled: StateFlow<Boolean> = preferencesManager.hapticFeedbackFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    init {
        refreshCacheSize()
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticFeedback(enabled)
        }
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            val bytes = storageManager.getCacheSizeBytes()
            _cacheSize.value = StorageManager.formatFileSize(bytes)
        }
    }

    fun clearCache(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isClearingCache.value = true
            storageManager.clearAllCache()
            val newBytes = storageManager.getCacheSizeBytes()
            _cacheSize.value = StorageManager.formatFileSize(newBytes)
            _isClearingCache.value = false
            onComplete()
        }
    }
}
