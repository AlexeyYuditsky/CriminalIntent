package com.alexeyyuditsky.criminalintent

import androidx.lifecycle.ViewModel
import com.alexeyyuditsky.criminalintent.database.CrimeRepository

class CrimeListViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get() // получаем экземпляр репозитория БД
    val crimeListLiveData = crimeRepository.getCrimes() // получаем список всех преступлений из репозитория БД объектом LiveData

    fun addCrime(crime: Crime) { // метод вызывается после нажатия "добавить преступление"
        crimeRepository.addCrime(crime)
    }
}