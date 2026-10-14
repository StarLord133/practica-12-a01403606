package mx.tec.charla.data.imagenes

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import mx.tec.charla.data.remote.SalaApi
import mx.tec.charla.domain.Destino
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** De la foto que elegiste a una clave en el servidor de la sala. */
@Singleton
class ImagenesRepository @Inject constructor(
    private val compresor: CompresorDeImagen,
    private val api: SalaApi
) {
    /** Comprime, sube y devuelve la clave. Lanza ImagenIlegible, IOException o HttpException. */
    suspend fun subir(uri: Uri, destino: Destino): String {
        val jpeg = compresor.comprimir(uri)
        val parte = MultipartBody.Part.createFormData("archivo", "foto.jpg", jpeg.toRequestBody("image/jpeg".toMediaType()))
        return api.subirImagen("${destino.servidor}/imagenes", "Bearer ${destino.token}", parte).id
    }
}
