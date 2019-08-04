package com.malliina.boattracker.ui.language

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.malliina.boattracker.Language
import com.malliina.boattracker.ProfileInfo
import com.malliina.boattracker.R
import com.malliina.boattracker.UserSettings
import timber.log.Timber

class LanguagesActivity: AppCompatActivity() {
    private lateinit var data: ProfileInfo
    private lateinit var languagesView: ListView

    private lateinit var viewModel: LanguagesViewModel

    private lateinit var items: List<LanguageItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.languages_toolbar))
        setContentView(R.layout.languages_activity)

        data = intent.getParcelableExtra(ProfileInfo.key)

        viewModel = ViewModelProviders.of(this, LanguagesViewModelFactory(application, data.token)).get(LanguagesViewModel::class.java)

        val lang = data.lang
        val profileLang = lang.profile
        findViewById<Toolbar>(R.id.languages_toolbar).title = profileLang.language
        items = listOf(
            LanguageItem(Language.Swedish, profileLang.swedish),
            LanguageItem(Language.Finnish, profileLang.finnish),
            LanguageItem(Language.English, profileLang.english)
        )
        val listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items.map { it.title })
        languagesView = findViewById<ListView>(R.id.language_list_view).apply {
            choiceMode = ListView.CHOICE_MODE_SINGLE
            adapter = listAdapter
            setItemChecked(items.indexOfFirst { it.language == data.language }, true)
        }
        languagesView.setOnItemClickListener { _, _, position, _ ->
            val selected = items[position]
            Timber.i("Changing language to ${selected.title}...")
            viewModel.changeLanguage(selected.language)
        }
        viewModel.getLanguage().observe(this, Observer { language ->
            // Update views
            UserSettings.instance.lang?.let {
                findViewById<Toolbar>(R.id.languages_toolbar).title = it.profile.language
            }
        })
    }
}


data class LanguageItem(val language: Language, val title: String)
