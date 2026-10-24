package com.example.data

import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    fun getClientById(id: Long): Flow<ClientEntity?> = clientDao.getClientById(id)

    fun getClientHistory(name: String, phone: String): Flow<List<ClientEntity>> = clientDao.getClientHistory(name, phone)

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchClients(query)

    suspend fun insertClient(client: ClientEntity): Long = clientDao.insertClient(client)

    suspend fun updateClient(client: ClientEntity) = clientDao.updateClient(client)

    suspend fun deleteClient(client: ClientEntity) = clientDao.deleteClient(client)

    suspend fun deleteClientById(id: Long) = clientDao.deleteClientById(id)
}
