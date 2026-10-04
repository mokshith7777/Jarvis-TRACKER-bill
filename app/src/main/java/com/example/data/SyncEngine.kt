package com.example.data

import com.example.data.dao.SyncQueueDao
import com.example.data.dao.TransactionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NetworkMode {
  ONLINE,
  OFFLINE,
  CLOUD_OUTAGE
}

enum class SyncStatus {
  SYNCED,
  SYNCING,
  PENDING,
  OFFLINE,
  FAILED
}

data class SyncEngineState(
  val status: SyncStatus = SyncStatus.SYNCED,
  val networkMode: NetworkMode = NetworkMode.ONLINE,
  val pendingCount: Int = 0,
  val totalSyncedCount: Int = 0,
  val lastSyncTimestamp: Long = System.currentTimeMillis(),
  val lastMessage: String = "JARVIS Cloud Replication Nominal"
)

class SyncEngine(
  private val syncQueueDao: SyncQueueDao,
  private val transactionDao: TransactionDao,
  private val scope: CoroutineScope
) {
  private val _state = MutableStateFlow(SyncEngineState())
  val state: StateFlow<SyncEngineState> = _state.asStateFlow()

  private var syncLoopJob: Job? = null

  init {
    startSyncLoop()
  }

  fun setNetworkMode(mode: NetworkMode) {
    _state.update {
      it.copy(
        networkMode = mode,
        status = when (mode) {
          NetworkMode.OFFLINE -> SyncStatus.OFFLINE
          NetworkMode.CLOUD_OUTAGE -> SyncStatus.FAILED
          NetworkMode.ONLINE -> if (it.pendingCount > 0) SyncStatus.PENDING else SyncStatus.SYNCED
        },
        lastMessage = when (mode) {
          NetworkMode.OFFLINE -> "Offline Mode: Mutations safely held in local Room DB"
          NetworkMode.CLOUD_OUTAGE -> "Cloud Outage Simulated: Backoff retry active"
          NetworkMode.ONLINE -> "Online: Reconnected to Stark Cloud"
        }
      )
    }
    if (mode == NetworkMode.ONLINE) {
      triggerSync()
    }
  }

  fun triggerSync() {
    scope.launch(Dispatchers.IO) {
      processQueue()
    }
  }

  fun resetFailedOperations() {
    scope.launch(Dispatchers.IO) {
      syncQueueDao.resetFailed()
      triggerSync()
    }
  }

  private fun startSyncLoop() {
    syncLoopJob?.cancel()
    syncLoopJob = scope.launch(Dispatchers.IO) {
      while (isActive) {
        val pending = syncQueueDao.getNextBatch(limit = 50)
        _state.update { it.copy(pendingCount = pending.size) }

        if (pending.isNotEmpty() && _state.value.networkMode == NetworkMode.ONLINE) {
          processQueue()
        }
        delay(4000)
      }
    }
  }

  private suspend fun processQueue() = withContext(Dispatchers.IO) {
    if (_state.value.networkMode != NetworkMode.ONLINE) {
      return@withContext
    }

    val batch = syncQueueDao.getNextBatch(limit = 20)
    if (batch.isEmpty()) {
      _state.update {
        it.copy(
          status = SyncStatus.SYNCED,
          pendingCount = 0,
          lastSyncTimestamp = System.currentTimeMillis(),
          lastMessage = "All records synchronized with Stark Cloud"
        )
      }
      return@withContext
    }

    _state.update {
      it.copy(
        status = SyncStatus.SYNCING,
        lastMessage = "Replicating ${batch.size} operations to Cloud..."
      )
    }

    // Simulate network latency (250ms per batch for smooth realistic HUD feedback)
    delay(400)

    for (op in batch) {
      if (_state.value.networkMode == NetworkMode.CLOUD_OUTAGE) {
        val nextRetry = System.currentTimeMillis() + (Math.pow(2.0, op.retryCount.toDouble()).toLong() * 1000L).coerceAtMost(30000L)
        syncQueueDao.update(
          op.copy(
            status = "FAILED",
            retryCount = op.retryCount + 1,
            nextRetryAt = nextRetry,
            lastError = "503 Stark Cloud Gateway Timeout"
          )
        )
        if (op.entityType == "TRANSACTION") {
          transactionDao.updateSyncStatus(op.entityId, "FAILED")
        }
        _state.update {
          it.copy(
            status = SyncStatus.FAILED,
            lastMessage = "Cloud outage encountered. Exponential backoff active."
          )
        }
        continue
      }

      // Successful cloud commit simulation
      syncQueueDao.delete(op.operationId)
      if (op.entityType == "TRANSACTION") {
        transactionDao.updateSyncStatus(op.entityId, "SYNCED")
      }
      _state.update {
        it.copy(
          totalSyncedCount = it.totalSyncedCount + 1
        )
      }
    }

    val remaining = syncQueueDao.getNextBatch(limit = 50)
    _state.update {
      it.copy(
        status = if (remaining.isEmpty()) SyncStatus.SYNCED else SyncStatus.PENDING,
        pendingCount = remaining.size,
        lastSyncTimestamp = System.currentTimeMillis(),
        lastMessage = if (remaining.isEmpty()) "JARVIS Cloud Synchronization Complete" else "${remaining.size} items pending"
      )
    }
  }
}
