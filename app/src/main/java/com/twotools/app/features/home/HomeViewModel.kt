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

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = preferencesManager.favoriteToolsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteTools: StateFlow<List<Tool>> = favoriteIds.map { favSet ->
        ToolRegistry.tools.filter { favSet.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTools: StateFlow<List<Tool>> = combine(
        _searchQuery,
        _selectedCategory,
        _showFavoritesOnly,
        favoriteIds
    ) { query, category, favOnly, favSet ->
        var list = ToolRegistry.searchTools(query)
        if (favOnly) {
            list = list.filter { favSet.contains(it.id) }
        } else if (category != null) {
            list = list.filter { it.category == category }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolRegistry.tools)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectAll() {
        _showFavoritesOnly.value = false
        _selectedCategory.value = null
    }

    fun selectFavorites() {
        _showFavoritesOnly.value = true
        _selectedCategory.value = null
    }

    fun onCategorySelected(category: ToolCategory?) {
        _showFavoritesOnly.value = false
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
