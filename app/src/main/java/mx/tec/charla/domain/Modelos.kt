package mx.tec.charla.domain

import kotlinx.serialization.Serializable

/**
 * Un mensaje de la sala. `en` son segundos desde 1970, como los cuenta Python (`time.time()`).
 * `imagen` es la clave de una foto en el servidor (GET /imagenes/{clave}), o null.
 */
@Serializable
data class Mensaje(val id: Long, val de: String, val texto: String, val en: Double, val imagen: String? = null)

/** Alguien que pidió entrar y espera a que el anfitrión decida. */
@Serializable
data class Solicitud(val id: String, val nickname: String)

/**
 * A qué sala está conectada la app, y como quién.
 *
 * En tu sala eres el anfitrión y entras con la clave; en la de alguien más
 * eres un invitado y entras con el token que te dieron al aceptarte.
 */
data class Destino(val servidor: String, val token: String, val yo: String, val esPropio: Boolean)

/** Lo que la pantalla dice de la conexión. */
sealed interface EstadoConexion {
    data object Desconectado : EstadoConexion
    data object Conectando : EstadoConexion
    data object Conectado : EstadoConexion
    data class Reconectando(val enSegundos: Long) : EstadoConexion
    /** El servidor no reconoce el token: no tiene caso reintentar. */
    data object Rechazado : EstadoConexion
}

/** Los nombres que pone el servidor. Nadie más puede usarlos. */
const val ANFITRION = "anfitrión"
const val SALA = "sala"

/** La dirección de una foto de esta sala. Se pide con el mismo token del WebSocket. */
fun Destino.urlDeImagen(clave: String) = "$servidor/imagenes/$clave"
