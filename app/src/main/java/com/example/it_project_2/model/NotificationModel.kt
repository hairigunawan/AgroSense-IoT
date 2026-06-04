package com.example.it_project_2.model

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // "success", "warning", "info"
    val timestamp: Long = System.currentTimeMillis()
)
