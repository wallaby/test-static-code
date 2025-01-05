package com.michaldrabik.ui_backup.features.import_

data class BackupImportUiState(
  val isLoading: Boolean = false,
  val isSuccess: Boolean = false,
  val isError: Throwable? = null,
)
