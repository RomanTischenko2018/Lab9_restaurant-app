package com.example.restaurant.data.remote

import com.example.restaurant.data.local.DishEntity
import com.example.restaurant.domain.model.Categories
import retrofit2.http.GET
import retrofit2.http.Query

/** Ответ TheMealDB: при отсутствии результатов поле `meals` равно `null`. */
data class MealsResponse(val meals: List<MealDto>?)

/** Блюдо в формате TheMealDB (используются только нужные поля). */
data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?
)

/** REST-API каталога блюд (https://www.themealdb.com/api.php). */
interface MealApi {

    /** Блюда, названия которых начинаются с буквы [letter]. */
    @GET("search.php")
    suspend fun searchByLetter(@Query("f") letter: String): MealsResponse

    companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}

/** Сопоставляет английскую категорию сервера с категорией меню ресторана. */
fun mapCategory(apiCategory: String?): String = when (apiCategory) {
    "Dessert" -> Categories.DESSERTS
    "Starter", "Side", "Breakfast" -> Categories.STARTERS
    else -> Categories.MAINS
}

/** API не отдаёт цен, поэтому для демонстрации цена вычисляется из идентификатора. */
fun priceFor(remoteId: Int): Int = 300 + (remoteId % 15) * 20

/** DTO → сущность БД; `null`, если идентификатор не число. */
fun MealDto.toDishEntity(): DishEntity? {
    val remoteId = idMeal.toIntOrNull() ?: return null
    return DishEntity(
        name = strMeal,
        description = strArea?.let { "Кухня: $it" }.orEmpty(),
        price = priceFor(remoteId),
        category = mapCategory(strCategory),
        remoteId = remoteId
    )
}
