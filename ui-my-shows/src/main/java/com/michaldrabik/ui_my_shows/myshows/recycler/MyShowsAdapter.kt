package com.michaldrabik.ui_my_shows.myshows.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type
import com.michaldrabik.ui_my_shows.myshows.views.MyShowAllView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowHeaderView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowsRecentsView

class MyShowsAdapter(
  private val itemClickListener: (ListItem) -> Unit,
  private val itemLongClickListener: (ListItem) -> Unit,
  private val onSortOrderClickListener: (MyShowsSection, SortOrder, SortType) -> Unit,
  private val onListViewModeClickListener: () -> Unit,
  private val onNetworksClickListener: () -> Unit,
  private val onGenresClickListener: () -> Unit,
  private val onTypeClickListener: () -> Unit,
  private val missingImageListener: (ListItem, Boolean) -> Unit,
  private val missingTranslationListener: (ListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<MyShowsItem>(
  listChangeListener = listChangeListener,
) {

  companion object {
    private const val VIEW_TYPE_HEADER = 1
    private const val VIEW_TYPE_SHOW_ITEM = 2
    private const val VIEW_TYPE_RECENTS_SECTION = 3
  }

  override val asyncDiffer = AsyncListDiffer(this, MyShowsItemDiffCallback())

  var listViewMode: ListViewMode = LIST_NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  fun setItems(
    newItems: List<MyShowsItem>,
    notifyChangeList: List<Type>?,
  ) {
    val notifyChange = notifyChangeList?.contains(Type.ALL_SHOWS_ITEM) == true
    super.setItems(newItems, notifyChange)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ) = when (viewType) {
    VIEW_TYPE_HEADER -> BaseViewHolder(MyShowHeaderView(parent.context))
    VIEW_TYPE_RECENTS_SECTION -> BaseViewHolder(MyShowsRecentsView(parent.context))
    VIEW_TYPE_SHOW_ITEM -> BaseViewHolder(
      when (listViewMode) {
        LIST_NORMAL -> MyShowAllView(parent.context)
      }.apply {
        itemClickListener = this@MyShowsAdapter.itemClickListener
        itemLongClickListener = this@MyShowsAdapter.itemLongClickListener
        missingImageListener = this@MyShowsAdapter.missingImageListener
        missingTranslationListener = this@MyShowsAdapter.missingTranslationListener
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
      VIEW_TYPE_HEADER -> (holder.itemView as MyShowHeaderView).bind(
        item = item.header!!,
        viewMode = listViewMode,
        typeClickListener = onTypeClickListener,
        sortClickListener = onSortOrderClickListener,
        networksClickListener = onNetworksClickListener,
        genresClickListener = onGenresClickListener,
        listModeClickListener = onListViewModeClickListener,
      )
      VIEW_TYPE_RECENTS_SECTION -> (holder.itemView as MyShowsRecentsView).bind(
        item.recentsSection!!,
        itemClickListener,
        itemLongClickListener,
      )
      VIEW_TYPE_SHOW_ITEM -> {
        when (listViewMode) {
          LIST_NORMAL -> (holder.itemView as MyShowAllView).bind(item)
        }
      }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position].type) {
      Type.ALL_SHOWS_HEADER -> VIEW_TYPE_HEADER
      Type.ALL_SHOWS_ITEM -> VIEW_TYPE_SHOW_ITEM
      Type.RECENT_SHOWS -> VIEW_TYPE_RECENTS_SECTION
    }
}
