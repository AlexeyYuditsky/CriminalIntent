package com.alexeyyuditsky.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap { // функция отвечает за масштабирование изображения
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options) // функция декодирует путь к файлу в растровое изображение

    val srcWidth = options.outWidth.toFloat() // чтение размеров изображения на диске
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1 // поле отвеает за уменьшение кол-ва пикселоей преобразованного файла
    if (srcHeight > destHeight || srcWidth > destWidth) { // выясняем, на сколько нужно уменьшить изображение
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale) heightScale else widthScale
        inSampleSize = sampleScale.roundToInt() // округление значение до целового
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize // если inSampleSize > 1, то изображение уменьшается для экономии памяти (Например, inSampleSize == 4 возвращает изображение, которое составляет 1/4 ширины/высоты оригинала и 1/16 числа пикселей)
    return BitmapFactory.decodeFile(path, options) // чтение и создание окончательного растрового изображения
}