package com.malinowski.bigandyellow.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.mapper.MessageToItemMapper
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.utils.SingleLiveEvent
import com.malinowski.bigandyellow.view.mvi.events.ChatEvent
import com.malinowski.bigandyellow.view.mvi.events.ChatEvent.*
import com.malinowski.bigandyellow.view.mvi.states.ScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class ChatViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var dataProvider: Repository

    private val _chatScreenState: MutableLiveData<ScreenState> = MutableLiveData()
    val chatScreenState: LiveData<ScreenState>
        get() = _chatScreenState

    //states
    val chatState = MutableLiveData(State.Chat())

    //event
    val scrollToPos = SingleLiveEvent<Int>()
    val showTopics = SingleLiveEvent<List<String>>()

    //mapper
    @Inject
    internal lateinit var messageToItemMapper: MessageToItemMapper

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun processEvent(event: ChatEvent) {
        when (event) {
            is SendMessage.ToUser ->
                sendMessageToUser(event.userEmail, event.content)
            is SendMessage.ToTopic ->
                sendMessageToTopic(event.streamId, event.topicName, event.content)
            is LoadMessages.ForUser ->
                processGetMessages(getMessages(event.userEmail, event.anchor))
            is LoadMessages.ForTopic ->
                processGetMessages(getMessages(event.streamId, event.topicName, event.anchor))
            is SetMessageNum ->
                setMessageNum(event.topicName, event.messageNum)
            is Reaction.Add ->
                addReaction(event.messageId, event.emojiName)
            is Reaction.Remove ->
                deleteReaction(event.messageId, event.emojiName)
            is DeleteMessage ->
                deleteMessage(event.messageId)
            is EditMessage ->
                editMessage(event.messageId, event.content)
            is ChangeMessageTopic ->
                changeMessageTopic(event.messageId, event.topic)
            is SetMessageID ->
                setMessageId(event.messageId)
            is LoadTopics ->
                loadTopics(event.messageId, event.streamId)
        }
    }

    fun setName(name: String) {
        chatState.value = State.Chat(name)
    }

    fun result(text: String = "") {
        _chatScreenState.postValue(ScreenState.Result(text))
    }

    fun error(error: Throwable) {
        _chatScreenState.postValue(ScreenState.Error(error))
    }

    private fun loading() {
        _chatScreenState.postValue(ScreenState.Loading)
    }

    private fun getMessages(
        stream: Int, topicName: String, anchor: String
    ): Observable<List<MessageData>> =
        dataProvider.loadMessages(stream, topicName, anchor)

    private fun getMessages(
        user: String, anchor: String
    ): Observable<List<MessageData>> =
        dataProvider.loadMessages(user, anchor)

    private fun processGetMessages(flow: Observable<List<MessageData>>) {
        loading()
        val state = chatState.value!!.copy()
        flow.map { messageToItemMapper(it, User.ME.id) }
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribeBy(
                onNext = { messagesPage ->
                    result()
                    val lastPage =
                        messagesPage.isEmpty() || messagesPage.size < PAGE_SIZE
                    val messages = state.messages.toMutableList().apply { addAll(messagesPage) }
                    chatState.value = state.copy(
                        messages = messages,
                        loaded = lastPage
                    )
                    if (state.messages.isEmpty())
                        scrollToPos.value = 0
                },
                onError = { error(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun setMessageNum(topicName: String?, messageNum: Int) = topicName?.let {
        dataProvider.setMessageNum(topicName, messageNum).subscribeBy(
            onSuccess = { Log.d("MESSAGE_NUM_DB", "$topicName $messageNum SAVED") },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    private fun sendMessage(
        type: RepositoryImpl.SendType, to: String, content: String, topic: String = ""
    ) {
        loading()
        dataProvider.sendMessage(type, to, content, topic)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { id ->
                    Log.d("MESSAGE_SEND", "$id $content")
                    val message = MessageItem(id, content, User.ME.id, true)
                    val state = chatState.value!!
                    val list = state.messages.toMutableList().apply { add(0, message) }
                    chatState.value = state.copy(messages = list)
                    scrollToPos.value = 0
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun sendMessageToUser(userEmail: String, content: String) {
        sendMessage(RepositoryImpl.SendType.PRIVATE, userEmail, content)
    }

    private fun sendMessageToTopic(stream: Int, topic: String, content: String) {
        sendMessage(
            RepositoryImpl.SendType.STREAM, "[$stream]", content, topic
        )
    }

    private fun addReaction(messageId: Int, emojiName: String) {
        dataProvider.addEmoji(messageId, emojiName).subscribeBy(
            onComplete = { },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    private fun deleteReaction(messageId: Int, emojiName: String) {
        dataProvider.deleteEmoji(messageId, emojiName).subscribeBy(
            onComplete = { },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    private fun deleteMessage(messageId: Int) {
        dataProvider.deleteMessage(messageId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    val state = chatState.value!!
                    val message = state.messages.find { it.id == messageId }
                    val list = state.messages.toMutableList().apply { remove(message) }
                    chatState.value = state.copy(messages = list)
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun editMessage(messageId: Int, content: String) {
        dataProvider.editMessage(messageId, content)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    val state = chatState.value!!
                    val message = state.messages.find { it.id == messageId }!!
                    val list = state.messages.toMutableList().apply {
                        this[indexOf(message)] = message.copy(message = content)
                    }
                    chatState.value = state.copy(messages = list)
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun changeMessageTopic(messageId: Int, topic: String) {
        dataProvider.editMessageTopic(messageId, topic)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    val state = chatState.value!!
                    val message = state.messages.find { it.id == messageId }!!
                    val list = state.messages.toMutableList().apply { remove(message) }
                    chatState.value = state.copy(messages = list)
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun loadTopics(messageId: Int, streamId: Int) {
        setMessageId(messageId)
        dataProvider.loadTopics(streamId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { topics ->
                    showTopics.value = topics.map { it.name }
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun setMessageId(messageId: Int) {
        chatState.postValue(chatState.value!!.copy(focusedMessageId = messageId))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}