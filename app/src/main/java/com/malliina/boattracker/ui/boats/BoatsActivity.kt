package com.malliina.boattracker.ui.boats

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import com.malliina.boattracker.Boat
import com.malliina.boattracker.BoatUser
import com.malliina.boattracker.IdToken
import com.malliina.boattracker.R
import com.malliina.boattracker.ui.tracks.TracksActivity
import kotlinx.android.synthetic.main.boat_item.view.*
import kotlinx.android.synthetic.main.stat_box.view.*
import timber.log.Timber

class BoatsActivity: AppCompatActivity() {
    private lateinit var boatsAdapter: BoatsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var viewModel: BoatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.boats_toolbar))
        Timber.tag(localClassName)
        setContentView(R.layout.boats_activity)
        viewManager = LinearLayoutManager(this)
        boatsAdapter = BoatsAdapter(emptyList())
        findViewById<RecyclerView>(R.id.boats_list).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = boatsAdapter
//            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        Timber.i("Loading boats...")
        val token = IdToken(intent.getStringExtra(TracksActivity.tokenExtra))
        viewModel = ViewModelProviders.of(this).get(BoatsViewModel::class.java)
        viewModel.getBoats(token).observe(this, Observer<BoatUser> { user ->
            boatsAdapter.boats = user?.boats ?: emptyList()
            boatsAdapter.notifyDataSetChanged()
        })
        val notifications = findViewById<Switch>(R.id.notifications_switch)
        notifications.isChecked = viewModel.notificationsEnabled
        notifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNotifications(isChecked)
        }
    }
}

class BoatsAdapter(var boats: List<Boat>): RecyclerView.Adapter<BoatsAdapter.BoatHolder>() {
    class BoatHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoatsAdapter.BoatHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.boat_item, parent, false) as ConstraintLayout
        return BoatHolder(layout)
    }

    override fun onBindViewHolder(bh: BoatHolder, position: Int) {
        val ctx = bh.itemView.context
        val boat = boats[position]
        val layout = bh.layout
        layout.boat.stat_label.text = ctx.getString(R.string.boat)
        layout.boat.stat_value.text = boat.name.name
        layout.token.stat_label.text = ctx.getString(R.string.token)
        layout.token.stat_value.text = boat.token.token
    }

    override fun getItemCount(): Int = boats.size
}
