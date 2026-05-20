package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val color: Int? = null,
    val isPredefined: Boolean = false,
    val isFamilyDefault: Boolean = false,
    val sortOrder: Int = 0
)
