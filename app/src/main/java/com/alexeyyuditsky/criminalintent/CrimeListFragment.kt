package com.alexeyyuditsky.criminalintent

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList()) // так как фрагмент будет ждать результатов из базы данных, прежде чем он сможет заполнить утилизатор (recycler view) преступлениями, инициализируем адаптер утилизатора пустым списком преступлений
    private val crimeListViewModel: CrimeListViewModel by lazy { ViewModelProviders.of(this).get(CrimeListViewModel::class.java) } // ViewModelProviders создаёт (если фрагмент был уничтожен пользователем) или возвращает (если фрагмент был уничтожен системой) ViewModel привязанную к фрагменту

    override fun onAttach(context: Context) { // функция вызывается, когда фрагмент прикрепляется к activity
        super.onAttach(context)
        callbacks = context as Callbacks? // приводим аргумент context который является экзмепляром activity(потому что фрагмент CrimeListFragment размещается в activity) к типу интерфейса Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // метод используется когда мы хотим указать, что вызов onCreateOptionsMenu() должен получить фрагмент, а не activity(по умолчанию)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // LayoutInflater и ViewGroup необходимы для заполнения макета фрагмента
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)  // заполняем макет фрагмента; container - определяет родителя макета; false - указывает, нужно ли включать заполненный макет в родителя
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context) // RecyclerView не отображает элементы на самом экране. Он передает эту задачу объекту LayoutManager. LayoutManager располагает каждый элемент, а также определяет, как работает прокрутка. LinearLayoutManager - позиционирует элементы в списке по вертикали
        crimeRecyclerView.adapter = adapter  // так как фрагмент будет ждать результатов из базы данных, прежде чем он сможет заполнить утилизатор (recycler view) преступлениями, инициализируем адаптер утилизатора пустым списком преступлений

        return view // возвращаем заполненный макет фрагмента активности-хосту
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // (благодаря этому методу список преступлений мы получаем только когда представление в должном состоянии) метод вызывается после возврата onCreateView, давая понять, что иерархия представления фрагмента находится на месте. Мы наблюдали за LiveData от onViewCreated(...), чтобы убедиться, что виджет готов к отображению данных о преступлении
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner, { crimes -> // Функция LiveData.observe(LifecycleOwner, Observer) используется для регистрации наблюдателя за экземпляром LiveData и связи наблюдения с жизненным циклом другого компонента (viewLifecycleOwner - это объект который отвечает за жизненный цикл View). Второй параметр функции observe(...) — это реализация Observer. Этот объект отвечает за реакцию на новые данные из LiveData. В этом случае блок кода наблюдателя выполняется всякий раз, когда обновляется список в LiveData. Наблюдатель получает список преступлений из LiveData и печатает сообщение журнала, если свойство не равно нулю.
            crimes?.let {
                updateUI(crimes)
            }
        })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null // устанавливаем null, так как в дальнейшем мы не сможем получить доступ к activity или рассчитывать на то, что она будет продолжать существовать
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) { // метод используется для заполнения Menu панели инструментов
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu) // вызов inflate() заполняет экземпляр Menu командами, определенными в файле "R.menu.fragment_crime_list"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // метод вызывается, когда происходит нажатие по элементу меню
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime() // создаём новый объект преступления
                crimeListViewModel.addCrime(crime) // добавляем его в БД
                callbacks?.onCrimeSelected(crime.id) // уведомляем родительскую activity о том, что запрошено добавление нового преступления
                true // если вернуть false обработка меню будет продолжена вызовом функции onOptionsItemSelected(MenuItem) из хост-activity (или, если activity содержит другие фрагменты, на этих фрагментах будет вызвана функция onOptionsItemSelected)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener { // RecyclerView ожидает, что элемент представления будет обернут в экземпляр ViewHolder. ViewHolder хранит ссылку на представление элемента(itemView). Определяем контейнер для представления, добавив в CrimeListFragment внутренний класс CrimeHolder, который расширяется от RecyclerView.ViewHolder
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title) // находим название и ниже дату в иерархии itemView(это весь view строки с двумя представлениями TextView), когда экземпляр впервые создается
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        private val emptyList: TextView = itemView.findViewById(R.id.empty_crime_list)
        private val newCrimeButton: Button = itemView.findViewById(R.id.new_crime_button)

        init { // назначение слушателя для объекта CrimeHolder
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SimpleDateFormat")
        fun bind(crime: Crime) { // метод вызывается всякий раз, когда RecyclerView запрашивает привязку CrimeHolder к конкретному преступлению и происходит заполнение виджетов данными преступлений
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = SimpleDateFormat("kk:mm   dd.MM.yyyy").format(this.crime.date)
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        fun bind() {
            titleTextView.visibility = View.GONE
            dateTextView.visibility = View.GONE
            solvedImageView.visibility = View.GONE
            emptyList.visibility = View.VISIBLE
            newCrimeButton.visibility = View.VISIBLE
            newCrimeButton.setOnClickListener {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
            }
        }

        override fun onClick(v: View?) { // слушатель нажатий объектов CrimeHolder
            callbacks?.onCrimeSelected(crime.id) // при нажатии на элемент списка, метод onCrimeSelected() вызывается объектом MainActivity(хост-активити) благодаря интерфейсу
        }
    }

    private inner class EmptyCrimeAdapter : RecyclerView.Adapter<CrimeHolder>() { // адаптер для отображения информации при пустом списке преступлений
        override fun getItemCount(): Int {
            return 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind()
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() { // адаптер выполняет следующие функции: создание необходимых ViewHolder по запросу; связывание ViewHolder с данными из модельного слоя.
        override fun getItemCount() = crimes.size // когда утилизатору нужно знать, сколько элементов в наборе данных поддерживают его (например, когда он впервые создается), он будет просить свой адаптер вызвать Adapter.getItemCount(). Функция getItemCount()возвращает количество элементов в списке преступлений, отвечая на запрос утилизатора.

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder { // метод отвечает за создание представления на дисплее, оборачивает его в холдер и возвращает результат. В этом случае мы наполняем list_item_view.xml и передаем полученное представление в новый экземпляр CrimeHolder
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) { // метод отвечает за заполнение данного холдера holder преступлением из данной позиции position.
            val crime = crimes[position]
            holder.bind(crime) // заполнение виджетов CrimeHolder данными преступлений
        }

    }

    private fun updateUI(crimes: List<Crime>) { // настраивает интерфейс CrimeListFragment
        if (crimes.isEmpty()) {
            val adapter = EmptyCrimeAdapter()
            crimeRecyclerView.adapter = adapter
        } else {
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
        }
    }

    companion object {
        fun newInstance() = CrimeListFragment()
    }
}