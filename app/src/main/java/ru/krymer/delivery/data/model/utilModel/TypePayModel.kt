package ru.krymer.delivery.data.model.utilModel

import ru.krymer.delivery.utills.Constants

enum class TypePayModel {
    CASH, NO_CASH, ANOTHER
}

fun String.getTypePayByString(): TypePayModel {
    return when (this) {
        Constants.PAY.CASH -> TypePayModel.CASH
        Constants.PAY.NO_CASH -> TypePayModel.NO_CASH
        Constants.PAY.ANOTHER -> TypePayModel.ANOTHER
        else -> TypePayModel.CASH
    }
}

fun TypePayModel.getStringByTypePay(): String {
    return when (this) {
        TypePayModel.CASH -> Constants.PAY.CASH
        TypePayModel.NO_CASH -> Constants.PAY.NO_CASH
        TypePayModel.ANOTHER -> Constants.PAY.ANOTHER
    }
}

fun TypePayModel.getRuStringByTypePay(): String {
    return when (this) {
        TypePayModel.CASH -> Constants.PAY.RU_CASH
        TypePayModel.NO_CASH -> Constants.PAY.RU_NO_CASH
        TypePayModel.ANOTHER -> Constants.PAY.RU_ANOTHER
    }
}