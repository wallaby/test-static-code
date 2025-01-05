package com.michaldrabik.ui_backup.features.import_

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_backup.features.import_.workers.BackupImportWorker
import com.michaldrabik.ui_backup.model.BackupScheme
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class BackupImportViewModel @Inject constructor(
  private val backupImportWorker: BackupImportWorker,
) : ViewModel() {

  private val initialState = BackupImportUiState()

  private val loadingState = MutableStateFlow(initialState.isLoading)
  private val successState = MutableStateFlow(initialState.isSuccess)
  private val errorState = MutableStateFlow(initialState.isError)

  fun runImport(jsonInput: String) {
    if (loadingState.value) return
    viewModelScope.launch {
      try {
        loadingState.update { true }
        delay(1.seconds)
        val importScheme = createImportData(jsonInput)
        if (importScheme != null) {
          backupImportWorker.run(importScheme)
          successState.update { true }
        } else {
          errorState.update { Error("Invalid Showly backup file.") }
        }
      } catch (error: Throwable) {
        rethrowCancellation(error) {
          errorState.update { error }
        }
      } finally {
        loadingState.update { false }
      }
    }
  }

  private fun createImportData(jsonInput: String): BackupScheme? {
    val moshi = Moshi
      .Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
    val jsonAdapter = moshi.adapter(BackupScheme::class.java)
    return jsonAdapter.fromJson(jsonInput)
  }

  fun clearState() {
    loadingState.update { false }
    successState.update { false }
    errorState.update { null }
  }

  val uiState = combine(
    loadingState,
    successState,
    errorState,
  ) { s1, s2, s3 ->
    BackupImportUiState(
      isLoading = s1,
      isSuccess = s2,
      isError = s3,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = BackupImportUiState(),
  )
}
