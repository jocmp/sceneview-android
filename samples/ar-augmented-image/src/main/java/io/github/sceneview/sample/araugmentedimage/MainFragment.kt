package io.github.sceneview.sample.araugmentedimage

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.arcore.isTracking
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.VideoNode

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var sceneView: ARSceneView

    val augmentedImageNodes = mutableListOf<AugmentedImageNode>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById<ARSceneView>(R.id.sceneView).apply {

            configureSession { session, config ->
                config.addAugmentedImage(
                    session, "rabbit",
                    requireContext().assets.open("augmentedimages/rabbit.jpg")
                        .use(BitmapFactory::decodeStream)
                )
                config.addAugmentedImage(
                    session, "painting",
                    requireContext().assets.open("augmentedimages/painting.jpg")
                        .use(BitmapFactory::decodeStream)
                )
            }
            onSessionUpdated = { session, frame ->
                sceneView
                frame.getUpdatedAugmentedImages().forEach { augmentedImage ->
                    if (augmentedImageNodes.none { it.imageName == augmentedImage.name }) {
                        val augmentedImageNode = AugmentedImageNode(engine, augmentedImage).apply {
                            when (augmentedImage.name) {
                                "rabbit" -> addChildNode(
                                    ModelNode(
                                        modelInstance = modelLoader.createModelInstance(
                                            assetFileLocation = "models/rabbit.glb"
                                        ),
                                        scaleToUnits = 0.1f,
                                        centerOrigin = Position(0.0f)
                                    )
                                )

                                "painting" -> {
                                    addChildNode(VideoNode(
                                        materialLoader = materialLoader,
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(
                                                requireContext(),
                                                Uri.parse("https://artgallery.gvsu.edu/admin/media/collectiveaccess/quicktime/8/7/9/8/2794_ca_attribute_values_value_blob_879897_original.m4v")
                                            )
                                            isLooping = true
                                            setOnPreparedListener {
                                                if (augmentedImage.isTracking) {
                                                    start()
                                                }
                                            }
                                            prepareAsync()
                                        }
                                    ).also { videoNode ->
                                        onTrackingStateChanged = { trackingState ->
                                            when (trackingState) {
                                                TrackingState.TRACKING -> {
                                                    if (!videoNode.mediaPlayer.isPlaying) {
                                                        videoNode.mediaPlayer.start()
                                                    }
                                                }

                                                else -> {
                                                    if (videoNode.mediaPlayer.isPlaying) {
                                                        videoNode.mediaPlayer.pause()
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                            }
                        }
                        addChildNode(augmentedImageNode)
                        augmentedImageNodes += augmentedImageNode
                    }
                }
            }
        }
    }
}
