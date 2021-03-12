package com.alexeyyuditsky.criminalintent.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.alexeyyuditsky.criminalintent.Crime
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) { // синглтон, который используется как класс репозитория, который инкапсулирует логику для доступа к данным из одного источника или совокупности источников. Он определяет, как захватывать и хранить определенный набор данных — локально, в базе данных или с удаленного сервера. Ваш код UI будет запрашивать все данные из репозитория, потому что интерфейсу неважно, как фактически хранятся или извлекаются данные. Это детали реализации самого репозитория. В нашем случае репозиторий будет обрабатывать только выборку данных из базы. Задача класса выдавать данные о преступлении и давать возможность легко передавать эти данные между классами контроллера

    private val database: CrimeDatabase = Room.databaseBuilder(context.applicationContext, CrimeDatabase::class.java, DATABASE_NAME).addMigrations(migration_1_2, migration_2_3).build() // свойство для хранения ссылки на БД (Функция Room.databaseBuilder() создает конкретную реализацию вашего абстрактного класса CrimeDatabase с использованием трех параметров. Сначала ему нужен объект Context, так как база данных обращается к файловой системе. Контекст приложения нужно передавать, так как синглтон, скорее всего, существует дольше, чем любой из ваших классов activity, Второй параметр — это класс базы данных, которую Room должен создать. Третий — имя файла базы данных, которую создаст Room. Нужно использовать приватную константу, определенную в том же файле, поскольку никакие другие компоненты не должны получать к ней доступ). addMigrations(migration_1_2) - производим передачу свойства migration_1_2 в БД для перевода БД из версии 1 в версию 2
    private val crimeDao = database.crimeDao() // свойство для хранения ссылки на объект интерфейса CrimeDao
    private val executor = Executors.newSingleThreadExecutor() // функция возвращает экземпляр исполнителя, который указывает на новый поток. Таким образом, любая работа, которую мы выполняем с исполнителем, будет происходить вне основного потока.
    private val filesDir = context.applicationContext.filesDir //

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes() // обращаемся к реализации запроса в CrimeDao, это помогает сохранить код репозитория простым и коротким (благодаря LiveData метод работает в фоновом потоке). Когда запрос завершается, объект LiveData будет обрабатывать отправку данных преступлений в основной поток и сообщать о любых наблюдателях
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)
    fun updateCrime(crime: Crime) {
        executor.execute { crimeDao.updateCrime(crime) } //как updateCrime(), так и addCrime() оборачивают вызовы в DAO внутри блока execute {}. Он выталкивает эти операции из основного потока, чтобы не блокировать работу пользовательского интерфейса
    }

    fun addCrime(crime: Crime) {
        executor.execute { crimeDao.addCrime(crime) }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName) // функция возвращает объект File, указывающий на место где хранится файл

    companion object {
        private var INSTANCE: CrimeRepository? = null // синглтон, экземпляр репозитория БД (функция: выдает данные о преступлении и дает возможность легко передавать эти данные между классами контроллера)

        fun initialize(context: Context) { // инициализация экземпляра репозитория БД
            if (INSTANCE == null)
                INSTANCE = CrimeRepository(context)
        }

        fun get(): CrimeRepository { // получение экземпляра репозитория БД
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}