package com.michaldrabik.ui_progress.calendar.recycler

import androidx.annotation.StringRes
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_progress.helpers.TranslationsBundle
import java.time.format.DateTimeFormatter
import com.michaldrabik.ui_model.Episode as EpisodeModel

sealed class CalendarListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  data class Episode(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val episode: EpisodeModel,
    val season: Season,
    val isWatched: Boolean,
    val isWatchlist: Boolean,
    val isSpoilerHidden: Boolean,
    val translations: TranslationsBundle? = null,
    val dateFormat: DateTimeFormatter? = null,
    val spoilers: SpoilersSettings? = null,
  ) : CalendarListItem(show, image, isLoading) {

    override fun isSameAs(other: ListItem) = episode.ids.trakt == (other as? Episode)?.episode?.ids?.trakt
  }

  data class Header(
    @StringRes val textResId: Int,
    val calendarMode: CalendarMode,
  ) : CalendarListItem(
    show = Show.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false,
  ) {

    companion object {
      fun create(
        @StringRes textResId: Int,
        mode: CalendarMode,
      ) = Header(
        textResId = textResId,
        calendarMode = mode,
      )
    }

    override fun isSameAs(other: ListItem) = textResId == (other as? Header)?.textResId
  }

  data class Filters(
    val mode: CalendarMode,
    val premieres: Boolean,
  ) : CalendarListItem(
    show = Show.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false,
  )
}
