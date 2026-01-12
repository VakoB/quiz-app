package com.example.quizapp.ui.groups

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.data.models.Group
import com.example.quizapp.databinding.ItemGroupSectionHeaderBinding
import com.example.quizapp.databinding.ItemGroupVerticalBinding
import com.example.quizapp.databinding.ItemGroupsCarouselBinding

class GroupsMainAdapter(
    private val onGroupClick: (Group) -> Unit
) : ListAdapter<GroupListItem, RecyclerView.ViewHolder>(GroupDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CAROUSEL = 1
        private const val TYPE_JOINED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupListItem.Header -> TYPE_HEADER
            is GroupListItem.MyGroupsCarousel -> TYPE_CAROUSEL
            is GroupListItem.JoinedGroupItem -> TYPE_JOINED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ItemGroupSectionHeaderBinding.inflate(inflater, parent, false))
            TYPE_CAROUSEL -> CarouselViewHolder(ItemGroupsCarouselBinding.inflate(inflater, parent, false))
            else -> VerticalViewHolder(ItemGroupVerticalBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as GroupListItem.Header)
            is CarouselViewHolder -> holder.bind(item as GroupListItem.MyGroupsCarousel)
            is VerticalViewHolder -> holder.bind(item as GroupListItem.JoinedGroupItem)
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<GroupListItem>() {
        override fun areItemsTheSame(oldItem: GroupListItem, newItem: GroupListItem): Boolean {
            return when {
                oldItem is GroupListItem.Header && newItem is GroupListItem.Header ->
                    oldItem.title == newItem.title
                oldItem is GroupListItem.MyGroupsCarousel && newItem is GroupListItem.MyGroupsCarousel ->
                    true
                oldItem is GroupListItem.JoinedGroupItem && newItem is GroupListItem.JoinedGroupItem ->
                    oldItem.group.groupId == newItem.group.groupId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: GroupListItem, newItem: GroupListItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class CarouselViewHolder(private val binding: ItemGroupsCarouselBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupListItem.MyGroupsCarousel) = with(binding) {
            rvHorizontalCarousel.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = MyGroupsHorizontalAdapter(item.groups, onGroupClick)
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemGroupSectionHeaderBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupListItem.Header) = with(binding) {
            groupSectionTitleTv.text = item.title
        }
    }

    inner class VerticalViewHolder(private val binding: ItemGroupVerticalBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupListItem.JoinedGroupItem) = with(binding) {
            joinedGroupNameTv.text = item.group.groupName

            root.setOnClickListener { onGroupClick(item.group) }
        }
    }
}