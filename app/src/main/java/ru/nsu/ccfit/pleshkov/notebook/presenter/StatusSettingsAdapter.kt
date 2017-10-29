package ru.nsu.ccfit.pleshkov.notebook.presenter

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.settings_item.view.*
import ru.nsu.ccfit.pleshkov.notebook.R
import ru.nsu.ccfit.pleshkov.notebook.model.StatusSettings

class StatusSettingsAdapter(
        items: Array<StatusSettings>
) : RecyclerView.Adapter<StatusSettingsAdapter.ViewHolder>() {

    private val settingsItems = items.copyOf()

    fun currentSettings() = settingsItems.copyOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflate(R.layout.settings_item)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(settingsItems[position])
    }

    override fun getItemCount() = settingsItems.size

    private fun ViewGroup.inflate(layoutRes: Int): View =
            LayoutInflater.from(context).inflate(layoutRes, this, false)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(settings: StatusSettings) = with(itemView) {
            val daysValue = settings.days
            val hoursValue = settings.hours
            val minutesValue = settings.minutes

            days.setText("$daysValue")
            days.addTextChangedListener(SimpleTextWatcher({ value ->
                settings.days = value
                days.setTextColor(getColorById(R.color.ok_color))
            }, {
                days.setTextColor(getColorById(R.color.error_color))
            }))

            hours.setText("$hoursValue")
            hours.addTextChangedListener(SimpleTextWatcher({ value ->
                settings.hours = value
                hours.setTextColor(getColorById(R.color.ok_color))
            }, {
                hours.setTextColor(getColorById(R.color.error_color))
            }))

            minutes.setText("$minutesValue")
            minutes.addTextChangedListener(SimpleTextWatcher({ value ->
                settings.minutes = value
                minutes.setTextColor(getColorById(R.color.ok_color))
            }, {
                minutes.setTextColor(getColorById(R.color.error_color))
            }))

            val status = settings.status
            statusTypeText.text = status.toString()
            statusTypeText.setTextColor(getColor(status))
        }
    }
}

class SimpleTextWatcher(
        private val setValue: (Int) -> Unit,
        private val onFailed: () -> Unit) : TextWatcher {
    override fun afterTextChanged(text: Editable) {
        val value = text.toString().toIntOrNull()
        if(value != null && value >= 0) {
            setValue(value)
        } else {
            onFailed()
        }
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}