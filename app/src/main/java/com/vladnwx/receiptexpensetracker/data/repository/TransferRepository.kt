package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.TransferDao
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val transferDao: TransferDao
) {
    suspend fun getAll(): List<TransferEntity> = transferDao.getAll()
    suspend fun getByAccountId(accountId: Long): List<TransferEntity> = transferDao.getByAccountId(accountId)
    suspend fun save(entity: TransferEntity): Long = transferDao.insert(entity)
    suspend fun deleteById(id: Long) = transferDao.deleteById(id)
}
