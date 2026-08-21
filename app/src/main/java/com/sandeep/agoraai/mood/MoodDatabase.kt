package com.sandeep.agoraai.mood

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MoodStorage(context: Context) {
    private val prefs = context.getSharedPreferences("mood_journal", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val entriesKey = "entries"

    fun saveMoodEntry(entry: MoodEntry) {
        val entries = getAllEntries().toMutableList()
        // If an entry for this date exists, replace it, otherwise add
        val existingIndex = entries.indexOfFirst { it.date == entry.date }
        if (existingIndex != -1) {
            entries[existingIndex] = entry.copy(id = entries[existingIndex].id)
        } else {
            val newId = if (entries.isEmpty()) 1L else entries.maxOf { it.id } + 1
            entries.add(entry.copy(id = newId))
        }
        
        val json = gson.toJson(entries)
        prefs.edit().putString(entriesKey, json).apply()
    }

    fun getAllEntries(): List<MoodEntry> {
        val json = prefs.getString(entriesKey, null) ?: return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getLast7DaysEntries(): List<MoodEntry> {
        val entries = getAllEntries()
        if (entries.isEmpty()) return emptyList()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(7)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            entries.filter { 
                try {
                    val entryDate = LocalDate.parse(it.date, formatter)
                    !entryDate.isBefore(sevenDaysAgo) && !entryDate.isAfter(today)
                } catch (e: Exception) {
                    false
                }
            }
        } else {
            // Fallback for older APIs: sort by date string (since it's YYYY-MM-DD) and take last 7
            entries.sortedByDescending { it.date }.take(7)
        }
    }

    fun getLatestEntry(): MoodEntry? {
        val entries = getAllEntries()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            entries.maxByOrNull { 
                try {
                    LocalDate.parse(it.date, formatter)
                } catch (e: Exception) {
                    LocalDate.MIN
                }
            }
        } else {
            entries.maxByOrNull { it.date }
        }
    }

    fun getTodayEntry(): MoodEntry? {
        val todayStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            // Simple fallback for today if needed (simplified since older API isn't primary target)
            val cal = java.util.Calendar.getInstance()
            String.format("%04d-%02d-%02d", 
                cal.get(java.util.Calendar.YEAR), 
                cal.get(java.util.Calendar.MONTH) + 1, 
                cal.get(java.util.Calendar.DAY_OF_MONTH))
        }
        return getAllEntries().find { it.date == todayStr }
    }
}
