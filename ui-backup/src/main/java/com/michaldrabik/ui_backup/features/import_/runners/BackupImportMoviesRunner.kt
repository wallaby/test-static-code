package com.michaldrabik.ui_backup.features.import_.runners

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.common.extensions.toUtcDateTime
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.ArchiveMovie
import com.michaldrabik.data_local.database.model.MyMovie
import com.michaldrabik.data_local.database.model.WatchlistMovie
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_backup.features.import_.model.BackupImportStatus.Importing
import com.michaldrabik.ui_backup.model.BackupMovies
import com.michaldrabik.ui_model.IdTrakt
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class BackupImportMoviesRunner @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
) : BackupImportRunner<BackupMovies>() {

  override suspend fun run(backup: BackupMovies) {
    Timber.d("Initialized.")
    runImport(backup)
      .also {
        Timber.d("Success.")
      }
  }

  private suspend fun runImport(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      importMoviesCollection(backup)
      importMoviesPinned(backup)
    }
  }

  private suspend fun importMoviesPinned(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      val localPinned = pinnedItemsRepository.getAllMovies()
      for (pinned in backup.progressPinned) {
        if (!localPinned.contains(pinned)) {
          pinnedItemsRepository.addMoviePinnedItem(IdTrakt(pinned))
        }
      }
    }
  }

  private suspend fun importMoviesCollection(backup: BackupMovies) {
    withContext(dispatchers.IO) {
      val localCollection = moviesRepository
        .loadCollection()
        .map { it.traktId }

      importMyMovies(backup, localCollection)
      importWatchlistMovies(backup, localCollection)
      importHiddenMovies(backup, localCollection)
    }
  }

  private suspend fun importMyMovies(
    backupMovies: BackupMovies,
    localCollection: List<Long>,
  ) {
    for (movie in backupMovies.collectionHistory) {
      Timber.d("Importing movie ${movie.traktId} ...")
      if (localCollection.contains(movie.traktId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.traktId)
      if (movieDetails == null) {
        fetchMovieDetails(IdTrakt(movie.traktId))
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val myMovie = MyMovie.fromTraktId(movie.traktId, timestamp)
      localSource.myMovies.insert(listOf(myMovie))

      Timber.d("Added to My Movies ${movie.traktId} ...")
    }
  }

  private suspend fun importWatchlistMovies(
    backupMovies: BackupMovies,
    localCollection: List<Long>,
  ) {
    for (movie in backupMovies.collectionWatchlist) {
      Timber.d("Importing movie ${movie.traktId} ...")
      statusListener?.invoke(Importing(movie.title))

      if (localCollection.contains(movie.traktId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.traktId)
      if (movieDetails == null) {
        fetchMovieDetails(IdTrakt(movie.traktId))
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val watchlistMovie = WatchlistMovie.fromTraktId(movie.traktId, timestamp)
      localSource.watchlistMovies.insert(watchlistMovie)

      Timber.d("Added to Watchlist ${movie.traktId} ...")
    }
  }

  private suspend fun importHiddenMovies(
    backupMovies: BackupMovies,
    localCollection: List<Long>,
  ) {
    for (movie in backupMovies.collectionHidden) {
      Timber.d("Importing movie ${movie.traktId} ...")
      statusListener?.invoke(Importing(movie.title))

      if (localCollection.contains(movie.traktId)) {
        Timber.d("Movie already in collection. Skipping.")
        continue
      }

      val movieDetails = localSource.movies.getById(movie.traktId)
      if (movieDetails == null) {
        fetchMovieDetails(IdTrakt(movie.traktId))
      }

      val timestamp = movie.addedAt.toUtcDateTime()?.toMillis() ?: nowUtcMillis()
      val hiddenMovie = ArchiveMovie.fromTraktId(movie.traktId, timestamp)
      localSource.archiveMovies.insert(hiddenMovie)

      Timber.d("Added to Hidden ${movie.traktId} ...")
    }
  }

  private suspend fun fetchMovieDetails(movieTraktId: IdTrakt) {
    Timber.d("Fetching remote movie details for $movieTraktId ...")
    moviesRepository.movieDetails.load(IdTrakt(movieTraktId.id), force = true)
//    try {
//    } catch (error: Throwable) {
//      rethrowCancellation(error) {
//        // TODO Handle Error
//      }
//    }
  }
}
