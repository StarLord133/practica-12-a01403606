package mx.tec.charla.ui.state

import android.graphics.Bitmap
import mx.tec.charla.domain.Destino
import mx.tec.charla.domain.EstadoConexion
import mx.tec.charla.domain.Mensaje
import mx.tec.charla.domain.Solicitud

/** Todo lo que la pantalla de la sala necesita para dibujarse. */
data class SalaUiState(
    val destino: Destino? = null,
    val conexion: EstadoConexion = EstadoConexion.Desconectado,
    val mensajes: List<Mensaje> = emptyList(),
    val solicitudes: List<Solicitud> = emptyList(),
    val invitacion: InvitacionUi = InvitacionUi.Cerrada,
    /** Se está comprimiendo y subiendo una foto: el botón de cámara gira mientras tanto. */
    val subiendoFoto: Boolean = false,
    /** Por qué no salió la última foto. null = nada que decir. */
    val avisoFoto: String? = null
) {
    val puedeEscribir: Boolean get() = conexion == EstadoConexion.Conectado
}

/** La hoja con tu QR. */
sealed interface InvitacionUi {
    data object Cerrada : InvitacionUi
    data object Cargando : InvitacionUi
    data class Lista(val url: String, val qr: Bitmap) : InvitacionUi
    data class Error(val mensaje: String) : InvitacionUi
}

/** La pantalla de entrar a otra sala. */
data class UnirseUiState(
    /** La sala elegida por alguna de las tres vías. null = todavía ninguna. */
    val servidor: String? = null,
    val nickname: String = "",
    val etapa: Etapa = Etapa.Eligiendo
) {
    val puedePedir: Boolean
        get() = servidor != null && nickname.trim().length in 2..20 &&
            (etapa == Etapa.Eligiendo || etapa is Etapa.Error)
}

sealed interface Etapa {
    data object Eligiendo : Etapa
    data object Enviando : Etapa
    data object Esperando : Etapa
    /** Te aceptaron: la pantalla se cierra y la sala se conecta sola. */
    data object Dentro : Etapa
    data class Error(val mensaje: String) : Etapa
}
