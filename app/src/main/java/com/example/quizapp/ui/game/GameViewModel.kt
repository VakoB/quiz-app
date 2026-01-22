package com.example.quizapp.ui.game

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizapp.data.games.GameRepository
import com.example.quizapp.data.groups.GroupsRepository
import com.example.quizapp.data.models.GamePlayer
import com.example.quizapp.data.models.Question
import com.example.quizapp.data.models.GameState
import com.google.firebase.database.ValueEventListener

class GameViewModel(
    private val repository: GameRepository = GameRepository(),
    private val groupsRepository: GroupsRepository = GroupsRepository()
) : ViewModel() {

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private var gameStateListener: ValueEventListener? = null

    private var lastQuestionIndex: Int? = null

    private val _players = MutableLiveData<List<GamePlayer>>()
    val players: LiveData<List<GamePlayer>> = _players

    private var playersListener: ValueEventListener? = null

    private var activeGroupId: String? = null


    fun listenGamePlayers(groupId: String) {
        if (playersListener != null) {
            repository.removePlayersListener(groupId, playersListener!!)
        }

        playersListener = repository.listenGamePlayers(groupId) { list ->
            _players.postValue(list)
        }
    }

    fun nextQuestion(groupId: String) {
        repository.nextQuestion(groupId)
    }

    // Fetch questions
    fun loadQuestions(groupId: String) {
        repository.getQuestions(groupId) { list ->
            _questions.value = list
            lastQuestionIndex?.let { updateCurrentQuestion(it) }
        }
    }

    private fun updateCurrentQuestion(index: Int) {
        val list = _questions.value ?: return
        if (index in list.indices) {
            _currentQuestion.value = list[index]
        }
    }


    fun publishQuestionsAndStartWaitingGame(
        groupId: String,
        questions: List<Question>,
        onComplete: (Boolean) -> Unit
    ) {
        repository.publishQuestions(groupId, questions) { success ->
            if (!success) {
                onComplete(false)
                return@publishQuestions
            }

            repository.createWaitingGame(groupId) { gameCreated ->
                onComplete(gameCreated)
            }
        }
    }

    fun joinLobby(groupId: String, onComplete: (Boolean) -> Unit) {
        repository.joinLobby(groupId, onComplete)
    }

    fun startGame(groupId: String, onComplete: (Boolean) -> Unit) {
        repository.startGame(groupId, onComplete)
    }

    fun listenGame(groupId: String) {
        activeGroupId = groupId
        gameStateListener = repository.listenGameState(groupId) { state ->
            _gameState.postValue(state)

            if (state.status != "FINISHED") {
                lastQuestionIndex = state.currentQuestionIndex
                updateCurrentQuestion(state.currentQuestionIndex)
            }
        }
    }


    fun submitAnswer(groupId: String, selectedIndex: Int) {
        repository.submitAnswer(groupId, selectedIndex)
    }


    fun finishGame(groupId: String) {
        repository.finishGame(groupId) {

        }
    }


    fun finalizeGame(groupId: String) {
        val players = _players.value ?: return

        repository.applyGameScoresToGroup(
            groupId = groupId,
            players = players
        ) { success ->
            if (success) {
                repository.cleanupGame(groupId)
            }
        }
    }

    override fun onCleared() {
        activeGroupId?.let { groupId ->
            gameStateListener?.let { repository.removeListener(groupId, it) }
            playersListener?.let { repository.removePlayersListener(groupId, it) }
        }
    }

}
