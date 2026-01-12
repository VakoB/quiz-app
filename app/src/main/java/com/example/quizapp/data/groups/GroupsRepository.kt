package com.example.quizapp.data.groups

import com.example.quizapp.data.auth.UserRepository
import com.example.quizapp.data.models.Group
import com.example.quizapp.data.models.GroupMember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Collections.emptyList
import java.util.Date

class GroupsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val groupsRef = firestore.collection("groups")

    fun getOwnedGroups(callback: (List<Group>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        groupsRef
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.toObjects(Group::class.java)
                callback(groups)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getJoinedGroups(callback: (List<Group>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { userDoc ->

                val groupIds = (userDoc.get("joinedGroups") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

                if (groupIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                groupsRef
                    .whereIn(FieldPath.documentId(), groupIds.take(10))
                    .get()
                    .addOnSuccessListener { snapshot ->
                        callback(snapshot.toObjects(Group::class.java))
                    }
                    .addOnFailureListener {
                        callback(emptyList())
                    }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun createGroup(
        groupName: String,
        onComplete: (Result<String>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
            ?: return onComplete(Result.failure(Exception("Not logged in")))

        val userRepository = UserRepository()
        userRepository.getUser(uid) { user ->
            if (user == null) {
                return@getUser onComplete(Result.failure(Exception("User not found")))
            }

            val groupRef = groupsRef.document()
            val groupId = groupRef.id
            val groupCode = generateGroupCode()

            val group = Group(
                groupId = groupId,
                groupName = groupName,
                ownerId = uid,
                groupCode = groupCode,
                createdAt = Date()
            )

            val ownerMember = GroupMember(
                uid = uid,
                firstName = user.firstName,
                lastName = user.lastName,
                role = "owner",
                currentScore = 0
            )

            firestore.runBatch { batch ->
                batch.set(groupRef, group)
                batch.set(groupRef.collection("members").document(uid), ownerMember)
            }.addOnSuccessListener {
                onComplete(Result.success(groupId))
            }.addOnFailureListener {
                onComplete(Result.failure(it))
            }
        }
    }


    fun joinGroup(
        groupCode: String,
        callback: (Result<String>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
            ?: return callback(Result.failure(Exception("Not logged in")))

        val userRepository = UserRepository()
        userRepository.getUser(uid) { user ->
            if (user == null) {
                return@getUser callback(Result.failure(Exception("User not found")))
            }

            groupsRef
                .whereEqualTo("groupCode", groupCode)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        callback(Result.failure(Exception("Group not found")))
                        return@addOnSuccessListener
                    }

                    val groupDoc = snapshot.documents.first()
                    val groupId = groupDoc.id

                    val memberRef = groupDoc.reference
                        .collection("members")
                        .document(uid)

                    val userRef = firestore
                        .collection("users")
                        .document(uid)

                    val newMember = GroupMember(
                        uid = uid,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = "member",
                        currentScore = 0
                    )

                    firestore.runBatch { batch ->
                        // Add member object to group's subcollection
                        batch.set(memberRef, newMember)

                        // Add groupId to user's joinedGroups array
                        batch.update(
                            userRef,
                            "joinedGroups",
                            FieldValue.arrayUnion(groupId)
                        )
                    }.addOnSuccessListener {
                        callback(Result.success(groupId))
                    }.addOnFailureListener {
                        callback(Result.failure(it))
                    }
                }
                .addOnFailureListener {
                    callback(Result.failure(it))
                }
        }
    }

    fun getGroup(groupId: String, callback: (Group?) -> Unit) {
        groupsRef.document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                val group = doc.toObject(Group::class.java)
                callback(group)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getGroupMembers(groupId: String, callback: (List<GroupMember>) -> Unit) {
        groupsRef.document(groupId)
            .collection("members")
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.toObjects(GroupMember::class.java))
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun generateGroupCode(): String {
        return (1..6)
            .map { ('A'..'Z').random() }
            .joinToString("")
    }
}
