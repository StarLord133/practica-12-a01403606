package mx.tec.charla.ui.navigation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.io.File
import mx.tec.charla.ui.screens.SalaScreen
import mx.tec.charla.ui.screens.UnirseScreen
import mx.tec.charla.ui.state.Etapa
import mx.tec.charla.ui.state.SalaViewModel
import mx.tec.charla.ui.state.UnirseViewModel

object Ruta {
    const val SALA = "sala"
    const val UNIRSE = "unirse"
}

@Composable
fun CharlaNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Ruta.SALA) {

        composable(Ruta.SALA) {
            val viewModel: SalaViewModel = hiltViewModel()
            val estado by viewModel.ui.collectAsStateWithLifecycle()

            // Se ve: conectar. Se va (otra pantalla, o la app al fondo): cerrar.
            // Lo mismo que el tablón de la Práctica 8 con su stream.
            LifecycleStartEffect(Unit) {
                viewModel.alAparecer()
                onStopOrDispose { viewModel.alDesaparecer() }
            }

            // Fotos para la sala: de la galería (sin permisos) o de la cámara.
            val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { foto ->
                if (foto != null) viewModel.enviarFoto(foto)
            }
            // La cámara es otra app: Android puede cerrar Charla mientras tomas la foto.
            // rememberSaveable guarda a qué archivo iba, para encontrarla al volver.
            val contexto = LocalContext.current
            var fotoPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
            val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
                val foto = fotoPendiente
                fotoPendiente = null
                if (tomada && foto != null) viewModel.enviarFoto(foto)
            }

            SalaScreen(
                estado = estado,
                onEnviar = viewModel::enviar,
                onAprobar = viewModel::aprobar,
                onRechazar = viewModel::rechazar,
                onInvitar = viewModel::invitar,
                onCerrarInvitacion = viewModel::cerrarInvitacion,
                onUnirme = { nav.navigate(Ruta.UNIRSE) },
                onReiniciar = viewModel::reiniciar,
                onCamara = {
                    val destino = archivoParaFoto(contexto)
                    fotoPendiente = destino
                    camara.launch(destino)
                },
                onGaleria = {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onAvisoFotoVisto = viewModel::avisoFotoVisto
            )
        }

        composable(Ruta.UNIRSE) {
            val viewModel: UnirseViewModel = hiltViewModel()
            val estado by viewModel.ui.collectAsStateWithLifecycle()

            // El selector de fotos del sistema (Práctica 10): sin permisos, solo ves lo que eliges.
            val selector = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imagen ->
                if (imagen != null) viewModel.leerImagen(imagen)
            }

            // Te aceptaron: de vuelta a la sala, que ya se está conectando a la nueva.
            LaunchedEffect(estado.etapa) {
                if (estado.etapa == Etapa.Dentro) nav.popBackStack()
            }

            UnirseScreen(
                estado = estado,
                onAtras = { nav.popBackStack() },
                onEscanear = viewModel::escanear,
                onElegirImagen = {
                    selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onPegar = viewModel::usarEnlace,
                onNickname = viewModel::cambiarNickname,
                onPedirEntrar = viewModel::pedirEntrar,
                onCancelar = viewModel::cancelar,
                onOtraSala = viewModel::otraSala
            )
        }
    }
}

/**
 * Donde la cámara deja la foto: siempre el mismo archivo, que la siguiente
 * sobrescribe. Ya subida, no hace falta guardarla. La dirección es content://
 * por FileProvider (AndroidManifest.xml y res/xml/fotos.xml).
 */
private fun archivoParaFoto(contexto: Context): Uri {
    val archivo = File(File(contexto.filesDir, "fotos").apply { mkdirs() }, "camara.jpg")
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.fotos", archivo)
}
