package com.michaldrabik.ui_my_movies.mymovies.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieHeaderView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMoviesRecentsView

class MyMoviesAdapter(
  private val itemClickListener: (MyMoviesItem) -> Unit,
  private val itemLongClickListener: (MyMoviesItem) -> Unit,
  private val missingImageListener: (MyMoviesItem, Boolean) -> Unit,
  private val missingTranslationListener: (MyMoviesItem) -> Unit,
  private val onSortOrderClickListener: (SortOrder, SortType) -> Unit,
  private val onGenresClickListener: () -> Unit,
  private val onListViewModeClickListener: () -> Unit,
  listChangeListener: (() -> Unit),
) : BaseMovieAdapter<MyMoviesItem>(
  listChangeListener = listChangeListener,
) {

  companion object {
    private const val VIEW_TYPE_HEADER = 1
    private const val VIEW_TYPE_MOVIE_ITEM = 2
    private const val VIEW_TYPE_RECENTS_SECTION = 3
  }

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, MyMoviesItemDiffCallback())

  var listViewMode: ListViewMode = LIST_NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ) = when (viewType) {
    VIEW_TYPE_HEADER -> BaseViewHolder(MyMovieHeaderView(parent.context))
    VIEW_TYPE_RECENTS_SECTION -> BaseViewHolder(MyMoviesRecentsView(parent.context))
    VIEW_TYPE_MOVIE_ITEM -> BaseAdapter.BaseViewHolder(
      when (listViewMode) {
        LIST_NORMAL -> MyMovieAllView(parent.context)
      }.apply {
        itemClickListener = this@MyMoviesAdapter.itemClickListener
        itemLongClickListener = this@MyMoviesAdapter.itemLongClickListener
        missingImageListener = this@MyMoviesAdapter.missingImageListener
        missingTranslationListener = this@MyMoviesAdapter.missingTranslationListener
      },
    )
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
  ) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as MyMovieHeaderView).bind(
        item.header!!,
        listViewMode,
        onSortOrderClickListener,
        onGenresClickListener,
        onListViewModeClickListener,
      )
      VIEW_TYPE_RECENTS_SECTION -> (holder.itemView as MyMoviesRecentsView).bind(
        item.recentsSection!!,
        itemClickListener,
        itemLongClickListener,
      )
      VIEW_TYPE_MOVIE_ITEM -> when (listViewMode) {
        LIST_NORMAL -> (holder.itemView as MyMovieAllView).bind(item)
      }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position].type) {
      Type.HEADER -> VIEW_TYPE_HEADER
      Type.ALL_MOVIES_ITEM -> VIEW_TYPE_MOVIE_ITEM
      Type.RECENT_MOVIES -> VIEW_TYPE_RECENTS_SECTION
    }
}
