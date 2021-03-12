package com.alexeyyuditsky.criminalintent

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0
private const val DATE_FORMAT = "kk:mm, dd.MM.yyyy"
private const val REQUEST_CONTACT = 0
private const val REQUEST_CODE_PERMISSION_READ_CONTACTS = 0
private const val REQUEST_PHOTO = 2

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private lateinit var treeObserver: ViewTreeObserver
    private var viewWidth = 0
    private var viewHeight = 0

    private val crimeDetailsViewModel: CrimeDetailViewModel by lazy { ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailsViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // LayoutInflater и ViewGroup необходимы для заполнения макета фрагмента
        val view = inflater.inflate(R.layout.fragment_crime, container, false) // заполняем макет фрагмента; container - определяет родителя макета; false - указывает, нужно ли включать заполненный макет в родителя
        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        timeButton = view.findViewById(R.id.crime_time)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        reportButton = view.findViewById(R.id.crime_report)
        suspectButton = view.findViewById(R.id.crime_suspect)
        callSuspectButton = view.findViewById(R.id.call_suspect)
        photoButton = view.findViewById(R.id.crime_camera)
        photoView = view.findViewById(R.id.crime_photo)

        treeObserver = photoView.viewTreeObserver
        treeObserver.addOnGlobalLayoutListener {
            viewWidth = photoView.width
            viewHeight = photoView.height
        }

        return view // возвращаем заполненный макет фрагмента активности-хосту
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailsViewModel.crimeLiveData.observe(viewLifecycleOwner, { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailsViewModel.getPhotoFile(crime) // сохраняем местонахождение файла фотографии
                photoUri = FileProvider.getUriForFile(requireActivity(), "com.alexeyyuditsky.criminalintent.fileprovider", photoFile) // функция преобразует локальный путь к файлу в Uri, который видит приложение камеры
                updateUI()
            }
        })
    }

    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher { // создаём анонимный класс который реализует интерфейс слушателя TextWatcher
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // CharSequence? - это ввод пользователя; метод отслеживает изменения введенного текста в EditText
                crime.title = s.toString() // задаём заголовок Crime по строке из EditText
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        titleField.addTextChangedListener(titleWatcher) // назначение слушателя, который следит за введёным текстом в EditText

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked -> crime.isSolved = isChecked }  // назначение слушателя, который следит за CheckBox

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply { // создаём DatePickerFragment и передаём в него аргументы (дату преступления)
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) // назначаем CrimeFragment целевым фрагментов для DatePickerFragment, 2 параметр отвечает за код запроса, по коду запроса целевой фрагмент позднее может определить, какой фрагмент возвращает информацию
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE) // функция show() используется для добавления экземпляра DialogFragment во FragmentManager и вывода его на экран
            }
        } // фрагмент this@CrimeFragment необходим для вызова функции requireFragmentManager() из CrimeFragment, а не из DatePickerFragment. Он ссылается на DatePickerFragment внутри блока apply, поэтому необходимо указать this из внешней области видимости.

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply { // создаём TimePickerFragment и передаём в него аргументы (дату преступления)
                setTargetFragment(this@CrimeFragment, REQUEST_TIME) // назначаем CrimeFragment целевым фрагментов для TimePickerFragment, 2 параметр отвечает за код запроса, по коду запроса целевой фрагмент позднее может определить, какой фрагмент возвращает информацию
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME) // функция show() используется для добавления экземпляра DialogFragment во FragmentManager и вывода его на экран
            }
        }

        suspectButton.setOnClickListener {
            val permissionStatus = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) // метод проверяет предоставил ли пользователь нашему приложению определенное разрешение, т.е. передаётся это разрешение в метод и результат возвращается в виде: "PERMISSION_GRANTED" или "PERMISSION_DENIED"
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) { // ВЫПОЛЯНЕТСЯ КОГДА ПОЛЬЗОВАТЕЛЬ ПРЕДОСТАВИЛ РАЗРЕШЕНИЯ
                val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI) // создаём и передаём неявный интент через startActivityForResult() для открытия контактов. ContactsContract - контент провайдер для доступа к БД контактов. ContactsContract.Contacts.CONTENT_URI используется для обращения к БД контактов
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            } // открытие активности с контактами
            else {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_PERMISSION_READ_CONTACTS) // метод вызывает запрос на разрешения (окно где нужно нажать, разрешить\запретить }
            }
        }

        callSuspectButton.setOnClickListener {
            if (crime.suspect.isEmpty()) {
                Snackbar.make(view!!, R.string.snack_bar_select_suspect, Snackbar.LENGTH_LONG).apply { // вызывается когда пользователь при запросе разрешения нажал "отклонить", но не нажал "больше не спрашивать"
                    val textView: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
                    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setAction(R.string.snack_bar_select) { // слушатель для SnackBar который запрашивает разрешение
                        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_PERMISSION_READ_CONTACTS) // метод вызывает запрос на разрешения (окно где нужно нажать, разрешить\запретить }
                    }
                    show()
                }
            } else {
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${crime.phone}")
                    startActivity(this)
                }
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply { // создаём и передаём неявный интент через startActivity()
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
                Intent.createChooser(it, getString(R.string.send_report)).apply { startActivity(this) } // всегда предлагать пользователю выбор приложения способного обработать этот интент, вместо того, что пользователь может выбрать одно приложение и нажать кнопку, чтобы использовать его по умолчанию
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // интент на запуск камеры

            // код проверяет наличие приложения камеры, после чего корректно блокируем кнопку, если приложение не найдено
            /*val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) isEnabled = false*/

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities)
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // устанавливаем флаг Intent.FLAG_GRANT_WRITE_URI_PERMISSION для каждой activity, которую может обрабатывать интент cameraImage. Так мы предоставляем им всем разрешение на запись специально для этого Uri.
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener {
            PictureDialogFragment.newInstance(crime.photoFileName, photoFile).show(fragmentManager!!, null)
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailsViewModel.saveCrime(crime) // сохранение в БД преступления
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // отзыв разрешений URI поставщика контента
    }

    override fun onDateSelected(date: Date) { // реализация интерфейса обратного вызова созданного в DatePickerFragment для CrimeFragment (используется для передачи даты из DatePickerFragment в CrimeFragment)
        crime.date = date
        updateUI()
    }

    @SuppressLint("SimpleDateFormat") // аннотация для использования SimpleDateFormat
    private fun updateUI() { // обновление интерфейса пользователя
        titleField.setText(crime.title)
        timeButton.text = SimpleDateFormat("kk:mm").format(crime.date) // установка времени преступления с использованием шаблона "kk:mm"
        dateButton.text = SimpleDateFormat("dd.MM.yyyy").format(crime.date)

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() // пропуск анимации CheckBox'a
        }

        if (crime.suspect.isNotEmpty()) { // если есть подозреваемый у преступления, отобразить его в тексте кнопки "выбрать подозреваемого"
            suspectButton.text = crime.suspect
        }

        updatePhotoView(viewWidth, viewHeight) // функция загружает объект Bitmap в ImageView
    }

    private fun updatePhotoView(width: Int, height: Int) { // функция загружает объект Bitmap в ImageView
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, width, height)
            photoView.rotation = getCameraPhotoOrientation(photoFile.path).toFloat()
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    private fun getCameraPhotoOrientation(imagePath: String?): Int { // метод переворачивает картинку, если она неправильно перевернута
        return when (ExifInterface(imagePath!!).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 90
            else -> 0
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // функция возвращает ответ при выходе с запущенной активности, которая запускалась через startActivityForResult
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFieldsName = arrayOf(ContactsContract.Contacts.DISPLAY_NAME) // массив для возврата столбца DISPLAY_NAME - имя контакта
                val queryFieldsId = arrayOf(ContactsContract.Contacts._ID) // массив для возврата столбца _ID, он будет использоваться для получения идентификатора подозреваемого
                val cursorName = requireActivity().contentResolver.query(contactUri!!, queryFieldsName, null, null, null) // наполение Cursor именами контактов. requireActivity() - тот же context только не возвращает null. contentResolver используется для получения Cursor. query - выполняет запрос к БД. contactUri говорит к какой БД обращаться. queryFieldsName говорит какие строки достать

                cursorName?.use {
                    if (it.count == 0) return // если курсор пуст, выйти
                    it.moveToFirst() // переходим к 1 строке курсора
                    val suspect = it.getString(0) // получаем значение 1 столбца 1 строки, эта строка будет именем подозреваемого
                    crime.suspect = suspect
                    suspectButton.text = suspect
                }

                val cursorId = requireActivity().contentResolver.query(contactUri, queryFieldsId, null, null, null) // создаём еще один курсор, чтобы получить идентификатор подозреваемого, для получение номера подозреваемого по идентификатору
                cursorId?.use {
                    if (it.count == 0) return
                    it.moveToFirst()
                    val contactId = it.getString(0) // здесь помещаем идентификатор подозреваемого в contactId, далее чтобы получить номер телефона
                    val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI // Это ресурс для получения номера телефона
                    val phoneNumberQueryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER) // phoneNumberQueryFields: список для возврата только столбца телефонного номера
                    val phoneWhereClause = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?" // phoneWhereClause: фильтр, объявляющий, какие строки возвращать
                    val phoneQueryParameters = arrayOf(contactId) // phoneQueryParameters заменяет вопросительный знак в phoneWhereClause
                    val phoneCursor = requireActivity().contentResolver.query(phoneURI, phoneNumberQueryFields, phoneWhereClause, phoneQueryParameters, null)

                    phoneCursor?.use { cursorPhone ->
                        cursorPhone.moveToFirst()
                        val phoneNumValue = cursorPhone.getString(0)
                        crime.phone = phoneNumValue // после получения номера телефона помещаем его в crime.phone
                    }
                    crimeDetailsViewModel.saveCrime(crime)
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // отзыв разрешений URI поставщика контента
                updatePhotoView(viewWidth, viewHeight) // функция загружает объект Bitmap в ImageView
            }
        }
    }

    private fun getCrimeReport(): String { // функция отвечает за формирование сообщения в неявном интенте
        val dateString = android.text.format.DateFormat.format(DATE_FORMAT, crime.date).toString() // формирования даты
        val solvedString = if (crime.isSolved) getString(R.string.crime_report_solved) else getString(R.string.crime_report_unsolved) // формирование раскрытости
        val suspect = if (crime.suspect.isBlank()) getString(R.string.crime_report_no_suspect) else getString(R.string.crime_report_suspect, crime.suspect) // формирование подозреваемого
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onTimeSelected(date: Date) { // реализация интерфейса обратного вызова созданного в DatePickerFragment для CrimeFragment (используется для передачи даты из DatePickerFragment в CrimeFragment)
        crime.date = date
        updateUI()
    }

    @SuppressLint("NewApi") // анотация относится к строчке textView.textAlignment, которая может вызываться в версии андройд 4.2 и выше
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) { // метод вызывается когда нажимается разрешить/запретить в окне о предоставлении разрешений
        when (requestCode) {
            REQUEST_CODE_PERMISSION_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // ВЫПОЛНЯЕТСЯ КОГДА РАЗРЕШЕНИЯ ПОЛУЧЕНЫ
                    val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI) // создаём и передаём неявный интент через startActivityForResult() для открытия контактов
                    startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                } else { // ВЫПОЛНЯЕТСЯ КОГДА РАЗРЕШЕНИЯ НЕ ПОЛУЧЕНЫ
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) { // вызывается когда пользователь при запросе разрешения нажал "отклонить" и нажал "больше не спрашивать"
                        Snackbar.make(view!!, R.string.snack_bar_setting_app, Snackbar.LENGTH_LONG).apply { // выведем пользователю SnackBar, который предложит пользователю предоставить все разрешения в настройках приложения
                            val textView: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
                            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            setAction(R.string.snack_bar_setting) { // слушатель для SnackBar который переходит к активности с настройками приложением (для включения разрешения после нажатия "больше не спрашивать")
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { // интент который позволяет открыть активити с настройками приложения где можно предоставить разрешения
                                    data = Uri.fromParts("package", context.packageName, null) // передача интенту данных о нашем приложении для открытия активити с настройками приложения где можно предоставить разрешения
                                    startActivity(this)
                                }
                            }
                            show()
                        }
                        return
                    }

                    Snackbar.make(view!!, R.string.snack_bar_permission, Snackbar.LENGTH_LONG).apply { // вызывается когда пользователь при запросе разрешения нажал "отклонить", но не нажал "больше не спрашивать"
                        val textView: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
                        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setAction(R.string.snack_bar_allow) { // слушатель для SnackBar который запрашивает разрешение
                            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_PERMISSION_READ_CONTACTS) // метод вызывает запрос на разрешения (окно где нужно нажать, разрешить\запретить }
                        }
                        show()
                    }
                    return
                }
            }
        }
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment { // функция получает UUID
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            } // создает пакет аргументов
            return CrimeFragment().apply {
                arguments = args
            } // создает экземпляр фрагмента, а затем присоединяет аргументы к фрагменту
        }
    }
}