package com.example.restaurant.data

import com.example.restaurant.data.remote.MealDto
import com.example.restaurant.data.remote.mapCategory
import com.example.restaurant.data.remote.priceFor
import com.example.restaurant.data.remote.toDishEntity
import com.example.restaurant.domain.model.Categories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealMapperTest {

    @Test
    fun `категории сервера сопоставляются с категориями меню`() {
        assertEquals(Categories.DESSERTS, mapCategory("Dessert"))
        assertEquals(Categories.STARTERS, mapCategory("Starter"))
        assertEquals(Categories.MAINS, mapCategory("Seafood"))
        assertEquals(Categories.MAINS, mapCategory(null))
    }

    @Test
    fun `цена вычисляется из идентификатора`() {
        assertEquals(340, priceFor(52772)) // 52772 % 15 == 2 -> 300 + 2 * 20
    }

    @Test
    fun `DTO преобразуется в сущность`() {
        val entity = MealDto("52772", "Teriyaki Chicken", "Chicken", "Japanese").toDishEntity()!!
        assertEquals("Teriyaki Chicken", entity.name)
        assertEquals("Кухня: Japanese", entity.description)
        assertEquals(52772, entity.remoteId)
    }

    @Test
    fun `нечисловой идентификатор отбрасывается`() {
        assertNull(MealDto("abc", "X", null, null).toDishEntity())
    }
}
