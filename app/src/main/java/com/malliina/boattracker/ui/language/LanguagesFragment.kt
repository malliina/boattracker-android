package com.malliina.boattracker.ui.language

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.malliina.boattracker.*
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.languages_activity.view.*
import timber.log.Timber

class LanguagesFragment: ResourceFragment(R.layout.languages_activity) {
    private lateinit var languagesView: ListView
    private lateinit var viewModel: LanguagesViewModel
    private lateinit var items: List<LanguageItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, LanguagesViewModelFactory(requireActivity().application))
            .get(LanguagesViewModel::class.java)
        val profileLang = lang.profile
        view.languages_toolbar.title = profileLang.language
        items = listOf(
            LanguageItem(Language.Swedish, profileLang.swedish),
            LanguageItem(Language.Finnish, profileLang.finnish),
            LanguageItem(Language.English, profileLang.english)
        )
        val listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, items.map { it.title })
        languagesView = view.language_list_view.apply {
            choiceMode = ListView.CHOICE_MODE_SINGLE
            adapter = listAdapter
            setItemChecked(items.indexOfFirst { it.language == lang.language }, true)
        }
        languagesView.setOnItemClickListener { _, _, position, _ ->
            val selected = items[position]
            Timber.i("Changing language to ${selected.title}...")
            viewModel.changeLanguage(selected.language)
        }
        viewModel.getLanguage().observe(viewLifecycleOwner) { language ->
                view.languages_toolbar.title = lang.profile.language
        }
    }
}

data class LanguageItem(val language: Language, val title: String)
