package com.alexeyyuditsky.criminalintent.database

import android.app.Application

class CriminalIntentApplication : Application() { // класс - экземпляр приложения (который создается, когда приложение запускается, и уничтожается, когда завершается процесс приложения) и нужен для инициализации репозитория БД в методе onCreate()

    override fun onCreate() { // метод вызывается когда приложение впервые загружается в память, это нужное место для инициализации репозитория БД
        super.onCreate()
        CrimeRepository.initialize(this) // инициализируем репозиторий БД, передав экземпляр приложения(this)
    }
}