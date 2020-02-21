package com.malliina.boattracker.ui.boats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.boat_item.view.*
import kotlinx.android.synthetic.main.boats_fragment.view.*
import kotlinx.android.synthetic.main.stat_box.view.*

class BoatsFragment: ResourceFragment(R.layout.boats_fragment) {
    private lateinit var boatsAdapter: BoatsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: BoatsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewManager = LinearLayoutManager(context)
        boatsAdapter = BoatsAdapter(emptyList(), lang.settings)
        view.boats_list.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = boatsAdapter
        }
        viewModel = ViewModelProvider(this, BoatsViewModelFactory(requireActivity().application))
            .get(BoatsViewModel::class.java)
        viewModel.boats.observe(viewLifecycleOwner) { user ->
            boatsAdapter.boats = user.boats
            boatsAdapter.notifyDataSetChanged()
        }
        val notifications = view.notifications_switch
        notifications.isChecked = viewModel.notificationsEnabled
        notifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNotifications(isChecked)
        }
        notifications.text = lang.settings.notifications
        view.notifications_text.text = lang.settings.notificationsText
        view.token_footer.text = lang.settings.tokenText
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
