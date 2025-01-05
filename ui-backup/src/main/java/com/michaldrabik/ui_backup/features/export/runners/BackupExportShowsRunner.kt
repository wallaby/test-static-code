package com.michaldrabik.ui_backup.features.export.runners

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.OnHoldItemsRepository
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_backup.model.BackupEpisode
import com.michaldrabik.ui_backup.model.BackupSeason
import com.michaldrabik.ui_backup.model.BackupShow
import com.michaldrabik.ui_backup.model.BackupShows
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class BackupExportShowsRunner @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val onHoldItemsRepository: OnHoldItemsRepository,
) : BackupExportRunner<BackupShows>() {

  override suspend fun run(): BackupShows {
    Timber.d("Initialized.")
    return runExport()
      .also {
        Timber.d("Success.")
      }
  }

  private suspend fun runExport(): BackupShows =
    withContext(dispatchers.IO) {
      val backupShowsCollection = exportShowsCollection()
      val backupShowsProgress = exportShowsProgress()
      val backupEpisodesProgress = exportEpisodesProgress()

      BackupShows(
        collectionHistory = backupShowsCollection.collectionHistory,
        collectionWatchlist = backupShowsCollection.collectionWatchlist,
        collectionHidden = backupShowsCollection.collectionHidden,
        progressSeasons = backupEpisodesProgress.progressSeasons,
        progressEpisodes = backupEpisodesProgress.progressEpisodes,
        progressPinned = backupShowsProgress.progressPinned,
        progressOnHold = backupShowsProgress.progressOnHold,
      )
    }

  private suspend fun exportShowsCollection(): BackupShows =
    withContext(dispatchers.IO) {
      val myShowsAsync = async { localSource.myShows.getAll() }
      val watchlistShowsAsync = async { localSource.watchlistShows.getAll() }
      val hiddenShowsAsync = async { localSource.archiveShows.getAll() }

      val myShows = myShowsAsync.await()
      val watchlistShows = watchlistShowsAsync.await()
      val hiddenShows = hiddenShowsAsync.await()

      val collectionMyShows = myShows.map {
        BackupShow(
          traktId = it.idTrakt,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }
      val collectionWatchlist = watchlistShows.map {
        BackupShow(
          traktId = it.idTrakt,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }
      val collectionHidden = hiddenShows.map {
        BackupShow(
          traktId = it.idTrakt,
          tmdbId = it.idTmdb,
          title = it.title,
          addedAt = dateIsoStringFromMillis(it.createdAt),
          updatedAt = dateIsoStringFromMillis(it.updatedAt),
        )
      }

      BackupShows(
        collectionHistory = collectionMyShows,
        collectionWatchlist = collectionWatchlist,
        collectionHidden = collectionHidden,
      )
    }

  private suspend fun exportEpisodesProgress(): BackupShows =
    withContext(dispatchers.IO) {
      val watchedEpisodesAsync = async { localSource.episodes.getAllWatched() }
      val watchedSeasonsAsync = async { localSource.seasons.getAllWatched() }

      val watchedEpisodes = watchedEpisodesAsync.await()
      val watchedSeasons = watchedSeasonsAsync.await()

      val progressSeasons = watchedSeasons.map { season ->
        BackupSeason(
          traktId = season.idTrakt,
          showTraktId = season.idShowTrakt,
          seasonNumber = season.seasonNumber,
        )
      }

      val progressEpisodes = watchedEpisodes.map { episode ->
        BackupEpisode(
          traktId = episode.idTrakt,
          showTraktId = episode.idShowTrakt,
          episodeNumber = episode.episodeNumber,
          seasonNumber = episode.seasonNumber,
          addedAt = episode.lastWatchedAt?.let { dateIsoStringFromMillis(it.toMillis()) },
        )
      }

      BackupShows(
        progressSeasons = progressSeasons,
        progressEpisodes = progressEpisodes,
      )
    }

  private suspend fun exportShowsProgress(): BackupShows =
    withContext(dispatchers.IO) {
      val pinnedIds = pinnedItemsRepository.getAllShows()
      val onHoldIds = onHoldItemsRepository.getAll().map { it.id }
      BackupShows(
        progressPinned = pinnedIds,
        progressOnHold = onHoldIds,
      )
    }
}
