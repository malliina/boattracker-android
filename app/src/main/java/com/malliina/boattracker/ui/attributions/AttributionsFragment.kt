package com.malliina.boattracker.ui.attributions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.AppAttribution
import com.malliina.boattracker.Link
import com.malliina.boattracker.R
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.attribution_item.view.*
import kotlinx.android.synthetic.main.attribution_link_item.view.*
import kotlinx.android.synthetic.main.attributions_fragment.view.*

class ComposeAttributions : ComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AttributionsView(lang.attributions.attributions)
            }
        }
    }
}

class AttributionsFragment : ResourceFragment(R.layout.attributions_fragment) {
    private lateinit var attributionsAdapter: AttributionsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewManager = LinearLayoutManager(context)
        attributionsAdapter = AttributionsAdapter(lang.attributions.attributions)
        view.attributions_list.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = attributionsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
}

class AttributionsAdapter(var attributions: List<AppAttribution>) : RecyclerView.Adapter<AttributionsAdapter.AttributionHolder>() {
    class AttributionHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributionHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.attribution_item, parent, false) as ConstraintLayout
        return AttributionHolder(layout)
    }

    override fun onBindViewHolder(ah: AttributionHolder, position: Int) {
        val attribution = attributions[position]
        val layout = ah.layout
        layout.attribution_title.text = attribution.title
        if (attribution.text != null) {
            layout.attribution_text.text = attribution.text
        } else {
            layout.attribution_text.visibility = View.GONE
        }
        val childLayoutManager = LinearLayoutManager(ah.itemView.context)
        layout.links_list.apply {
            layoutManager = childLayoutManager
            adapter = LinksAdapter(attribution.links)
        }
    }

    override fun getItemCount(): Int = attributions.size
}

class LinksAdapter(var links: List<Link>) : RecyclerView.Adapter<LinksAdapter.LinkHolder>() {
    class LinkHolder(val layout: ConstraintLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.attribution_link_item, parent, false) as ConstraintLayout
        return LinkHolder(layout)
    }

    override fun onBindViewHolder(lh: LinkHolder, position: Int) {
        val link = links[position]
        val layout = lh.layout
        layout.attribution_link_url.text = link.url.url
    }

    override fun getItemCount(): Int = links.size
}
