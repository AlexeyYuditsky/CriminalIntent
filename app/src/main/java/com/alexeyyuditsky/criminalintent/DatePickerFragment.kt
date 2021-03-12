package com.alexeyyuditsky.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() { // не рекомендуется создавать класс для AlertDialog без управления им из FragmentManager, поэтому создаём наш подкласс от DialogFragment(), а также, если экземпляр DatePickerDialog упакован во фрагмент, после поворота диалоговое окно будет создано заново и появится на экране

    interface Callbacks { // интерфейс обратного вызова который реализуется фрагментом CrimeFragment
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog { // экзмепляр FragmentManager хост-activity вызывает эту функцию в процессе вывода DialogFragment на экран

        val calendar = Calendar.getInstance()

        val date = arguments?.getSerializable(ARG_DATE) as Date // извлекаем аргументы пакета для фрагмента с переданной датой из CrimeFragment
        calendar.time = date // устанавливаем переданую дату преступления из CrimeFragment

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day, hourOfDay, minute).time // сохраняем выбранную дату из виджета DatePickerDialog
            targetFragment?.let { (it as Callbacks).onDateSelected(resultDate) } // получаем целевой объект через targetFragment: CrimeFragment и приводим его к интерфейсу Callbacks, далее для приведенного к интерфейсу объекта(CrimeFragment типа Callbacks) вызываем реализованный метод onDateSelected() внутри CrimeFragment
        } // слушатель DatePickerDialog для отправки даты в CrimeFragment

        return DatePickerDialog(requireContext(), dateListener, initialYear, initialMonth, initialDay) // первый параметр — это контекстный объект, который необходим для доступа к необходимым ресурсам элемента. Второй параметр — слушатель выбранных дат в DatePickerDialog. Последние три параметра — это год, месяц и день, к которым должно быть инициализировано окно выбора даты
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment { // функция используется для создания аргументов для фрагмента DatePickerFragment, создания объекта DatePickerFragment и заполнения его пакетом аргументов
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}