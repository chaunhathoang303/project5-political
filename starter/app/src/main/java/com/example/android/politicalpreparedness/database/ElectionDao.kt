package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg election: Election)

    //TODO: Add select all election query
    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun getAllElection(): List<Election>

    //TODO: Add select single election query
    @Query("SELECT * FROM election_table WHERE id = :id")
    fun getElectionById(id: String): Election

    //TODO: Add delete query
    @Query("DELETE FROM election_table WHERE id = :id")
    fun deletedElectionById(id: String)

    //TODO: Add clear query
    @Query("DELETE FROM election_table")
    suspend fun clear()
}