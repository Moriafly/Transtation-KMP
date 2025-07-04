package com.funny.compose.ai.bean

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import com.funny.translation.helper.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * 可供选择的模型列表，包含模型名称、模型描述、模型价格（每千字符）
 * @constructor
 */
@Serializable
@Stable
@Keep
data class Model(
    @SerialName("chat_bot_id")
    val chatBotId: Int,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("cost_1k_tokens")
    val cost1kTokens: BigDecimal,
    val name: String,
    val description: String,
    val avatar: String = "",
    @SerialName("max_context_tokens")
    // 最大上下文长度，单位 token
    val maxContextTokens: Int,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int,
    @SerialName("token_counter_id")
    val tokenCounterId: String,
    @SerialName("input_file_types")
    val inputFileTypes: ModelFileTypes = ModelFileTypes.TEXT,
    @SerialName("base_timeout")
    val baseTimeout: Int = 60,
    @SerialName("per_char_timeout")
    val perCharTimeoutMillis: Int = 5,
    val tag: String = "",
    @SerialName("tag_desc")
    val tagDescription: String = ""
) {
    companion object {
        val Empty = Model(-1, BigDecimal.ZERO, "Test", "", "", 0, 0, "default", tag = "强")
    }

    val isFree = cost1kTokens.compareTo(BigDecimal.ZERO) == 0
}