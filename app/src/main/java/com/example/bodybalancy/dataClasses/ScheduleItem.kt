package com.example.bodybalancy.dataClasses

data class ScheduleItem(
    val id: Int,
    var title: String,
    var description: String,
    var time: Long,
    var isOn: Boolean,
    var timeInMillis: Long
)

