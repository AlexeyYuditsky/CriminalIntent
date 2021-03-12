package com.alexeyyuditsky.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexeyyuditsky.criminalintent.Crime

@Database(entities = [Crime::class], version = 3) // аннотация сообщает Room о том, что этот класс представляет собой базу данных в приложении. Самой аннотации требуется два параметра. Первый параметр — это список классов-сущностей, который сообщает Room, какие использовать классы при создании и управлении таблицами для этой базы данных. В нашем случае мы передаем только класс Crime, так как это единственная сущность в приложении. Второй параметр — версия базы данных. При первом создании базы данных версия должна быть равна 1. При будущей разработке приложения вы можете добавлять новые сущности и новые свойства существующим сущностям. В этом случае вам нужно будет изменить список сущностей и увеличить версию базы данных, чтобы обозначить факт изменения
@TypeConverters(CrimeTypeConverters::class) // аннотация явно добавляет конвертеры(созданные в классе CrimeTypeConverters) к классу базы данных, чтобы использовать функции для преобразования типов
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao // подключение "объекта доступа к данным" для использования интерфейса CrimeDao
}

val migration_1_2 = object : Migration(1, 2) { // инструкция по обновлению базы данных с версии 1 на версию 2
    override fun migrate(database: SupportSQLiteDatabase) { // используем параметр базы данных для выполнения любых SQL-команд, необходимых для обновления таблиц
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspects TEXT NOT NULL DEFAULT ''") // дословно: изменить таблицу Crime, добавить столбец suspect, текстовый, не нулевой, инициализируемый значением ''
    }
}

val migration_2_3 = object : Migration(2, 3) { // инструкция по обновлению базы данных с версии 1 на версию 2
    override fun migrate(database: SupportSQLiteDatabase) { // используем параметр базы данных для выполнения любых SQL-команд, необходимых для обновления таблиц
        database.execSQL("ALTER TABLE Crime ADD COLUMN phone TEXT NOT NULL DEFAULT ''") // дословно: изменить таблицу Crime, добавить столбец suspect, текстовый, не нулевой, инициализируемый значением ''
    }
}
