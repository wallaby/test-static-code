package com.michaldrabik.ui_backup.model

import com.squareup.moshi.Json

data class BackupShows(
  @Json(name = "cH") val collectionHistory: List<BackupShow> = emptyList(),
  @Json(name = "cW") val collectionWatchlist: List<BackupShow> = emptyList(),
  @Json(name = "cHid") val collectionHidden: List<BackupShow> = emptyList(),
  @Json(name = "pEp") val progressEpisodes: List<BackupEpisode> = emptyList(),
  @Json(name = "pSe") val progressSeasons: List<BackupSeason> = emptyList(),
  @Json(name = "pP") val progressPinned: List<Long> = emptyList(),
  @Json(name = "pOH") val progressOnHold: List<Long> = emptyList(),
)

data class BackupShow(
  @Json(name = "id") val traktId: Long,
  @Json(name = "tmId") val tmdbId: Long,
  @Json(name = "t") val title: String,
  @Json(name = "a") val addedAt: String,
  @Json(name = "u") val updatedAt: String,
)

data class BackupSeason(
  @Json(name = "id") val traktId: Long,
  @Json(name = "sId") val showTraktId: Long,
  @Json(name = "sN") val seasonNumber: Int,
)

data class BackupEpisode(
  @Json(name = "id") val traktId: Long,
  @Json(name = "sId") val showTraktId: Long,
  @Json(name = "eN") val episodeNumber: Int,
  @Json(name = "sN") val seasonNumber: Int,
  @Json(name = "a") val addedAt: String?,
)
