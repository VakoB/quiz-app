package com.example.quizapp.ui.groups.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizapp.data.groups.GroupsRepository
import com.example.quizapp.data.models.Group
import com.example.quizapp.data.models.GroupMember
import com.example.quizapp.ui.groups.GroupListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener


class GroupsViewModel(
    private val repository: GroupsRepository = GroupsRepository()
) : ViewModel() {
    private val _items = MutableLiveData<List<GroupListItem>>()
    val items: LiveData<List<GroupListItem>> = _items
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val _navigateToGroupDetails = MutableLiveData<String?>()
    val navigateToGroupDetails: LiveData<String?> = _navigateToGroupDetails

    private val _isOwner = MutableLiveData<Boolean>()
    val isOwner: LiveData<Boolean> = _isOwner

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> = _group

    private val _members = MutableLiveData<List<GroupMember>>()
    val members: LiveData<List<GroupMember>> = _members

    private val _isGameActive = MutableLiveData<Boolean>()
    val isGameActive: LiveData<Boolean> = _isGameActive

    private var gameExistsListener: ValueEventListener? = null



    fun listenGameExists(groupId: String) {
        gameExistsListener?.let {
            repository.removeGameExistsListener(groupId, it)
        }

        gameExistsListener = repository.listenGameExists(groupId) { exists ->
            _isGameActive.postValue(exists)
        }
    }

    fun loadGroups() {
        _loading.value = true

        repository.getOwnedGroups { owned ->
            repository.getJoinedGroups { joined ->
                val uiItems = mutableListOf<GroupListItem>()

                if (owned.isNotEmpty()) {
                    uiItems.add(
                        GroupListItem.Header("Groups You Manage")
                    )
                    uiItems.add(
                        GroupListItem.MyGroupsCarousel(owned)
                    )
                }

                if (joined.isNotEmpty()) {
                    uiItems.add(
                        GroupListItem.Header("Joined Communities")
                    )
                    uiItems.addAll(
                        joined.map {
                            GroupListItem.JoinedGroupItem(it)
                        }
                    )
                }

                _items.postValue(uiItems)
                _loading.postValue(false)
            }
        }
    }

    fun loadGroup(groupId: String) {
        _loading.value = true

        if (groupId.isBlank()) {
            return
        }

        repository.getGroup(groupId) { grp ->
            if (grp != null) {
                _group.postValue(grp)

                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                _isOwner.postValue(grp.ownerId == currentUid)

                repository.getGroupMembers(groupId) { memList ->
                    _members.postValue(memList)
                    _loading.postValue(false)
                }
            } else {
                _error.postValue("Failed to load group")
                _loading.postValue(false)
            }
        }
    }

    fun createGroup(groupName: String) {
        if (groupName.isBlank()) {
            _error.value = "Group name cannot be empty"
            return
        }

        _loading.value = true

        repository.createGroup(groupName) { result ->
            result
                .onSuccess { groupId ->
                    loadGroups()
                    _navigateToGroupDetails.value = groupId
                }
                .onFailure { throwable ->
                    _error.value = throwable.message ?: "Failed to create group"
                    _loading.value = false
                }
        }
    }

    fun joinGroup(code: String) {
        if (code.isBlank()) {
            _error.value = "Invalid group code"
            return
        }

        _loading.value = true

        repository.joinGroup(code) { result ->
            result
                .onSuccess { groupId ->
                    loadGroups()
                    _loading.postValue(false)
                    _navigateToGroupDetails.value = groupId
                }
                .onFailure {
                    _error.value = it.message
                    _loading.value = false
                }
        }
    }


    fun clearError() {
        _error.value = null
    }

    fun onNavigationHandled() {
        _navigateToGroupDetails.value = null
    }

}
