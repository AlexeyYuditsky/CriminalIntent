package com.alexeyyuditsky.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alexeyyuditsky.criminalintent.Crime
import java.util.*

@Dao // аннотация сообщает Room, что CrimeDao — это один из ваших объектов доступа к данным. Когда вы прикрепляете CrimeDao к вашему классу базы данных, Room будет генерировать реализации функций, которые вы добавляете к этому интерфейсу.
interface CrimeDao {
    @Query("SELECT * FROM crime") // аннотация указывает, что getCrimes() и getCrime(UUID) предназначены для извлечения информации из базы данных, а не вставки, обновления или удаления элементов из базы данных. Возвращаемый тип каждой функции запроса в интерфейсе DAO отражает тип результата запроса. Аннотация @Query в качестве входных данных ожидает строку, содержащую команду SQL
    fun getCrimes(): LiveData<List<Crime>> // оборачиваем объект в LiveData, чтобы иметь возможность передачи этих данных между потоками (Возвращая экземпляр LiveData из нашего класса DAO, мы запускаем запрос в фоновом потоке. Когда запрос завершается, объект LiveData будет обрабатывать отправку данных преступлений в основной поток и сообщать о любых наблюдателях)

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}