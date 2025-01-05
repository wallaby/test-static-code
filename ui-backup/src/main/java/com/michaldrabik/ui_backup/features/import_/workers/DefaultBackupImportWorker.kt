package com.michaldrabik.ui_backup.features.import_.workers

import com.michaldrabik.ui_backup.features.import_.runners.BackupImportListsRunner
import com.michaldrabik.ui_backup.features.import_.runners.BackupImportMoviesRunner
import com.michaldrabik.ui_backup.features.import_.runners.BackupImportShowsRunner
import com.michaldrabik.ui_backup.model.BackupScheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultBackupImportWorker @Inject constructor(
  private val importShowsRunner: BackupImportShowsRunner,
  private val importMoviesRunner: BackupImportMoviesRunner,
  private val importListsRunner: BackupImportListsRunner,
) : BackupImportWorker {

  override suspend fun run(backup: BackupScheme) {
    coroutineScope {
      val importShowsAsync = async { importShowsRunner.run(backup.shows) }
      val importMoviesAsync = async { importMoviesRunner.run(backup.movies) }

      awaitAll(importShowsAsync, importMoviesAsync)
      importListsRunner.run(backup.lists)
    }
  }
}
