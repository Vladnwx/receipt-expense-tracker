package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    suspend fun getAll(): List<ContactEntity> = contactDao.getAll()
    suspend fun getById(id: Long): ContactEntity? = contactDao.getById(id)
    suspend fun search(query: String): List<ContactEntity> = contactDao.search(query)
    suspend fun save(entity: ContactEntity): Long = contactDao.insert(entity)
    suspend fun deleteById(id: Long) = contactDao.deleteById(id)
}
