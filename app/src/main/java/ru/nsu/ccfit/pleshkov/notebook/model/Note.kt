package ru.nsu.ccfit.pleshkov.notebook.model

data class Note(
        val title: String,
        val text: String,
        val timeCreated: Long,
        val timeToDo: Long
)