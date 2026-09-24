package com.twotools.app.features.vault

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VaultRepository(private val context: Context) {

    private val vaultFile: File by lazy {
        File(context.filesDir, "vault_entries.json")
    }

    suspend fun loadEntries(): List<VaultEntry> = withContext(Dispatchers.IO) {
        if (!vaultFile.exists()) return@withContext emptyList()
        try {
            val content = vaultFile.readText()
            val array = JSONArray(content)
            val list = mutableListOf<VaultEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VaultEntry(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", ""),
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        category = obj.optString("category", "Website"),
                        notes = obj.optString("notes", ""),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveEntries(entries: List<VaultEntry>) = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray()
            for (item in entries) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("username", item.username)
                    put("password", item.password)
                    put("category", item.category)
                    put("notes", item.notes)
                    put("updatedAt", item.updatedAt)
                }
                array.put(obj)
            }
            vaultFile.writeText(array.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
