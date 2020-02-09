package com.malliina.boattracker.ui.boats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.*
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
        setContentView(R.layout.boats_activity)

        viewManager = LinearLayoutManager(this)
        val lang: Lang = intent.getParcelableExtra(Lang.key)
        boatsAdapter = BoatsAdapter(emptyList(), lang.settings)
        findViewById<RecyclerView>(R.id.boats_list).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = boatsAdapter
        }
        Timber.i("Loading boats...")
        val token: IdToken = intent.getParcelableExtra(IdToken.key)
        viewModel = ViewModelProvider(this, BoatsViewModelFactory(application, token))
            .get(BoatsViewModel::class.java)
        viewModel.getBoats().observe(this, Observer<BoatUser> { user ->
            boatsAdapter.boats = user?.boats ?: emptyList()
            boatsAdapter.notifyDataSetChanged()
        })
        val notifications = findViewById<Switch>(R.id.notifications_switch)
        notifications.isChecked = viewModel.notificationsEnabled
        notifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNotifications(isChecked)
        }
        findViewById<Toolbar>(R.id.boats_toolbar).title = lang.track.boats
        findViewById<Switch>(R.id.notifications_switch).text = lang.settings.notifications
        findViewById<TextView>(R.id.notifications_text).text = lang.settings.notificationsText
        findViewById<TextView>(R.id.token_footer).text = lang.settings.tokenText
    }
}

class BoatsAdapter(var boats: List<Boat>, private val lang: SettingsLang)
    : RecyclerView.Adapter<BoatsAdapter.BoatHolder>() {
    class BoatHolder(val layout: ConstraintLayout): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoatHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.boat_item, parent, false) as ConstraintLayout
        return BoatHolder(layout)
    }

    override fun onBindViewHolder(bh: BoatHolder, position: Int) {
        val boat = boats[position]
        val layout = bh.layout
        layout.boat.stat_label.text = lang.boat
        layout.boat.stat_value.text = boat.name.name
        layout.token.stat_label.text = lang.token
        layout.token.stat_value.text = boat.token.token
    }

    override fun getItemCount(): Int = boats.size
}
