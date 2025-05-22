package com.robert.spring_boot_crash_course.database.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class User(
    val email: String,
    val password: String,
    @Id val id: ObjectId = ObjectId(),
)