package io.github.sceneview.node

import android.media.MediaPlayer
import com.google.android.filament.Engine
import com.google.android.filament.RenderableManager
import com.google.android.filament.Texture
import com.google.ar.sceneform.rendering.ExternalTexture
import dev.romainguy.kotlin.math.normalize
import io.github.sceneview.geometries.Plane
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size

open class VideoNode(
    val materialLoader: MaterialLoader,
    engine: Engine = materialLoader.engine,
    mediaPlayer: MediaPlayer,
    val texture: Texture = ExternalTexture(materialLoader.engine).filamentTexture,
    /**
     * `null` to adjust size on the normalized image size
     */
    val size: Size? = null,
    center: Position = Plane.DEFAULT_CENTER,
    normal: Direction = Plane.DEFAULT_NORMAL,
    builderApply: RenderableManager.Builder.() -> Unit = {}
) : PlaneNode(
    engine = engine,
    size = size ?: normalize(
        Size(
            mediaPlayer.videoWidth.toFloat(),
            mediaPlayer.videoHeight.toFloat()
        )
    ),
    center = center,
    normal = normal,
    materialInstance = materialLoader.createVideoInstance(texture),
    builderApply = builderApply
) {
    var mediaPlayer = mediaPlayer
        set(value) {
            field = value
            if (size == null) {
                updateGeometry(
                    size = normalize(Size(value.videoWidth.toFloat(), value.videoHeight.toFloat()))
                )
            }
        }

    init {
        if (size == null) {
            mediaPlayer.doOnVideoSized { player, width, height ->
                if (player == this.mediaPlayer) {
                    updateGeometry(size = normalize(Size(width.toFloat(), height.toFloat())))
                }
            }
        }
    }

    override fun destroy() {
        super.destroy()

        mediaPlayer.stop()
    }
}
//
//open class VideoNode(
//    videoMaterial: VideoMaterial,
//    val player: MediaPlayer,
//    size: Size = Plane.DEFAULT_SIZE,
//    center: Position = Plane.DEFAULT_CENTER,
//    normal: Direction = Plane.DEFAULT_NORMAL,
//    scaleToVideoRatio: Boolean = true,
//    /**
//     * Keep the video aspect ratio.
//     * - `true` to fit within max width or height
//     * - `false` the video will be stretched to the node scale
//     */
//    keepAspectRatio: Boolean = true,
//    /**
//     * The parent node.
//     *
//     * If set to null, this node will not be attached.
//     *
//     * The local position, rotation, and scale of this node will remain the same.
//     * Therefore, the world position, rotation, and scale of this node may be different after the
//     * parent changes.
//     */
//    parent: Node? = null,
//    renderableApply: RenderableManager.Builder.() -> Unit = {}
//) : PlaneNode(
//    engine = videoMaterial.engine,
//    size = size,
//    center = center,
//    normal = normal,
//    materialInstance = videoMaterial.instance,
//    parent = parent,
//    renderableApply = renderableApply
//) {
//
//

fun MediaPlayer.doOnVideoSized(block: (player: MediaPlayer, videoWidth: Int, videoHeight: Int) -> Unit) {
    if (videoWidth > 0 && videoHeight > 0) {
        block(this, videoWidth, videoHeight)
    } else {
        setOnVideoSizeChangedListener { _, width: Int, height: Int ->
            if (width > 0 && height > 0) {
                block(this, width, height)
            }
        }
    }
}
