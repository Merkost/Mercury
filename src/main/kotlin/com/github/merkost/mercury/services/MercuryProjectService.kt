package com.github.merkost.mercury.services

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.Alarm
import com.github.merkost.mercury.listener.RoomPsiListener
import com.github.merkost.mercury.model.MercuryUiState
import com.github.merkost.mercury.model.RoomSchema
import com.github.merkost.mercury.parser.SchemaResolver
import java.util.concurrent.Callable

@Service(Service.Level.PROJECT)
class MercuryProjectService(private val project: Project) : Disposable {

    private val log = Logger.getInstance(MercuryProjectService::class.java)
    private val schemaResolver = SchemaResolver(project)
    private val _uiState = mutableStateOf<MercuryUiState>(MercuryUiState.Loading)
    private var previousSchema = RoomSchema(databases = emptyList())
    private var hasEverFoundDatabases = false
    private val retryAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private var retryCount = 0

    val uiState: State<MercuryUiState> get() = _uiState

    init {
        RoomPsiListener(project, this).register(this)
    }

    fun refreshSchema() {
        val current = _uiState.value
        if (current is MercuryUiState.Populated) {
            _uiState.value = MercuryUiState.Refreshing(current.schema)
        } else if (current is MercuryUiState.Refreshing) {
            return
        } else {
            _uiState.value = MercuryUiState.Loading
        }

        ReadAction.nonBlocking(Callable {
            schemaResolver.resolve()
        })
            .inSmartMode(project)
            .coalesceBy(this)
            .expireWith(this)
            .finishOnUiThread(ModalityState.defaultModalityState()) { newSchema ->
                if (newSchema != previousSchema) {
                    previousSchema = newSchema
                    if (newSchema.databases.isNotEmpty()) hasEverFoundDatabases = true
                    _uiState.value = when {
                        newSchema.databases.isEmpty() && !hasEverFoundDatabases -> {
                            scheduleRetry()
                            MercuryUiState.Loading
                        }
                        newSchema.databases.isEmpty() -> MercuryUiState.Empty
                        else -> {
                            retryCount = 0
                            MercuryUiState.Populated(newSchema)
                        }
                    }
                    log.info("Mercury: schema refreshed — ${newSchema.databases.size} database(s)")
                } else {
                    val currentState = _uiState.value
                    if (currentState is MercuryUiState.Refreshing) {
                        _uiState.value = MercuryUiState.Populated(currentState.currentSchema)
                    } else if (currentState is MercuryUiState.Loading) {
                        _uiState.value = if (previousSchema.databases.isEmpty()) {
                            MercuryUiState.Empty
                        } else {
                            MercuryUiState.Populated(previousSchema)
                        }
                    }
                }
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private fun scheduleRetry() {
        if (retryCount >= 10) return
        retryCount++
        retryAlarm.cancelAllRequests()
        retryAlarm.addRequest({ refreshSchema() }, 3000L)
    }

    override fun dispose() {}
}
