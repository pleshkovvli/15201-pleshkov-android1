package ru.nsu.ccfit.pleshkov.notebook.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*

import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.*
import ru.nsu.ccfit.pleshkov.notebook.presenter.StatusSettingsAdapter

class SettingsActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_settings

    private lateinit var settingsAdapter: StatusSettingsAdapter
    private lateinit var settingsApi: SettingsApi

    companion object {
        fun newIntent(context: Context): Intent =
                Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)

        settingsApi = SettingsApi(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings_save -> {
            settingsApi.setSettings(settingsAdapter.currentSettings())
            onBackPressed()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun sharedPreferences() =
            getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE)

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        settingsAdapter = StatusSettingsAdapter(settingsApi.getSettings())
        recyclerView.adapter = settingsAdapter

        val divider = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(divider)
    }
}
