package com.michaldrabik.ui_discover.filters.feed

import com.michaldrabik.ui_model.DiscoverFeed

internal data class DiscoverFiltersFeedUiState(
  val feedOrder: DiscoverFeed? = null,
  val isLoading: Boolean? = null,
)
