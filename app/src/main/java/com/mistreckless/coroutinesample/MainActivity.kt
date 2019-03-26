package com.mistreckless.coroutinesample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.actor
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private var job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    @Inject
    lateinit var viewModel: MainViewModel

    @UseExperimental(InternalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()
        val actor = actor<ViewState> {
            for (msg in channel) processState(msg)
        }
        viewModel.sendAction(ViewAction.ViewCreated(this, actor))
    }

    private fun processState(state: ViewState) {
        when(state){
            ViewState.Init -> {
                fetchBtn.visibility = View.VISIBLE
                retryBtn.visibility = View.GONE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                fetchBtn.setOnClickListener {
                    viewModel.sendAction(ViewAction.FetchClick(this))
                }
            }
            ViewState.Loading -> {
                fetchBtn.visibility = View.GONE
                retryBtn.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            ViewState.Error -> {
                fetchBtn.visibility = View.GONE
                retryBtn.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                retryBtn.setOnClickListener {
                    viewModel.sendAction(ViewAction.RetryClick(this))
                }
            }
            is ViewState.VenuesFetched -> {
                fetchBtn.visibility = View.GONE
                retryBtn.visibility = View.GONE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val adapter = VenuesAdapter(state.venues)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
            is ViewState.RestoredVenues -> {
                fetchBtn.visibility = View.GONE
                retryBtn.visibility = View.GONE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val adapter = VenuesAdapter(state.venues)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(state.position)
            }
            ViewState.RestoredError -> {
                fetchBtn.visibility = View.GONE
                retryBtn.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                retryBtn.setOnClickListener {
                    viewModel.sendAction(ViewAction.RetryClick(this))
                }
            }
        }
    }

    override fun onDestroy() {
        (recyclerView.layoutManager as? LinearLayoutManager)?.let {
            viewModel.sendAction(ViewAction.ViewDestroy(it.findFirstVisibleItemPosition()))
        }

        if (isFinishing) viewModel.destroy()
        job.cancel()
        super.onDestroy()
    }
}
