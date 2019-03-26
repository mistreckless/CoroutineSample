package com.mistreckless.coroutinesample

import android.arch.lifecycle.ViewModel
import com.mistreckless.coroutinesample.service.FoursquareApiService
import com.mistreckless.coroutinesample.util.selectLoop
import com.mistreckless.coroutinesample.util.trySend
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext

class MainViewModel(private val apiService: FoursquareApiService) : ViewModel(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    init {
        process()
    }

    private val actionChannel = Channel<ViewAction>()

    fun destroy() {
        job.cancel()
    }

    fun sendAction(action: ViewAction) = runBlocking {
        actionChannel.send(action)
    }

    private fun process() {
        launch {
            val state = State()
            val fetchChannel = Channel<FetchState>()
            job.invokeOnCompletion {
                fetchChannel.cancel()
                actionChannel.cancel()
            }
            selectLoop {
                actionChannel.onReceive { action ->
                    when (action) {
                        is ViewAction.ViewCreated -> {
                            state.viewChannel = action.channel
                            when {
                                state.venues.isNotEmpty() -> state.viewState = ViewState.RestoredVenues(state.venues, state.scrollPosition)
                                state.fetchError != null -> state.viewState = ViewState.RestoredError
                                else -> state.viewState = ViewState.Init
                            }
                            state.viewChannel.trySend(state.viewState)
                        }
                        is ViewAction.FetchClick -> {
                            state.viewState = ViewState.Loading
                            state.viewChannel.trySend(state.viewState)
                            fetch(action.scope, fetchChannel)
                        }
                        is ViewAction.RetryClick -> {
                            state.viewState = ViewState.Loading
                            state.viewChannel.trySend(state.viewState)
                            fetch(action.scope, fetchChannel)
                        }
                        is ViewAction.ViewDestroy -> {
                            state.scrollPosition = action.scrollPosition
                        }
                    }
                }
                fetchChannel.onReceive { result ->
                    when (result) {
                        is FetchState.Success -> {
                            state.venues = result.venues
                            state.fetchError = null
                            state.viewState = ViewState.VenuesFetched(state.venues)
                            state.viewChannel.trySend(state.viewState)
                        }
                        is FetchState.Error -> {
                            state.fetchError = result.e
                            state.viewState = ViewState.Error
                            state.viewChannel.trySend(state.viewState)
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetch(scope: CoroutineScope, channel: SendChannel<FetchState>) {
        scope.launch(Dispatchers.IO) {
            try {
                val response =
                    apiService.fetchVenues(BuildConfig.MoscowLL, BuildConfig.SearchRadius, BuildConfig.CategoryId)
                        .await()
                channel.send(FetchState.Success(response.response.venues))
            } catch (e: Exception) {
                channel.send(FetchState.Error(e))
            }
        }
    }
}

