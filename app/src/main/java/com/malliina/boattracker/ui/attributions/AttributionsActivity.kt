package com.malliina.boattracker.ui.attributions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.*
import kotlinx.android.synthetic.main.attribution_item.view.*
import kotlinx.android.synthetic.main.attribution_link_item.view.*

class AttributionsActivity: AppCompatActivity() {
    private lateinit var attrs: AttributionInfo
    private lateinit var attributionsAdapter: AttributionsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attributions_activity)

        viewManager = LinearLayoutManager(this)
        attrs = intent.getParcelableExtra(AttributionInfo.key)
        attributionsAdapter = AttributionsAdapter(attrs.attributions)
        findViewById<Toolbar>(R.id.attributions_toolbar).title = attrs.title
        findViewById<RecyclerView>(R.id.attributions_list).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = attributionsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
}

class AttributionsAdapter(var attributions: List<AppAttribution>): RecyclerView.Adapter<AttributionsAdapter.AttributionHolder>() {
    class AttributionHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

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

class LinksAdapter(var links: List<Link>): RecyclerView.Adapter<LinksAdapter.LinkHolder>() {
    class LinkHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.attribution_link_item, parent, false) as ConstraintLayout
        return LinkHolder(layout)
    }

    override fun onBindViewHolder(lh: LinkHolder, position: Int) {
        val link = links[position]
        val layout = lh.layout
//        layout.attribution_link_text.text = link.text
        layout.attribution_link_url.text = link.url.url
    }

    override fun getItemCount(): Int = links.size
}
