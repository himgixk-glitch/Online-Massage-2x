package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CallEntity
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.StoryEntity
import com.example.data.model.StoryType
import com.example.data.model.UserEntity
import com.example.data.repository.ChatterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS,
    CALLS,
    STORIES,
    CONTACTS,
    SETTINGS
}

data class ActiveCallState(
    val callId: String,
    val contactName: String,
    val contactPhoto: String,
    val contactId: String,
    val type: CallType,
    val isIncoming: Boolean,
    val isAnswered: Boolean = false,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isVideoOff: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isPipMode: Boolean = false
)

data class ActiveStoryGroup(
    val userId: String,
    val userName: String,
    val userPhoto: String,
    val stories: List<StoryEntity>,
    val currentIndex: Int = 0,
    val isPaused: Boolean = false
)

class ChatterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatterRepository(AppDatabase.getInstance(application))

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chats: StateFlow<List<ChatEntity>> = repository.allChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private val _activeStoryGroup = MutableStateFlow<ActiveStoryGroup?>(null)
    val activeStoryGroup: StateFlow<ActiveStoryGroup?> = _activeStoryGroup.asStateFlow()

    private val _chatSearchQuery = MutableStateFlow("")
    val chatSearchQuery: StateFlow<String> = _chatSearchQuery.asStateFlow()

    private val _isNewChatDialogOpen = MutableStateFlow(false)
    val isNewChatDialogOpen: StateFlow<Boolean> = _isNewChatDialogOpen.asStateFlow()

    private val _isCreateStoryDialogOpen = MutableStateFlow(false)
    val isCreateStoryDialogOpen: StateFlow<Boolean> = _isCreateStoryDialogOpen.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    private val _editingMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage: StateFlow<MessageEntity?> = _editingMessage.asStateFlow()

    private val _isAuthCompleted = MutableStateFlow(true)
    val isAuthCompleted: StateFlow<Boolean> = _isAuthCompleted.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        if (chatId != null) {
            viewModelScope.launch {
                repository.markChatAsRead(chatId)
            }
        }
    }

    fun getChatMessages(chatId: String): StateFlow<List<MessageEntity>> =
        repository.getChatMessages(chatId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getChat(chatId: String): StateFlow<ChatEntity?> =
        repository.getChatById(chatId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setChatSearchQuery(query: String) {
        _chatSearchQuery.value = query
    }

    fun setReplyingTo(message: MessageEntity?) {
        _replyingToMessage.value = message
    }

    fun setEditingMessage(message: MessageEntity?) {
        _editingMessage.value = message
    }

    fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        voiceDurationSeconds: Int = 0
    ) {
        val reply = _replyingToMessage.value
        val edit = _editingMessage.value

        if (edit != null) {
            viewModelScope.launch {
                repository.editMessage(edit.id, text)
                _editingMessage.value = null
            }
            return
        }

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text,
                type = type,
                mediaUrl = mediaUrl,
                replyToId = reply?.id,
                replyToText = reply?.text,
                voiceDurationSeconds = voiceDurationSeconds,
                scope = viewModelScope
            )
            _replyingToMessage.value = null
        }
    }

    fun reactToMessage(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.reactToMessage(messageId, emoji)
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        viewModelScope.launch {
            repository.deleteForEveryone(messageId)
        }
    }

    fun deleteMessageLocally(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // Call Actions
    fun startCall(contactId: String, contactName: String, contactPhoto: String, type: CallType) {
        val callId = "call_${System.currentTimeMillis()}"
        _activeCall.value = ActiveCallState(
            callId = callId,
            contactName = contactName,
            contactPhoto = contactPhoto,
            contactId = contactId,
            type = type,
            isIncoming = false,
            isAnswered = false
        )

        // Simulate contact picking up the call after 2 seconds!
        viewModelScope.launch {
            delay(2200)
            if (_activeCall.value != null && !_activeCall.value!!.isAnswered) {
                _activeCall.value = _activeCall.value!!.copy(isAnswered = true)
                startCallTimer()
            }
        }
    }

    fun triggerIncomingCall(callerName: String, callerPhoto: String, type: CallType) {
        val callId = "call_inc_${System.currentTimeMillis()}"
        _activeCall.value = ActiveCallState(
            callId = callId,
            contactName = callerName,
            contactPhoto = callerPhoto,
            contactId = "user_incoming",
            type = type,
            isIncoming = true,
            isAnswered = false
        )
    }

    fun answerCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isAnswered = true)
        startCallTimer()
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCall.value != null) {
                delay(1000)
                _activeCall.value?.let { call ->
                    _activeCall.value = call.copy(durationSeconds = call.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMute() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isMuted = !call.isMuted)
        }
    }

    fun toggleVideo() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isVideoOff = !call.isVideoOff)
        }
    }

    fun toggleSpeaker() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isSpeakerOn = !call.isSpeakerOn)
        }
    }

    fun switchCamera() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isFrontCamera = !call.isFrontCamera)
        }
    }

    fun togglePipMode() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isPipMode = !call.isPipMode)
        }
    }

    fun endCall() {
        val call = _activeCall.value ?: return
        callTimerJob?.cancel()
        callTimerJob = null

        val duration = call.durationSeconds
        val status = if (call.isAnswered) {
            if (call.isIncoming) CallStatus.INCOMING else CallStatus.OUTGOING
        } else {
            if (call.isIncoming) CallStatus.MISSED else CallStatus.ENDED
        }

        val myUser = currentUser.value
        viewModelScope.launch {
            repository.recordCall(
                callerId = if (call.isIncoming) call.contactId else myUser?.uid ?: "user_me",
                callerName = if (call.isIncoming) call.contactName else myUser?.name ?: "Alex",
                callerPhoto = if (call.isIncoming) call.contactPhoto else myUser?.photo ?: "",
                receiverId = if (call.isIncoming) myUser?.uid ?: "user_me" else call.contactId,
                receiverName = if (call.isIncoming) myUser?.name ?: "Alex" else call.contactName,
                receiverPhoto = if (call.isIncoming) myUser?.photo ?: "" else call.contactPhoto,
                type = call.type,
                status = status,
                durationSeconds = duration
            )
        }
        _activeCall.value = null
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            repository.clearCallHistory()
        }
    }

    // Story / Status Actions
    fun openStoryViewer(group: ActiveStoryGroup) {
        _activeStoryGroup.value = group
        markCurrentStoryViewed()
    }

    fun closeStoryViewer() {
        _activeStoryGroup.value = null
    }

    fun nextStory() {
        val group = _activeStoryGroup.value ?: return
        if (group.currentIndex < group.stories.size - 1) {
            _activeStoryGroup.value = group.copy(currentIndex = group.currentIndex + 1)
            markCurrentStoryViewed()
        } else {
            closeStoryViewer()
        }
    }

    fun previousStory() {
        val group = _activeStoryGroup.value ?: return
        if (group.currentIndex > 0) {
            _activeStoryGroup.value = group.copy(currentIndex = group.currentIndex - 1)
            markCurrentStoryViewed()
        }
    }

    fun setStoryPaused(isPaused: Boolean) {
        _activeStoryGroup.value?.let {
            _activeStoryGroup.value = it.copy(isPaused = isPaused)
        }
    }

    private fun markCurrentStoryViewed() {
        val group = _activeStoryGroup.value ?: return
        val story = group.stories.getOrNull(group.currentIndex) ?: return
        viewModelScope.launch {
            repository.markStoryViewed(story.storyId)
        }
    }

    fun postStory(
        type: StoryType,
        mediaUrl: String? = null,
        textContent: String? = null,
        gradientIndex: Int = 0,
        caption: String? = null
    ) {
        viewModelScope.launch {
            repository.postStory(type, mediaUrl, textContent, gradientIndex, caption)
            _isCreateStoryDialogOpen.value = false
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
        }
    }

    fun openCreateStoryDialog() {
        _isCreateStoryDialogOpen.value = true
    }

    fun closeCreateStoryDialog() {
        _isCreateStoryDialogOpen.value = false
    }

    fun openNewChatDialog() {
        _isNewChatDialogOpen.value = true
    }

    fun closeNewChatDialog() {
        _isNewChatDialogOpen.value = false
    }

    fun createChat(name: String, photo: String, isGroup: Boolean, participantIds: String) {
        viewModelScope.launch {
            val chatId = repository.createNewChat(name, photo, isGroup, participantIds)
            _isNewChatDialogOpen.value = false
            selectChat(chatId)
        }
    }

    fun saveProfile(name: String, phone: String, photo: String, bio: String) {
        viewModelScope.launch {
            repository.saveCurrentUser(name, phone, photo, bio)
            _isAuthCompleted.value = true
        }
    }

    fun setAuthCompleted(completed: Boolean) {
        _isAuthCompleted.value = completed
    }
}
