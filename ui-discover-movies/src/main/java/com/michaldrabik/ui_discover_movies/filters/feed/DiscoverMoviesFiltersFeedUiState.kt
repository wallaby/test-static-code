package com.michaldrabik.ui_discover_movies.filters.feed

import com.michaldrabik.ui_model.DiscoverFeed

internal data class DiscoverMoviesFiltersFeedUiState(
  val feedOrder: DiscoverFeed? = null,
  val isLoading: Boolean? = null,
)
