package com.example.quizapp.data.games


import com.example.quizapp.data.auth.UserRepository
import com.example.quizapp.data.models.GamePlayer
import com.example.quizapp.data.models.Question
import com.example.quizapp.data.models.GameState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class GameRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val rtdb: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val userRepository: UserRepository = UserRepository()
) {

    fun getQuestions(groupId: String, callback: (List<Question>) -> Unit) {
        firestore.collection("groups")
            .document(groupId)
            .collection("questions")
            .get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                val questions = snapshot.toObjects(Question::class.java)
                callback(questions)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun nextQuestion(groupId: String) {
        val gameRef = rtdb.reference
            .child("games")
            .child(groupId)

        gameRef
            .child("currentQuestionIndex")
            .runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentIndex = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentIndex + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (!committed) return

                resetPlayersForNextQuestion(gameRef)
            }
        })
    }

    //next question helper
    private fun resetPlayersForNextQuestion(gameRef: DatabaseReference) {
        gameRef.child("players")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { playerSnap ->
                    playerSnap.ref.updateChildren(
                        mapOf(
                            "selectedOption" to null,
                            "isCorrect" to null
                        )
                    )
                }
            }
    }

    fun startGame(groupId: String, onComplete: (Boolean) -> Unit) {
        val gameRef = rtdb.reference.child("games").child(groupId)

        val updateMap = mapOf(
            "status" to "QUESTION",
            "currentQuestionIndex" to 0,
            "phaseEndTime" to System.currentTimeMillis() + 15000
        )

        gameRef.updateChildren(updateMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun joinLobby(groupId: String, onComplete: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onComplete(false)
            return
        }

        userRepository.getUser(uid) { user ->
            if (user == null) {
                onComplete(false)
                return@getUser
            }

            val player = GamePlayer(
                uid = uid,
                firstName = user.firstName,
                lastName = user.lastName,
                role = "member",
                currentScore = 0
            )

            val playerRef = rtdb.reference
                .child("games")
                .child(groupId)
                .child("players")
                .child(uid)

            playerRef.setValue(player)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    fun publishQuestions(
        groupId: String,
        questions: List<Question>,
        onComplete: (Boolean) -> Unit
    ) {
        val questionsRef = firestore
            .collection("groups")
            .document(groupId)
            .collection("questions")

        questionsRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()

                // delete old questions
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // add new questions
                questions.forEach { question ->
                    val docRef = if (question.questionId.isNotEmpty()) {
                        questionsRef.document(question.questionId)
                    } else {
                        questionsRef.document() // generate new id if empty
                    }
                    batch.set(docRef, question)
                }

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    fun createWaitingGame(groupId: String, onComplete: (Boolean) -> Unit) {
        val gameRef = rtdb.reference.child("games").child(groupId)
        val initialState = mapOf(
            "status" to "WAITING",
            "currentQuestionIndex" to 0,
            "phaseEndTime" to 0L,
        )
        gameRef.setValue(initialState)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun submitAnswer(groupId: String, selectedIndex: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val gameRef = rtdb.reference
            .child("games")
            .child(groupId)

        gameRef.child("currentQuestionIndex")
            .get()
            .addOnSuccessListener { indexSnap ->

                val currentIndex =
                    indexSnap.getValue(Int::class.java) ?: return@addOnSuccessListener

                firestore.collection("groups")
                    .document(groupId)
                    .collection("questions")
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val question = snapshot.documents.getOrNull(currentIndex)
                            ?: return@addOnSuccessListener

                        val correctIndex = question.getLong("correctIndex")?.toInt()
                            ?: return@addOnSuccessListener

                        val isCorrect = selectedIndex == correctIndex

                        val playerRef = gameRef
                            .child("players")
                            .child(uid)


                        val updates = mutableMapOf<String, Any>(
                            "selectedOption" to selectedIndex,
                            "isCorrect" to isCorrect
                        )

                        if (isCorrect) {
                            updates["currentScore"] = ServerValue.increment(1)
                        }


                        playerRef.updateChildren(updates)
                    }
            }
    }


    fun listenGameState(groupId: String, callback: (GameState) -> Unit): ValueEventListener {
        val gameRef = rtdb.reference.child("games").child(groupId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(GameState::class.java)
                state?.let { callback(it) }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        gameRef.addValueEventListener(listener)
        return listener
    }

    fun removeListener(groupId: String, listener: ValueEventListener) {
        rtdb.reference.child("games").child(groupId).removeEventListener(listener)
    }

    fun finishGame(groupId: String, onComplete: (Boolean) -> Unit) {
        rtdb.reference
            .child("games")
            .child(groupId)
            .updateChildren(
                mapOf(
                    "status" to "FINISHED",
                )
            )
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun applyGameScoresToGroup(
        groupId: String,
        players: List<GamePlayer>,
        onComplete: (Boolean) -> Unit
    ) {
        val groupRef = firestore.collection("groups").document(groupId)

        firestore.runBatch { batch ->
            players.forEach { player ->
                val uid = player.uid
                if (uid.isBlank()) return@forEach

                val memberRef = groupRef
                    .collection("members")
                    .document(uid)

                batch.update(
                    memberRef,
                    "currentScore",
                    FieldValue.increment(player.currentScore.toLong())
                )
            }

        }.addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }


    fun cleanupGame(groupId: String, onComplete: (Boolean) -> Unit = {}) {
        rtdb.reference
            .child("games")
            .child(groupId)
            .removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    fun listenGamePlayers(
        groupId: String,
        onUpdate: (List<GamePlayer>) -> Unit
    ): ValueEventListener {

        val ref = rtdb.reference
            .child("games")
            .child(groupId)
            .child("players")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull {
                    it.getValue(GamePlayer::class.java)
                }
                onUpdate(players)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        return listener
    }

    fun removePlayersListener(groupId: String, listener: ValueEventListener) {
        rtdb.reference
            .child("games")
            .child(groupId)
            .child("players")
            .removeEventListener(listener)
    }


}
