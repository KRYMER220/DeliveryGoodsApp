package ru.krymer.delivery.utills

class Constants {


    object MENU {
        const val ROUTE = "Маршруты"
        const val TRIP = "Рейсы"
        const val PRODUCT = "Товары"
        const val COURIER = "Курьеры"
        const val ANALITIC = "Аналитика"
    }

    object KEYS {
        const val COURIER_FILTER = "courier"
        const val FONT = "font"
        const val AUTH = "auth"
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
        const val EMPTY_NAME = "Введите название!"
        const val EMPTY_SALARY = "Введите зарплату!"
        const val EMPTY_DATA = "Данные отсутствуют!"
        const val EMPTY_PRICE = "Заполните цену!"
        const val EMPTY_STRING = ""
        const val EMPTY_FIELD = "Поле пусто!"
        const val EMPTY_PRICE_MILLAGE = "Введите цену километража!"
        const val EMPTY_USER_NAME = "Введите имя!"
        const val EMPTY_ARREARS = "Введите долг!"
        const val EMPTY_USER_PERCENT = "Введите процент курьера!"
        const val FIELD_IMPORTANT = "Обязательное поле! "
        const val EMPTY_LIST = "Список пуст! Вам нужно добавить "
    }

    object ADD {
        const val COURIER = "курьера, во вкладке Курьеры!"
        const val CLIENT = "клиента! В данный маршрут."
        const val ROUTE = "маршрут, во вкладке Маршруты!"
    }

    object ERROR {
        const val ERROR_NUMBER_INPUT = "Введите целое или дробное число!"
        const val SERVER_ERROR_RESPONSE = "Ошибка при получении ответа от сервера. Повторите снова!"
        const val ERROR = "Непредвиденная ошибка!"
        const val AGAIN = "Ошибка! Попробуйте снова!"
        const val RESRTRAINT = "Ограничено!"
        const val PHONE = "Не корректный номер телефона!"
        const val CORD = "Не корректные координаты!"
        const val EMAIL_INVALID = "Введен не корректный адрес электронной почты!"
        const val PASS_INVALID = "Пароль меньше 8 символов!"
        const val GENERAL_ERROR = "Что-то пошло не так!"
        const val ETHERNET = "Отсутствует интернет соединение! \nПовторите запрос!"
        const val USER_BANNED = "Доступ ограничен! Ваш профиль заблокирован!"
        const val MISSING_CORDS = "Гео-точка отсутствует!"
        const val LIST_EMPTY = "Список пуст!"
        const val AUTH = "Требуется повторная авторизация!"
        const val CANCEL_OPERATION = "Вызов операции отменен!"
    }

    object ACTIONS {
        const val EXIT = "Выход"
        const val INFO = "Инфо: "
        const val ERROR = "Ошибка: "
        const val SUCCEED = "Выполнено: "
        const val AGAIN = "Повторить"
        const val SAVE = "Сохранить"
        const val HIDE = "Скрыть"
        const val SHOW = "Показать"
    }
}