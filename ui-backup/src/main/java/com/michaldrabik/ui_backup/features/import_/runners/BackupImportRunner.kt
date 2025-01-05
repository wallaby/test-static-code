package com.michaldrabik.ui_backup.features.import_.runners

internal abstract class BackupImportRunner<T> {
  abstract suspend fun run(backup: T)
}
