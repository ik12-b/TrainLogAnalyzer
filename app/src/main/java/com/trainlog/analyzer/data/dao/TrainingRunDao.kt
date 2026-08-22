package com.trainlog.analyzer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.trainlog.analyzer.data.model.TrainingRun
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingRunDao {
    @Query("SELECT * FROM training_runs ORDER BY createdAt DESC")
    fun getAllRuns(): Flow<List<TrainingRun>>

    @Query("SELECT * FROM training_runs WHERE id = :id")
    suspend fun getRunById(id: Long): TrainingRun?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: TrainingRun): Long

    @Update
    suspend fun update(run: TrainingRun)

    @Delete
    suspend fun delete(run: TrainingRun)
}
