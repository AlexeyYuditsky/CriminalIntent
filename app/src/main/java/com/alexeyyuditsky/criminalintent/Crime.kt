package com.alexeyyuditsky.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity // аннотация определаят объект Crime сущностью Room, а также указывает, что класс определяет структуру таблицы или набора таблиц в базе данных, в этом случае каждая строка в таблице будет представлять собой отдельные преступления. Каждое свойство, определенное в классе, превратится в столбец в таблице, при этом имя свойства станет именем столбца. В нашем случае у таблицы будет четыре столбца: id, title, date и isSolved.
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(), // аннотация указывает какой столбец в базе данных является первичным ключом (Первичный ключ в базе данных — это такой столбец, который содержит данные, уникальные для каждой записи или строки. Такой столбец можно использовать для вызова отдельных записей.)
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = "",
    var phone: String = ""
) {
    val photoFileName
        get() = "IMG_$id.jpg" // свойство для получения названия фото
}