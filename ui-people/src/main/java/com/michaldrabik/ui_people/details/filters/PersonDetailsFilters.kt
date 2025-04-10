package com.michaldrabik.ui_people.details.filters

import com.michaldrabik.common.Mode

data class PersonDetailsFilters(
  val modes: List<Mode> = emptyList(),
  val onlyCollection: Boolean = false,
)
