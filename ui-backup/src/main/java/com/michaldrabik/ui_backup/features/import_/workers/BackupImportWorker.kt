package com.michaldrabik.ui_backup.features.import_.workers

import com.michaldrabik.ui_backup.model.BackupScheme

interface BackupImportWorker {
  suspend fun run(backup: BackupScheme)
}
