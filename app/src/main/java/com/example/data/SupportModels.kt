package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

// --- SUPPORT TICKETS ---
@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketNumber: String,
    val customerId: Int? = null,
    val customerName: String? = null,
    val employeeId: Int? = null,
    val employeeName: String? = null,
    val systemType: String, // "INTERNAL" or "EXTERNAL"
    val subject: String,
    val category: String, // "SOFTWARE", "HARDWARE", "HR", "ACCOUNTS", "SPARE_PARTS", "GENERAL"
    val status: String, // "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"
    val priority: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val slaDeadline: Long, // Countdown limit timestamp
    val assignedToId: Int? = null,
    val assignedToName: String? = null,
    val escalatedToName: String? = null,
    val isEscalated: Boolean = false,
    val rating: Int = 0, // Customer feedback rating (1-5)
    val feedback: String = "" // Customer feedback text
)

// --- SUPPORT MESSAGES ---
@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketId: Int,
    val senderId: Int,
    val senderName: String,
    val senderRole: String, // "OWNER", "ADMIN", "STAFF", "CUSTOMER", "PUBLIC" etc.
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiResponse: Boolean = false
)

// --- KNOWLEDGE ARTICLES ---
@Entity(tableName = "knowledge_articles")
data class KnowledgeArticle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "Troubleshooting", "HR Rules", "Product Manual", "Payment Policy" etc.
    val systemType: String, // "INTERNAL", "EXTERNAL" or "BOTH"
    val createdBy: String,
    val viewsCount: Int = 0
)

// --- AI SUPPORT SESSIONS ---
@Entity(tableName = "ai_support_sessions")
data class AiSupportSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val systemType: String, // "INTERNAL" or "EXTERNAL"
    val createdAt: Long = System.currentTimeMillis()
)

// --- DAO FOR SUPPORT SYSTEM ---
@Dao
interface SupportDao {
    // Tickets
    @Query("SELECT * FROM support_tickets WHERE systemType = :systemType ORDER BY createdAt DESC")
    fun getTicketsBySystemFlow(systemType: String): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE customerId = :customerId AND systemType = 'EXTERNAL' ORDER BY createdAt DESC")
    fun getExternalTicketsByCustomerFlow(customerId: Int): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Int): SupportTicket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicket)

    @Delete
    suspend fun deleteTicket(ticket: SupportTicket)

    // Messages
    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getMessagesByTicketFlow(ticketId: Int): Flow<List<SupportMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SupportMessage): Long

    // Knowledge Articles
    @Query("SELECT * FROM knowledge_articles ORDER BY title ASC")
    fun getAllArticlesFlow(): Flow<List<KnowledgeArticle>>

    @Query("SELECT * FROM knowledge_articles WHERE systemType = :systemType OR systemType = 'BOTH' ORDER BY title ASC")
    fun getArticlesBySystemFlow(systemType: String): Flow<List<KnowledgeArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: KnowledgeArticle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<KnowledgeArticle>)

    @Update
    suspend fun updateArticle(article: KnowledgeArticle)

    @Delete
    suspend fun deleteArticle(article: KnowledgeArticle)
}
