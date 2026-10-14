package mx.tec.charla.data.imagenes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** La foto no se pudo leer: el archivo está dañado o no es imagen. */
class ImagenIlegible : Exception("No se pudo leer la foto")

/**
 * Una foto de cámara pesa 3 o 4 MB. En un chat nadie la ve a 4000 pixeles:
 * se reduce a 1280 de lado y se vuelve JPEG al 80 %. Queda en ~100 KB.
 *
 * De paso se pierde el EXIF (Bitmap.compress no lo copia): la ubicación y el
 * modelo del teléfono no salen de tu teléfono. Lo único que se rescata antes
 * es la orientación, porque sin ella las fotos verticales llegan acostadas.
 */
@Singleton
class CompresorDeImagen @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun comprimir(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 1. Solo las medidas, sin cargar los pixeles.
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) throw ImagenIlegible()

        // 2. Leerla ya reducida (inSampleSize, potencia de 2): una foto de 12 MP
        //    completa son 48 MB de memoria, y así no se llega a eso.
        var muestra = 1
        while (max(medidas.outWidth, medidas.outHeight) / (muestra * 2) >= LADO_MAX) muestra *= 2
        val leida = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
        } ?: throw ImagenIlegible()

        // 3. El ajuste fino a 1280 y el giro, en una sola pasada.
        val escala = minOf(1f, LADO_MAX.toFloat() / max(leida.width, leida.height))
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(grados(uri).toFloat())
        }
        val final = Bitmap.createBitmap(leida, 0, 0, leida.width, leida.height, matriz, true)

        val jpeg = ByteArrayOutputStream().use {
            final.compress(Bitmap.CompressFormat.JPEG, CALIDAD, it)
            it.toByteArray()
        }
        Log.d(TAG, "${medidas.outWidth}x${medidas.outHeight} → ${final.width}x${final.height}, ${jpeg.size / 1024} KB")
        if (final !== leida) final.recycle()
        leida.recycle()
        jpeg
    }

    private fun grados(uri: Uri): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
    }.getOrNull() ?: 0

    private companion object {
        const val LADO_MAX = 1280
        const val CALIDAD = 80
        const val TAG = "Compresor"
    }
}
