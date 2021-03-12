package com.alexeyyuditsky.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {

    interface Callbacks { // интерфейс обратного вызова который реализуется фрагментом CrimeFragment
        fun onTimeSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()

        val date = arguments?.getSerializable(ARG_TIME) as Date // извлекаем аргументы пакета для фрагмента с переданной датой из CrimeFragment
        calendar.time = date // устанавливаем переданую дату преступления из CrimeFragment

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val dateListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
            val resultTime: Date = GregorianCalendar(initialYear, initialMonth, initialDay, hourOfDay, minute).time // сохраняем выбранную дату из виджета TimePickerDialog
            targetFragment?.let { (it as Callbacks).onTimeSelected(resultTime) } // получаем целевой объект через targetFragment: CrimeFragment и приводим его к интерфейсу Callbacks, далее для приведенного к интерфейсу объекта(CrimeFragment типа Callbacks) вызываем реализованный метод onDateSelected() внутри CrimeFragment
        } // слушатель TimePickerDialog для отправки даты в CrimeFragment

        return TimePickerDialog(requireContext(), dateListener, hourOfDay, minute, true)
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment { // функция используется для создания аргументов для фрагмента DatePickerFragment, создания объекта TimePickerFragment и заполнения его пакетом аргументов
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}