package com.example.bodybalancy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.R
import com.example.bodybalancy.dataClasses.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAdapter(
    private val scheduleList: MutableList<ScheduleItem>,
    private val toggleSchedule: (Int) -> Unit,
    private val editSchedule: (Int) -> Unit,
    private val deleteSchedule: (Int) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val toggleButton: ToggleButton = itemView.findViewById(R.id.toggleButton)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(schedule: ScheduleItem) {
            titleTextView.text = "Title: ${schedule.title}"
            descriptionTextView.text = "Description:\n${schedule.description}"
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = timeFormat.format(schedule.timeInMillis)
            timeTextView.text = "Time: $time"

            toggleButton.setOnCheckedChangeListener(null)
            toggleButton.isChecked = schedule.isOn
            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                toggleSchedule(schedule.id)
            }

            editButton.setOnClickListener {
                editSchedule(schedule.id)
            }

            deleteButton.setOnClickListener {
                deleteSchedule(schedule.id)
            }
        }
    }
}
