package com.twotools.app.features.vault

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class VaultTab(val label: String) {
    VAULT("My Vault"),
    GENERATOR("Generator")
}

class VaultViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    var selectedTab by mutableStateOf(VaultTab.VAULT)
        private set

    val entries = mutableStateListOf<VaultEntry>()
    var searchQuery by mutableStateOf("")
        private set
    var selectedCategoryFilter by mutableStateOf<String?>(null)
        private set

    val visiblePasswordIds = mutableStateListOf<String>()

    var genLength by mutableFloatStateOf(16f)
        private set
    var genIncludeUpper by mutableStateOf(true)
        private set
    var genIncludeLower by mutableStateOf(true)
        private set
    var genIncludeDigits by mutableStateOf(true)
        private set
    var genIncludeSymbols by mutableStateOf(true)
        private set
    var generatedPassword by mutableStateOf("")
        private set
    var passwordStrength by mutableStateOf("Strong" to 0.75f)
        private set

    var isAddDialogOpen by mutableStateOf(false)
        private set
    var newTitle by mutableStateOf("")
    var newUsername by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var newCategory by mutableStateOf("Website")
    var newNotes by mutableStateOf("")

    init {
        loadVault()
        generateNewPassword()
    }

    fun selectTab(tab: VaultTab) {
        selectedTab = tab
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun selectCategoryFilter(category: String?) {
        selectedCategoryFilter = category
    }

    fun togglePasswordVisibility(id: String) {
        if (id in visiblePasswordIds) {
            visiblePasswordIds.remove(id)
        } else {
            visiblePasswordIds.add(id)
        }
    }

    fun loadVault() {
        viewModelScope.launch {
            val list = repository.loadEntries()
            entries.clear()
            entries.addAll(list)
        }
    }

    fun addEntry(title: String, username: String, pass: String, category: String, notes: String) {
        if (title.isBlank() || pass.isBlank()) return
        val entry = VaultEntry(
            title = title.trim(),
            username = username.trim(),
            password = pass.trim(),
            category = category,
            notes = notes.trim()
        )
        viewModelScope.launch {
            entries.add(0, entry)
            repository.saveEntries(entries)
        }
    }

    fun deleteEntry(entry: VaultEntry) {
        viewModelScope.launch {
            entries.remove(entry)
            visiblePasswordIds.remove(entry.id)
            repository.saveEntries(entries)
        }
    }

    fun openAddDialog(prefillPassword: String = "") {
        newTitle = ""
        newUsername = ""
        newPassword = prefillPassword.ifBlank { generatedPassword }
        newCategory = "Website"
        newNotes = ""
        isAddDialogOpen = true
    }

    fun closeAddDialog() {
        isAddDialogOpen = false
    }

    fun onGenLengthChange(length: Float) {
        genLength = length
        generateNewPassword()
    }

    fun toggleGenUpper() {
        genIncludeUpper = !genIncludeUpper
        generateNewPassword()
    }

    fun toggleGenLower() {
        genIncludeLower = !genIncludeLower
        generateNewPassword()
    }

    fun toggleGenDigits() {
        genIncludeDigits = !genIncludeDigits
        generateNewPassword()
    }

    fun toggleGenSymbols() {
        genIncludeSymbols = !genIncludeSymbols
        generateNewPassword()
    }

    fun generateNewPassword() {
        val pass = PasswordGeneratorEngine.generatePassword(
            length = genLength.toInt(),
            includeUpper = genIncludeUpper,
            includeLower = genIncludeLower,
            includeDigits = genIncludeDigits,
            includeSymbols = genIncludeSymbols
        )
        generatedPassword = pass
        passwordStrength = PasswordGeneratorEngine.calculateStrength(pass)
    }

    val filteredEntries: List<VaultEntry>
        get() {
            return entries.filter { entry ->
                val matchesQuery = searchQuery.isBlank() ||
                        entry.title.contains(searchQuery, ignoreCase = true) ||
                        entry.username.contains(searchQuery, ignoreCase = true) ||
                        entry.category.contains(searchQuery, ignoreCase = true)

                val matchesCat = selectedCategoryFilter == null || entry.category == selectedCategoryFilter
                matchesQuery && matchesCat
            }
        }
}
