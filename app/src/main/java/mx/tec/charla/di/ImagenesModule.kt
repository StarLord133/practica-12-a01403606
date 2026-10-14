package mx.tec.charla.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ImagenesModule {

    /**
     * El cargador de fotos de Coil, con el MISMO OkHttpClient que Retrofit y
     * el WebSocket. Si Coil armara el suyo, sería otro cliente con otra
     * configuración: lo que le agregues al de la app no le llegaría a las fotos.
     */
    @Provides
    @Singleton
    fun imageLoader(@ApplicationContext context: Context, cliente: OkHttpClient): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { cliente })) }
            .build()
}
