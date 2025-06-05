package com.michaldrabik.ui_progress.calendar.cases.items

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsFiltersRepository
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_progress.calendar.helpers.WatchlistAppender
import com.michaldrabik.ui_progress.calendar.helpers.filters.CalendarRecentsFilter
import com.michaldrabik.ui_progress.calendar.helpers.groupers.CalendarRecentsGrouper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRecentsCase @Inject constructor(
  dispatchers: CoroutineDispatchers,
  localSource: LocalDataSource,
  mappers: Mappers,
  showsRepository: ShowsRepository,
  translationsRepository: TranslationsRepository,
  spoilersRepository: SettingsSpoilersRepository,
  filtersRepository: SettingsFiltersRepository,
  imagesProvider: ShowImagesProvider,
  dateFormatProvider: DateFormatProvider,
  watchlistAppender: WatchlistAppender,
  override val filter: CalendarRecentsFilter,
  override val grouper: CalendarRecentsGrouper,
) : CalendarItemsCase(
  dispatchers,
  localSource,
  mappers,
  showsRepository,
  translationsRepository,
  spoilersRepository,
  filtersRepository,
  imagesProvider,
  dateFormatProvider,
  watchlistAppender,
) {

  override fun sortEpisodes() =
    compareByDescending<Episode> { it.firstAired }
      .thenByDescending { it.idShowTrakt }
      .thenByDescending { it.episodeNumber }

  override fun isWatched(episode: Episode) = episode.isWatched

  override fun isSpoilerHidden(episode: Episode) = !episode.isWatched
}
