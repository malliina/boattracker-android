package com.malliina.boattracker.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.malliina.boattracker.Language
import com.malliina.boattracker.R
import com.malliina.boattracker.ui.ComposeFragment
import com.malliina.boattracker.ui.ResourceFragment
import kotlinx.android.synthetic.main.languages_activity.view.*
import timber.log.Timber

class ComposeLanguages : ComposeFragment() {
    private val viewModel: LanguagesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LanguagesView(lang, viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.language.observe(viewLifecycleOwner) {
            // Does not work: findNavController().currentDestination?.label = ...
            (activity as AppCompatActivity).supportActionBar?.title = lang.profile.language
        }
    }
}

class LanguagesFragment : ResourceFragment(R.layout.languages_activity) {
    private lateinit var languagesView: ListView
    private val viewModel: LanguagesViewModel by viewModels()
    private lateinit var items: List<LanguageItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val profileLang = lang.profile
        items = listOf(
            LanguageItem(Language.Swedish, profileLang.swedish),
            LanguageItem(Language.Finnish, profileLang.finnish),
            LanguageItem(Language.English, profileLang.english)
        )
        val listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice,
            items.map { it.title }
        )
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
        findNavController().currentDestination?.label = lang.profile.language
        viewModel.language.observe(viewLifecycleOwner) { _ ->
            // Does not work: findNavController().currentDestination?.label = ...
            (activity as AppCompatActivity).supportActionBar?.title = lang.profile.language
        }
    }
}

data class LanguageItem(val language: Language, val title: String)
