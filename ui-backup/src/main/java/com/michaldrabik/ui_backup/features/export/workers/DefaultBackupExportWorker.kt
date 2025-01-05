package com.michaldrabik.ui_backup.features.export.workers

import com.michaldrabik.ui_backup.BackupConfig.SCHEME_VERSION
import com.michaldrabik.ui_backup.features.export.runners.BackupExportListsRunner
import com.michaldrabik.ui_backup.features.export.runners.BackupExportMoviesRunner
import com.michaldrabik.ui_backup.features.export.runners.BackupExportShowsRunner
import com.michaldrabik.ui_backup.model.BackupScheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultBackupExportWorker @Inject constructor(
  private val exportShowsRunner: BackupExportShowsRunner,
  private val exportMoviesRunner: BackupExportMoviesRunner,
  private val exportListsRunner: BackupExportListsRunner,
) : BackupExportWorker {

  override suspend fun run(): BackupScheme {
    val exportShows = exportShowsRunner.run()
    val exportMovies = exportMoviesRunner.run()
    val exportLists = exportListsRunner.run()

    return BackupScheme(
      version = SCHEME_VERSION,
      shows = exportShows,
      movies = exportMovies,
      lists = exportLists,
    )
  }
}
