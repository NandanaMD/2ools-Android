package com.twotools.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twotools.app.core.model.Tool
import com.twotools.app.core.model.ToolCategory
import com.twotools.app.core.registry.ToolRegistry
import com.twotools.app.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ToolCategory?>(null)
    val selectedCategory: StateFlow<ToolCategory?> = _selectedCategory.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = preferencesManager.favoriteToolsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val filteredTools: StateFlow<List<Tool>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        val searched = ToolRegistry.searchTools(query)
        if (category == null) searched else searched.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolRegistry.tools)

    val favoriteTools: StateFlow<List<Tool>> = favoriteIds.map { favSet ->
        ToolRegistry.tools.filter { favSet.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: ToolCategory?) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(toolId)
        }
    }

    fun recordToolUsed(toolId: String) {
        viewModelScope.launch {
            preferencesManager.recordToolUsage(toolId)
        }
    }
}
