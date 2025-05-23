package ru.krymer.delivery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "failed")
data class FailedRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerializedName("api_type") val apiType: String,
    @SerializedName("endpoint") val endpoint: String,
    @SerializedName("method") val method: String,
    @SerializedName("url") val url: String,
    @SerializedName("params") val params: String,
    @SerializedName("body_json") val bodyJson: String?,
    @SerializedName("error_message") val errorMessage: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)