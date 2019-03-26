package com.mistreckless.coroutinesample

import com.mistreckless.coroutinesample.data.VenuesItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

data class State(
    var viewChannel: SendChannel<ViewState> = Channel(),
    var viewState: ViewState = ViewState.Init,
    var venues: List<VenuesItem> = emptyList(),
    var scrollPosition: Int = 0,
    var fetchError: Exception? = null
)

sealed class FetchState {
    data class Success(val venues: List<VenuesItem>) : FetchState()
    data class Error(val e: Exception) : FetchState()
}

sealed class ViewState {
    object Init : ViewState()
    object Loading : ViewState()
    object Error : ViewState()
    data class VenuesFetched(val venues: List<VenuesItem>) : ViewState()
    data class RestoredVenues(val venues: List<VenuesItem>, val position: Int) : ViewState()
    object RestoredError: ViewState()
}

sealed class ViewAction {
    data class ViewCreated(val scope: CoroutineScope, val channel: SendChannel<ViewState>) : ViewAction()
    data class FetchClick(val scope: CoroutineScope) : ViewAction()
    data class RetryClick(val scope: CoroutineScope) : ViewAction()
    data class ViewDestroy(val scrollPosition: Int): ViewAction()
}