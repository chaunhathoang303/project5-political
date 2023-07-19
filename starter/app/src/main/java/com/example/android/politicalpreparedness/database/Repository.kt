package com.example.android.politicalpreparedness.database

import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(
    private val dao: ElectionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insertElection(election: Election) {
        withContext(ioDispatcher) {
            dao.insert(election)
        }
    }

    suspend fun getAllElection(): Result<List<Election>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(dao.getAllElection())
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    suspend fun getElectionById(id: String): Result<Election> = withContext(ioDispatcher) {
        try {
            val election = dao.getElectionById(id)
            if (election != null) {
                return@withContext Result.Success(election)
            } else {
                return@withContext Result.Error("Election not found!")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e.localizedMessage)
        }
    }

    suspend fun deleteElectionById(id: String) {
        withContext(ioDispatcher) {
            dao.deletedElectionById(id)
        }
    }

    suspend fun deleteAllElection() {
        withContext(ioDispatcher) {
            dao.clear()
        }
    }
}