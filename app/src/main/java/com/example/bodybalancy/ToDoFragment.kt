package com.example.bodybalancy

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bodybalancy.adapters.ScheduleAdapter
import com.example.bodybalancy.dataClasses.ScheduleItem
import com.example.bodybalancy.utils.AlarmReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ToDoFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var scheduleList: MutableList<ScheduleItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scheduleList = loadScheduleListFromLocalStorage()
        scheduleAdapter = ScheduleAdapter(scheduleList, this::toggleSchedule, this::editSchedule) { scheduleId ->
            deleteSchedule(scheduleId)
        }

        recyclerView.adapter = scheduleAdapter


        setHasOptionsMenu(true) // Enable options menu in the fragment

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.todo_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                // Open the add schedule dialog
                showAddScheduleDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddScheduleDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_add_schedule, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Schedule")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                // Handle the add button click
                val title = dialogView.findViewById<EditText>(R.id.editTextTitle).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.editTextDescription).text.toString()
                val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
                val hour = timePicker.hour
                val minute = timePicker.minute

                // Convert the selected time to milliseconds
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val timeInMillis = calendar.timeInMillis
                // Convert milliseconds to hours
                val timeInHours = TimeUnit.MILLISECONDS.toHours(timeInMillis)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = timeFormat.format(timeInMillis)

                // Create a new ScheduleItem object with a unique ID
                val newSchedule = ScheduleItem(scheduleList.size, title, description, timeInHours, isOn = true, timeInMillis)


                // Add the new schedule to the list
                scheduleList.add(newSchedule)

                // Save the updated schedule list to local storage
                saveScheduleListToLocalStorage()

                // Notify the adapter about the new item
                scheduleAdapter.notifyItemInserted(scheduleList.size - 1)

                // Cancel the alarm (in case it was already set)
                cancelAlarm(newSchedule)

                // Set the alarm immediately after creating the schedule
                setAlarm(newSchedule)

                // Show a toast message to inform the user
                Toast.makeText(requireContext(), "Schedule added successfully", Toast.LENGTH_SHORT).show()
                Log.d("ToDoFragment", "editSchedule: timeInMillis -> {${timeInMillis} equal to {$time}}")

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle the cancel button click
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }




    private fun toggleSchedule(position: Int) {
        val schedule = scheduleList[position]
        setAlarm(schedule)
        schedule.isOn = !schedule.isOn
        scheduleAdapter.notifyItemChanged(position)

        if (schedule.isOn) {
            setAlarm(schedule) // Set alarm if the schedule is turned on
        } else {
            cancelAlarm(schedule) // Cancel alarm if the schedule is turned off
        }
    }

    private fun editSchedule(position: Int) {
        val schedule = scheduleList[position]

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_add_schedule, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Schedule")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                // Handle the save button click
                val title = dialogView.findViewById<EditText>(R.id.editTextTitle).text.toString()
                val description = dialogView.findViewById<EditText>(R.id.editTextDescription).text.toString()
                val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
                val hour = timePicker.hour
                val minute = timePicker.minute

                // Create a calendar instance and set the selected time
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = schedule.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val timeInMillis = calendar.timeInMillis
                val timeInHours = TimeUnit.MILLISECONDS.toHours(timeInMillis)

                // Cancel the alarm (in case it was already set)
                cancelAlarm(schedule)

                // Update the schedule with the new values
                schedule.title = title
                schedule.description = description
                schedule.time = timeInHours
                schedule.timeInMillis = timeInMillis

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = timeFormat.format(schedule.timeInMillis)

                // Set the alarm immediately after updating the schedule
                setAlarm(schedule)

                // Notify the adapter about the updated item
                scheduleAdapter.notifyItemChanged(position)

                // Show a toast message to inform the user
                Toast.makeText(requireContext(), "Schedule updated successfully", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "editSchedule: timeInMillis -> {${schedule.timeInMillis} equal to {$time}}")

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle the cancel button click
                dialog.dismiss()
            }
            .create()

        dialogView.findViewById<EditText>(R.id.editTextTitle).setText(schedule.title)
        dialogView.findViewById<EditText>(R.id.editTextDescription).setText(schedule.description)
        dialogView.findViewById<TimePicker>(R.id.timePicker).apply {
            setIs24HourView(true)

            // Extract the hour and minute values from the existing schedule's timeInMillis
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = schedule.timeInMillis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            currentHour = hour
            currentMinute = minute
        }

        dialog.show()
    }

    private fun deleteSchedule(scheduleId: Int) {
        val schedule = scheduleList.find { it.id == scheduleId }
        if (schedule != null) {
            val position = scheduleList.indexOf(schedule)
            scheduleAdapter.notifyItemRemoved(position)

            cancelAlarm(schedule) // Cancel the alarm associated with the deleted schedule

            scheduleList.remove(schedule)

            if (scheduleList.isEmpty()) {
                // Handle the case when the list is empty
                handleEmptyList()
            } else {
                // Save the updated schedule list to local storage
                saveScheduleListToLocalStorage()
            }

            // Delay the removal of the item from the list to prevent the IndexOutOfBoundsException
            val handler = Handler()
            handler.postDelayed({
                scheduleList.remove(schedule)
                scheduleAdapter.notifyItemRemoved(position)
            }, 1000) // Adjust the delay time as needed
        } else {
            Toast.makeText(requireContext(), "Schedule not found", Toast.LENGTH_SHORT).show()
        }
    }



    private fun handleEmptyList() {
        // Perform any necessary cleanup when the list is empty
        // For example, cancel all remaining alarms or perform other actions
        // specific to your use case
        // ...
        // Save the updated empty schedule list to local storage
        saveScheduleListToLocalStorage()
    }




    private fun setAlarm(schedule: ScheduleItem) {
        Log.d("Alarm", "Setting alarm for schedule ID: ${schedule.id}, Time: ${schedule.timeInMillis}")

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("scheduleId", schedule.id) // Pass the schedule ID to the receiver
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            schedule.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, schedule.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, schedule.timeInMillis, pendingIntent)
        }
    }


    private fun cancelAlarm(schedule: ScheduleItem) {
        Log.d("Alarm", "Canceling alarm for schedule ID: ${schedule.id}")

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            schedule.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }


    private fun loadScheduleListFromLocalStorage(): MutableList<ScheduleItem> {
        val sharedPreferences = requireContext().getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
        val scheduleListJson = sharedPreferences.getString("schedule_list", null)

        return if (scheduleListJson != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<ScheduleItem>>() {}.type
            gson.fromJson(scheduleListJson, type)
        } else {
            mutableListOf()
        }
    }

    private fun saveScheduleListToLocalStorage() {
        val sharedPreferences = requireContext().getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val scheduleListJson = gson.toJson(scheduleList)

        editor.putString("schedule_list", scheduleListJson)
        editor.apply()
    }

}
