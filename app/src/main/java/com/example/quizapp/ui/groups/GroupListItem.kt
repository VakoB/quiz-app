package com.example.quizapp.ui.groups

import com.example.quizapp.data.models.Group

sealed class GroupListItem {
    data class Header(val title: String) : GroupListItem()
    data class MyGroupsCarousel(val groups: List<Group>) : GroupListItem()
    data class JoinedGroupItem(val group: Group) : GroupListItem()
}