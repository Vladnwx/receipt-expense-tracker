package com.qrcode.scanner.domain.category

interface CategoryClassifier {

    suspend fun classify(name: String, price: Double, accountId: Long? = null): ClassifierResult

    suspend fun isAvailable(): Boolean

    suspend fun loadModel(): Boolean

    data class ClassifierResult(
        val categoryId: Long?,
        val categoryName: String?,
        val confidence: Float,
        val suggestions: List<Suggestion>
    )

    data class Suggestion(
        val categoryId: Long,
        val categoryName: String,
        val confidence: Float
    )
}
