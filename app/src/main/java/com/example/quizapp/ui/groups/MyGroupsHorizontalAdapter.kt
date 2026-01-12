package com.example.quizapp.ui.groups

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.data.models.Group
import com.example.quizapp.databinding.ItemMyGroupCardBinding

class MyGroupsHorizontalAdapter(
    private val groups: List<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<MyGroupsHorizontalAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemMyGroupCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) = with(binding) {
            myGroupTitleTv.text = group.groupName
            root.setOnClickListener { onClick(group) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyGroupCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(groups[position])
    override fun getItemCount() = groups.size
}