package com.alexeyyuditsky.criminalintent

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        // добавляем UI-фрагмент CrimeFragment в MainActivity
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) // запрашиваем экземпляр CrimeListFragment (с идентификатором контейнерного представления R.id.fragment_container) от FragmentManager. Если этот фрагмент уже находится в списке, FragmentManager возвращает его
        if (currentFragment == null) {
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager
                .beginTransaction() // создание транзакции фрагмента
                .add(R.id.fragment_container, fragment) // включиние операции add в транзакцию фрагмента (сообщает FragmentManager, где в макете MainActivity должен находиться макет фрагмента)
                .commit() // закрепление транзакции фрагмента
        }
    }

    override fun onCrimeSelected(crimeId: UUID) { // реализация метода интерфейса CrimeListFragment.Callbacks (метод размещает CrimeFragment в активности-хосте)
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("CrimeListFragment") // позволяет использовать системную кнопку назад (возвращает на предыдущую транзакцию, а именно CrimeListFragment)
            .commit()
    }
}