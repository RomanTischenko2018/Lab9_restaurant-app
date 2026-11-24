package com.example.restaurant.data.local

import com.example.restaurant.domain.model.Categories

/** Начальное меню: заполняет БД при первом запуске, чтобы приложение работало без интернета. */
object SeedData {
    val dishes: List<DishEntity> = listOf(
        DishEntity(name = "Салат «Цезарь»", description = "Курица, романо, пармезан, сухарики", price = 380, category = Categories.STARTERS),
        DishEntity(name = "Оливье", description = "Классический салат с индейкой", price = 290, category = Categories.STARTERS),
        DishEntity(name = "Брускетта с томатами", description = "Чиабатта, томаты, базилик", price = 260, category = Categories.STARTERS),
        DishEntity(name = "Жульен с грибами", description = "Шампиньоны под сырной шапкой", price = 340, category = Categories.STARTERS),
        DishEntity(name = "Борщ со сметаной", description = "Говядина, свёкла, чесночные пампушки", price = 320, category = Categories.MAINS),
        DishEntity(name = "Стейк рибай", description = "Мраморная говядина, соус демиглас", price = 1450, category = Categories.MAINS),
        DishEntity(name = "Пельмени домашние", description = "Со сметаной и зеленью", price = 380, category = Categories.MAINS),
        DishEntity(name = "Паста карбонара", description = "Бекон, желток, пармезан", price = 420, category = Categories.MAINS),
        DishEntity(name = "Котлета по-киевски", description = "С картофельным пюре", price = 460, category = Categories.MAINS),
        DishEntity(name = "Тирамису", description = "Маскарпоне, савоярди, кофе", price = 290, category = Categories.DESSERTS),
        DishEntity(name = "Чизкейк Нью-Йорк", description = "С ягодным соусом", price = 310, category = Categories.DESSERTS),
        DishEntity(name = "Медовик", description = "Домашний, со сметанным кремом", price = 240, category = Categories.DESSERTS),
        DishEntity(name = "Морс клюквенный", description = "0,3 л", price = 150, category = Categories.DRINKS),
        DishEntity(name = "Чай чёрный", description = "Чайник 0,5 л", price = 120, category = Categories.DRINKS),
        DishEntity(name = "Кофе американо", description = "200 мл", price = 140, category = Categories.DRINKS),
        DishEntity(name = "Лимонад домашний", description = "Лимон, мята, 0,4 л", price = 200, category = Categories.DRINKS)
    )
}
