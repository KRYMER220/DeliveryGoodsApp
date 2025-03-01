package ru.krymer.delivery.data.model.utilModel

data class MessageModel(val id: Long, val message: String, val type: TypeMessageModel)

enum class TypeMessageModel {
    INFO, ERROR, SUCCEED
}
