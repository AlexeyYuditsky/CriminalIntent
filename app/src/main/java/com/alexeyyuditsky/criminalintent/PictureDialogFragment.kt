package com.alexeyyuditsky.criminalintent

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

class PictureDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_picture_dialog, container, false)
        val imageView = view.findViewById(R.id.crime_picture) as ImageView

        val photoFileName = arguments?.getSerializable("PHOTO_URI") as String
        val photoFile = arguments?.getSerializable("PHOTO_PATH") as File
        imageView.rotation = getCameraPhotoOrientation(photoFile.path).toFloat()
        imageView.setImageBitmap(BitmapFactory.decodeFile(requireContext().filesDir.path + "/" + photoFileName))

        return view
    }

    private fun getCameraPhotoOrientation(imagePath: String?): Int { // метод переворачивает картинку, если она неправильно перевернута
        return when (ExifInterface(imagePath!!).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) { // получаем аттрибуты фотки
            ExifInterface.ORIENTATION_ROTATE_90 -> 90 // если фото повёрнуто, то вернуть его в исходое состояние повернув на 90 градусов
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 90
            else -> 0
        }
    }

    companion object {
        fun newInstance(photoFileName: String, photoFile: File): PictureDialogFragment {
            return PictureDialogFragment().apply { arguments = Bundle().apply { putSerializable("PHOTO_URI", photoFileName); putSerializable("PHOTO_PATH", photoFile); } }
        }
    }
}