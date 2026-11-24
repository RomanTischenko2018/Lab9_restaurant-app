# Ресторан — мобильное приложение

Android-приложение для ресторана: меню, корзина и история заказов. Проект выполнен в рамках
лабораторной работы №9 («Итоговое занятие. Защита проекта») по дисциплине «Программирование
мобильных устройств», вариант 6 (собственный проект) на стеке **Android + Compose + Room + Retrofit**.

## Возможности

- **Меню:** блюда по категориям (закуски, основные блюда, десерты, напитки), поиск по названию, фильтр по категории.
- **Управление меню:** добавление, редактирование и удаление блюд.
- **Корзина:** добавление порций из меню, изменение количества, подсчёт суммы, значок с количеством на иконке корзины.
- **Заказы:** оформление заказа на номер столика, история заказов с составом и датой, удаление заказа.
- **Сервер:** кнопка «Обновить» подгружает дополнительные блюда из открытого API [TheMealDB](https://www.themealdb.com/api.php).
- **Без интернета:** начальное меню зашито в БД, приложение полностью работает офлайн; при ошибке сети показывается сообщение.
- Светлая и тёмная тема, динамические цвета Material You (Android 12+).

> Цены в TheMealDB отсутствуют, поэтому для блюд с сервера цена вычисляется из идентификатора (демонстрационное значение).

## Технологический стек

| Назначение | Технология |
|:---|:---|
| Язык | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + Clean Architecture (presentation / domain / data) |
| Состояние | `StateFlow`, `collectAsStateWithLifecycle` |
| Локальная БД | Room (SQLite): 4 таблицы, внешние ключи, транзакции, KSP |
| Сеть | Retrofit + OkHttp + Gson |
| Навигация | Navigation Compose |
| Асинхронность | Kotlin Coroutines + Flow |
| Тесты | JUnit 4 + kotlinx-coroutines-test |
| CI | GitHub Actions |

## Архитектура

```
Compose Screen ──события──▶ ViewModel ──▶ UseCase ──▶ Repository (interface, domain)
       ▲                        │                              │
       └── StateFlow<UiState> ──┘                              ▼
                                                   Repository Impl (data)
                                                    ├── Room: DishDao, CartDao, OrderDao
                                                    └── Retrofit: MealApi
```

**База данных:**

```
dishes (id, name, description, price, category, remoteId*)      * уникальный индекс
cart (dishId PK → dishes.id ON DELETE CASCADE, quantity)
orders (id, createdAt, tableNumber, total)
order_items (id, orderId → orders.id ON DELETE CASCADE, name, price, quantity)
```

Источник истины — Room (offline-first): UI подписан на `Flow`, поэтому любое изменение сразу
отображается. Оформление заказа выполняется **одной транзакцией**: снимок корзины → запись
заказа и позиций → очистка корзины. В позициях заказа название и цена копируются, поэтому
последующее изменение меню не меняет историю.

## Структура проекта

```
app/src/main/java/com/example/restaurant/
├── MainActivity.kt, RestaurantApplication.kt
├── di/AppContainer.kt                  # ручной DI-контейнер
├── domain/                             # модели, интерфейсы репозиториев, use case'ы
├── data/
│   ├── local/                          # Room: сущности, DAO, база, начальное меню
│   ├── remote/                         # Retrofit: MealApi, DTO, маппинг
│   └── repository/Repositories.kt
└── ui/
    ├── theme/, navigation/
    ├── menu/   cart/   orders/   dish/ # экран + ViewModel
app/src/test/                           # unit-тесты
```

## Установка и запуск

**Требования:** Android Studio Ladybug (2024.2) или новее, JDK 17, Android SDK 35,
эмулятор или устройство с Android 7.0+ (API 24).

1. Клонируйте репозиторий: `git clone <URL-вашего-репозитория>`
2. Откройте папку проекта в Android Studio и дождитесь Gradle Sync.
3. Выберите эмулятор/устройство и нажмите **Run ▶** (или `./gradlew installDebug`).

```bash
./gradlew testDebugUnitTest   # unit-тесты
./gradlew assembleDebug       # APK: app/build/outputs/apk/debug/
```

## Автор

Тищенко Роман Сергеевич · группа ИНС-б-о-23-1
