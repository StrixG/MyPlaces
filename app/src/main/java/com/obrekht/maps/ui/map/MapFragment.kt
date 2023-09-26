package com.obrekht.maps.ui.map

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.obrekht.maps.NavGraphDirections
import com.obrekht.maps.R
import com.obrekht.maps.databinding.FragmentMapBinding
import com.obrekht.maps.model.Place
import com.obrekht.maps.utils.createBitmapFromVector
import com.obrekht.maps.utils.viewBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlin.random.Random

class MapFragment : Fragment(R.layout.fragment_map) {

    private val binding by viewBinding(FragmentMapBinding::bind)
    private val viewModel: MapViewModel by viewModels()

    private var insetsController: WindowInsetsControllerCompat? = null

    private lateinit var map: Map
    private var placeIcon: Bitmap? = null

    private var placemarkTranslationY: Float = 0f
    private var shortAnimationDuration: Int = 0

    private var isPlacemarkVisible: Boolean = false

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            addPlacemark(point, Place(Random.nextLong(), "Name", "Description", 0.0, 0.0))
            hidePin()
        }

        override fun onMapLongTap(map: Map, point: Point) {
            showPin()

            val action = NavGraphDirections.actionOpenPlacemarkOptions(0)
            findNavController().navigate(action)

            map.cameraPosition.run {
                val position = CameraPosition(point, zoom, azimuth, tilt)
                map.move(position, CAMERA_ANIMATION, null)
            }
        }
    }

    private val cameraListener = CameraListener { _, _, cameraUpdateReason, _ ->
        if (cameraUpdateReason == CameraUpdateReason.GESTURES) {
            hidePin()
        }
    }

    private val placemarkTapListener = MapObjectTapListener { mapObject, _ ->
        if (mapObject is PlacemarkMapObject && mapObject.userData is Long) {
            val placemarkId = mapObject.userData as Long
            val action = NavGraphDirections.actionOpenPlacemarkOptions(placemarkId)
            findNavController().navigate(action)

            true
        } else {
            false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.transparent_gray)

        val placemarkSize = resources.getDimension(R.dimen.placemark_size)
        placeIcon = requireContext().createBitmapFromVector(
            R.drawable.place_24,
            R.color.placemark,
            placemarkSize.toInt(),
            placemarkSize.toInt()
        )
        map = binding.mapView.map.apply {
            addInputListener(inputListener)
            addCameraListener(cameraListener)
            move(START_POSITION)
        }
        placemarkTranslationY = -resources.getDimension(R.dimen.placemark_size) / 2
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    override fun onDestroyView() {
        requireActivity().window.statusBarColor = 0

        insetsController = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun addPlacemark(point: Point, place: Place) {
        map.mapObjects.addPlacemark(point).apply {
            userData = place.id
            setIcon(ImageProvider.fromBitmap(placeIcon))
            setText(place.name)
            addTapListener(placemarkTapListener)
        }
    }

    private fun showPin() {
        if (isPlacemarkVisible) return
        isPlacemarkVisible = true

        binding.placemark.isVisible = true

        ObjectAnimator.ofPropertyValuesHolder(
            binding.placemark,
            PropertyValuesHolder.ofFloat(View.ALPHA, binding.placemark.alpha, 1f),
            PropertyValuesHolder.ofFloat(
                View.TRANSLATION_Y,
                placemarkTranslationY * 2,
                placemarkTranslationY
            )
        ).apply {
            duration = shortAnimationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
    }

    private fun hidePin() {
        if (!isPlacemarkVisible) return
        isPlacemarkVisible = false

        ObjectAnimator.ofPropertyValuesHolder(
            binding.placemark,
            PropertyValuesHolder.ofFloat(View.ALPHA, binding.placemark.alpha, 0f),
            PropertyValuesHolder.ofFloat(
                View.TRANSLATION_Y,
                placemarkTranslationY,
                placemarkTranslationY * 2
            )
        ).apply {
            duration = shortAnimationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
    }

    companion object {
        private val START_POSITION = CameraPosition(Point(54.710161, 20.510138), 15f, 0f, 0f)
        private val CAMERA_ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
    }
}