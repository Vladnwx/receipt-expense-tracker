package com.vladnwx.receiptexpensetracker.data.util

import org.json.JSONArray
import org.json.JSONObject

data class FnsJsonItem(
    val name: String,
    val price: Double,
    val quantity: Double,
    val sum: Double,
    val nds: Int = 0
)

data class FnsJsonData(
    val raw: String,
    val shareId: String? = null,
    val dateTime: String? = null,
    val totalSum: Double = 0.0,
    val fn: String? = null,
    val fd: String? = null,
    val fp: String? = null,
    val items: List<FnsJsonItem> = emptyList(),
    val retailerName: String? = null,
    val retailerInn: String? = null,
    val retailerAddress: String? = null,
    val retailPlace: String? = null,
    val operationType: Int = 1,
    val cashTotalSum: Double = 0.0,
    val ecashTotalSum: Double = 0.0
)

object FnsJsonParser {
    fun parse(jsonStr: String): FnsJsonData? {
        return try {
            val root = JSONObject(jsonStr)
            val receipt = extractReceipt(root) ?: return null

            val totalSum = receipt.optDouble("totalSum", 0.0) / 100.0
            val cashSum = receipt.optDouble("cashTotalSum", 0.0) / 100.0
            val ecashSum = receipt.optDouble("ecashTotalSum", 0.0) / 100.0

            val items = mutableListOf<FnsJsonItem>()
            val itemsArray = receipt.optJSONArray("items")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.getJSONObject(i)
                    items.add(FnsJsonItem(
                        name = item.optString("name", "").trim(),
                        price = item.optDouble("price", 0.0) / 100.0,
                        quantity = item.optDouble("quantity", 1.0),
                        sum = item.optDouble("sum", 0.0) / 100.0,
                        nds = item.optInt("nds", 0)
                    ))
                }
            }

            fun optStr(key: String): String? = receipt.optString(key, "").trim().ifBlank { null }

            val shareId = root.optString("_id", "").ifBlank { null }

            FnsJsonData(
                raw = jsonStr,
                shareId = shareId,
                dateTime = optStr("dateTime") ?: optStr("date"),
                totalSum = totalSum,
                fn = optStr("fiscalDriveNumber"),
                fd = optStr("fiscalDocumentNumber"),
                fp = optStr("fiscalSign"),
                items = items,
                retailerName = optStr("user"),
                retailerInn = optStr("userInn"),
                retailerAddress = optStr("retailPlaceAddress"),
                retailPlace = optStr("retailPlace"),
                operationType = receipt.optInt("operationType", 1),
                cashTotalSum = cashSum,
                ecashTotalSum = ecashSum
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractReceipt(root: JSONObject): JSONObject? {
        val ticket = root.optJSONObject("ticket")
        if (ticket != null) {
            val doc = ticket.optJSONObject("document")
            if (doc != null) {
                val receipt = doc.optJSONObject("receipt")
                if (receipt != null) return receipt
            }
        }
        val doc = root.optJSONObject("document")
        if (doc != null) {
            val receipt = doc.optJSONObject("receipt")
            if (receipt != null) return receipt
        }
        val receiptDirect = root.optJSONObject("receipt")
        if (receiptDirect != null) return receiptDirect
        return null
    }
}
