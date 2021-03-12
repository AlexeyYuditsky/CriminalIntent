package com.alexeyyuditsky.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.alexeyyuditsky.criminalintent.database.CrimeRepository
import java.io.File
import java.util.*

class CrimeDetailViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get() // получаем объект репозитория
    private val crimeIdLiveData = MutableLiveData<UUID>() // хранит идентификатор отображаемого в данный момент преступления (или выводимого на отображение) фрагментом CrimeFragment. При первом создании CrimeDetailViewModel идентификатор преступления не устанавливается.

    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) { crimeId -> crimeRepository.getCrime(crimeId) } // функция преобразования(Transformations) возвращает новый объект LiveData, который мы называем результатом преобразования, значение которого обновляется каждый раз, когда изменяется значение триггерного объекта crimeIdLiveData

    fun loadCrime(crimeId: UUID) { // загрузка преступления
        crimeIdLiveData.value = crimeId // свойство value объекта LiveData, возвращаемое из функции отображения, используется для установки свойства value для результата преобразования
    }

    fun saveCrime(crime: Crime) { // сохранение в БД преступления
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File { // выдаёт путь файла изображения для CrimeFragment
        return crimeRepository.getPhotoFile(crime)
    }
}