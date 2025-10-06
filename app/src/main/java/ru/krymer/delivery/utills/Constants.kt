package ru.krymer.delivery.utills

class Constants {

    object KEYS {
        const val FILTER = "filter"
        const val SORT = "sort"
        const val SETTINGS = "settings"
        const val AUTH = "auth"
        const val FONT_SIZE = "fSize"
    }

    object PatternDate {
        const val DEFAULT = "dd.MM.yyyy"
        const val FULL = "dd-MM-yyyy\nHH:mm:ss"
    }

    object UserStatus {
        const val OFFLINE = "offline"
        const val ONLINE = "online"
    }

    object HttpRequestKeys {
        const val ID = "id"
        const val ID_CLIENT = "idClient"
        const val ID_FACTORY = "idFactory"
        const val ID_ROUTE = "idRoute"
        const val ID_TRIP = "idTrip"
        const val ID_SHOP = "idShop"
        const val LAST_DATE = "lastDate"
        const val LIMIT = "limit"
        const val TOKEN_TITLE = "Authorization"
    }

    object SORT {
        const val ASC = "asc"
        const val DESC = "desc"
    }

    object PAY {
        const val CASH = "cash"
        const val NO_CASH = "noCash"
        const val ANOTHER = "another"
        const val RU_CASH = "Нал"
        const val RU_NO_CASH = "Без/нал"
        const val RU_ANOTHER = "Смешаный"
    }

    object TOKEN {
        const val ACCESS = "access"
        const val TOKEN_TYPE = "Bearer "
    }

    object Role {
        const val ADMIN = "admin"
        const val USER = "user"
        const val MODERATOR = "moderator"
        const val SYSTEM = "system"
    }

    object EMPTY {
        const val EMPTY_LIST = "Список пуст! Вам нужно добавить"
    }

    object ERROR {
        const val SERVER_ERROR_RESPONSE = "Ошибка при получении ответа от сервера. Повторите снова!"
        const val ERROR = "Непредвиденная ошибка!"
        const val AGAIN = "Ошибка! Попробуйте снова!"
        const val RESRTRAINT = "Ограничено!"
        const val GENERAL_ERROR = "Что-то пошло не так!"
        const val USER_BANNED = "Доступ ограничен! Ваш профиль заблокирован!"
        const val MISSING_CORDS = "Гео-точка отсутствует!"
        const val LIST_EMPTY = "Список пуст!"
        const val AUTH = "Требуется повторная авторизация!"
        const val AUTH_SIGN = "Ошибка авторизации!"
        const val NOT_FOUND = "Данные не найдены!"
        const val TIMEOUT = "Время исполнения запроса истекло!"
        const val CANCEL_OPERATION = "Вызов операции отменен!"
    }
}