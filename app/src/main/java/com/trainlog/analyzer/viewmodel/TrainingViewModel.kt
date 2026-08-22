package com.trainlog.analyzer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trainlog.analyzer.data.db.AppDatabase
import com.trainlog.analyzer.data.model.TrainingRun
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).trainingRunDao()

    val allRuns = dao.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveRun(run: TrainingRun, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = dao.insert(run)
            onSaved(id)
        }
    }

    fun updateRun(run: TrainingRun) {
        viewModelScope.launch {
            dao.update(run)
        }
    }

    fun deleteRun(run: TrainingRun) {
        viewModelScope.launch {
            dao.delete(run)
        }
    }

    suspend fun getRun(id: Long): TrainingRun? = dao.getRunById(id)
}
