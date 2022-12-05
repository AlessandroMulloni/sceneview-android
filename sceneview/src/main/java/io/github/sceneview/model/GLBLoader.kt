package io.github.sceneview.model

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.gorisse.thomas.lifecycle.observe
import io.github.sceneview.Filament.assetLoader
import io.github.sceneview.Filament.resourceLoader
import io.github.sceneview.renderable.setScreenSpaceContactShadows
import io.github.sceneview.utils.useFileBufferNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.Buffer

object GLBLoader {

    /**
     * ### Utility for loading a glTF 3D model
     *
     * @param glbFileLocation the glb file location:
     * - A relative asset file location *models/mymodel.glb*
     * - An android resource from the res folder *context.getResourceUri(R.raw.mymodel)*
     * - A File path *Uri.fromFile(myModelFile).path*
     * - An http or https url *https://mydomain.com/mymodel.glb*
     */
    suspend fun loadModel(
        context: Context,
        glbFileLocation: String
    ): Model? = context.useFileBufferNotNull(glbFileLocation) { buffer ->
        withContext(Dispatchers.Main) {
            createModel(buffer)
        }
    }

    /**
     * ### Utility for loading a glTF 3D model
     *
     * For Java compatibility usage.
     *
     * Kotlin developers should use [GLBLoader.loadModel]
     *
     * [Documentation][GLBLoader.loadEnvironment]
     *
     */
    fun loadModelAsync(
        context: Context,
        lifecycle: Lifecycle,
        glbFileLocation: String,
        result: (Model?) -> Unit
    ) = lifecycle.coroutineScope.launchWhenCreated {
        result(loadModel(context, glbFileLocation))
    }

    fun createModel(buffer: Buffer): Model? =
        assetLoader.createAssetFromBinary(buffer)?.also { asset ->
            resourceLoader.loadResources(asset)
            //TODO: Used by Filament ModelViewer, see if it's usefull
            asset.renderableEntities.forEach {
                it.setScreenSpaceContactShadows(true)
            }
            asset.releaseSourceData()
        }
}