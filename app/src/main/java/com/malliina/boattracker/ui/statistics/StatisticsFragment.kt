package com.malliina.boattracker.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.Controls
import com.malliina.boattracker.ui.ResourceFragment
import com.malliina.boattracker.ui.Status
import kotlinx.android.synthetic.main.statistics_fragment.view.*
import kotlinx.android.synthetic.main.track_item.view.*

class ComposeStatistics : ComposeFragment() {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            StatisticsView(viewModel, lang)
        }
    }
}

class StatisticsFragment : ResourceFragment(R.layout.statistics_fragment) {
    private lateinit var viewAdapter: StatsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewManager = LinearLayoutManager(context)
        viewAdapter = StatsAdapter(null, lang)
        view.statistics_view.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        val ctrl =
            Controls(view.statistics_loading, view.statistics_view, view.statistics_feedback_text)
        viewModel.stats.observe(viewLifecycleOwner) { outcome ->
            when (outcome.status) {
                Status.Success -> {
                    ctrl.showList()
                    outcome.data?.let { data ->
                        if (data.yearly.isEmpty()) {
                        } else {
                            viewAdapter.stats = data
                            viewAdapter.notifyDataSetChanged()
                        }
                    }
                }
                Status.Error -> {
                }
                Status.Loading -> {
                    ctrl.enableLoading()
                }
            }
        }
    }
}

class StatsAdapter(
    var stats: StatsResponse?,
    private val lang: Lang
) : RecyclerView.Adapter<StatsAdapter.StatHolder>() {
    private val yearly: List<YearlyStats> get() = stats?.yearly ?: emptyList()
    private val list: List<Stats> get() = yearly.flatMap { y -> listOf(y) + y.monthly }
    private val trackLang = lang.track

    class StatHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.track_item,
            parent,
            false
        ) as ConstraintLayout
        return StatHolder(layout)
    }

    override fun onBindViewHolder(th: StatHolder, position: Int) {
        val row = list[position]
        val layout = th.layout
        layout.date_text.text = row.label
        layout.first.fill(trackLang.distance, row.distance.formatKilometers())
        layout.second.fill(trackLang.duration, row.duration.formatted())
        layout.third.fill(trackLang.days, "${row.days}")
        layout.track_more_button.visibility = View.GONE
    }

    override fun getItemCount(): Int = list.size
}
