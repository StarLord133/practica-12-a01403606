package mx.tec.charla

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

/**
 * Donde Hilt arma el contenedor de la app (Práctica 9). Declarada en el manifiesto con `android:name`.
 *
 * También le dice a Coil qué cargador usar en todos los `AsyncImage`: el de
 * di/ImagenesModule.kt. Sin esto, Coil inventaría uno propio.
 */
@HiltAndroidApp
class CharlaApplication : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var imageLoader: Provider<ImageLoader>

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
