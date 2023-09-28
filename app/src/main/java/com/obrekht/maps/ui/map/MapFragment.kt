package com.obrekht.maps.ui.map

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.obrekht.maps.R
import com.obrekht.maps.databinding.FragmentMapBinding
import com.obrekht.maps.model.Place
import com.obrekht.maps.utils.createBitmapFromVector
import com.obrekht.maps.utils.hasLocationPermission
import com.obrekht.maps.utils.viewBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map) {

    private val binding by viewBinding(FragmentMapBinding::bind)
    private val viewModel: MapViewModel
            by navGraphViewModels(R.id.map_fragment) { MapViewModel.Factory }

    private val args: MapFragmentArgs by navArgs()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var map: Map
    private var pinIcon: Bitmap? = null

    private var pinTranslationY: Float = 0f
    private var shortAnimationDuration: Int = 0

    private var isPinVisible: Boolean = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            moveCameraToMe()
        } else {
            Snackbar.make(
                requireView(),
                R.string.location_permission_denied,
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.buttonFindMe)
                .show()
        }
    }

    private val cameraListener = CameraListener { _, cameraPosition, cameraUpdateReason, finished ->
        with(binding.buttonCompass) {
            rotation = 360f - cameraPosition.azimuth
            if (cameraPosition.azimuth == 0f) {
                if (finished && isVisible) {
                    animate().alpha(0f)
                        .withEndAction { isVisible = false }
                        .start()
                }
            } else {
                if (!isVisible) {
                    alpha = 0f
                    isVisible = true
                    animate().alpha(1f).start()
                }
            }
        }

        if (cameraUpdateReason == CameraUpdateReason.GESTURES) {
            hidePin()
            if (finished) {
                tryStickToNorth()
            }
        }

        if (finished) {
            viewModel.onCameraTargetChange(
                cameraPosition.target.latitude,
                cameraPosition.target.longitude
            )
        }
    }

    private val cameraMovedCallback = Map.CameraCallback { completed ->
        if (view == null) return@CameraCallback

        if (completed) {
            openPlaceEdit()
        }
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            viewModel.clearPlace()
            showPin()

            moveCamera(point, callback = cameraMovedCallback)
        }

        override fun onMapLongTap(map: Map, point: Point) {}
    }

    private val placemarkTapListener = MapObjectTapListener { mapObject, _ ->
        if (mapObject is PlacemarkMapObject && mapObject.userData is Long) {
            val placeId = mapObject.userData as Long
            viewModel.loadPlace(placeId)
            openPlaceEdit(placeId)

            true
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.transparent_gray)

        map = binding.mapView.mapWindow.map.apply {
            logo.setAlignment(Alignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM))
            addInputListener(inputListener)
            addCameraListener(cameraListener)
        }

        savedInstanceState?.run {
            getDoubleArray(KEY_CAMERA_POSITION_TARGET)?.let {
                val (latitude, longitude) = it
                moveCamera(
                    Point(latitude, longitude),
                    getFloat(KEY_CAMERA_POSITION_ZOOM),
                    getFloat(KEY_CAMERA_POSITION_AZIMUTH),
                    getFloat(KEY_CAMERA_POSITION_TILT),
                    animate = false
                )
            }
        } ?: run {
            if (args.placeId == 0L) {
                moveCameraToMe(false)
            }
        }

        val placemarkSize = resources.getDimension(R.dimen.placemark_size)
        pinTranslationY = -placemarkSize / 2
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        pinIcon = requireContext().createBitmapFromVector(
            R.drawable.place_24,
            R.color.placemark,
            placemarkSize.toInt(),
            placemarkSize.toInt()
        )

        binding.buttonCompass.setOnClickListener {
            rotateCameraToNorth()
        }

        binding.buttonFindMe.setOnClickListener {
            moveCameraToMe()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    hidePin()

                    if (!it.isCameraMoved && it.selectedPlace != null) {
                        val point = Point(it.selectedPlace.latitude, it.selectedPlace.longitude)
                        moveCamera(point, DEFAULT_ZOOM, 0f, 0f)
                        viewModel.cameraMoved()
                    }

                    map.mapObjects.clear()
                    it.placeList.forEach(::addPlaceOnMap)
                }
        }
    }

    override fun onDestroyView() {
        requireActivity().window.statusBarColor = 0

        map.removeCameraListener(cameraListener)
        map.removeInputListener(inputListener)

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

    override fun onSaveInstanceState(outState: Bundle) {
        map.cameraPosition.run {
            outState.putAll(
                bundleOf(
                    KEY_CAMERA_POSITION_TARGET to doubleArrayOf(target.latitude, target.longitude),
                    KEY_CAMERA_POSITION_ZOOM to zoom,
                    KEY_CAMERA_POSITION_AZIMUTH to azimuth,
                    KEY_CAMERA_POSITION_TILT to tilt
                )
            )
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun openPlaceEdit(placeId: Long = 0L) {
        val action = MapFragmentDirections.actionOpenPlaceEdit().apply {
            this.placeId = placeId
        }
        findNavController().navigate(action)
    }

    private fun addPlaceOnMap(place: Place) {
        map.mapObjects.addPlacemark().apply {
            geometry = Point(place.latitude, place.longitude)
            userData = place.id
            setIcon(ImageProvider.fromBitmap(pinIcon), IconStyle().apply {
                anchor = PointF(0.5f, 1f)
            })
            setText(place.name, TextStyle().apply {
                placement = TextStyle.Placement.BOTTOM
            })
            addTapListener(placemarkTapListener)
        }
    }

    private fun showPin() {
        if (isPinVisible) return
        isPinVisible = true

        binding.placemark.isVisible = true

        ObjectAnimator.ofPropertyValuesHolder(
            binding.placemark,
            PropertyValuesHolder.ofFloat(View.ALPHA, binding.placemark.alpha, 1f),
            PropertyValuesHolder.ofFloat(
                View.TRANSLATION_Y,
                pinTranslationY * 2,
                pinTranslationY
            )
        ).apply {
            duration = shortAnimationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
    }

    private fun hidePin() {
        if (!isPinVisible) return
        isPinVisible = false

        ObjectAnimator.ofPropertyValuesHolder(
            binding.placemark,
            PropertyValuesHolder.ofFloat(View.ALPHA, binding.placemark.alpha, 0f),
            PropertyValuesHolder.ofFloat(
                View.TRANSLATION_Y,
                pinTranslationY,
                pinTranslationY * 2
            )
        ).apply {
            duration = shortAnimationDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToMe(animate: Boolean = true) {
        if (!hasLocationPermission()) {
            requestLocationPermissions()
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_LOW_POWER,
            CancellationTokenSource().token
        )
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    hidePin()
                    moveCamera(Point(it.latitude, it.longitude), DEFAULT_ZOOM, animate = animate)
                }
            }
    }

    private fun moveCamera(
        target: Point? = null,
        zoom: Float? = null,
        azimuth: Float? = null,
        tilt: Float? = null,
        animate: Boolean = true,
        callback: Map.CameraCallback? = null
    ) {
        map.cameraPosition.run {
            val position = CameraPosition(
                target ?: this.target,
                zoom ?: this.zoom,
                azimuth ?: this.azimuth,
                tilt ?: this.tilt
            )
            if (animate) {
                map.move(position, CAMERA_ANIMATION, callback)
            } else {
                map.move(position)
            }
        }
    }

    private fun rotateCameraToNorth() {
        map.cameraPosition.run {
            val position = CameraPosition(target, zoom, 0f, tilt)
            map.move(position, CAMERA_ANIMATION, null)
        }
    }

    private fun tryStickToNorth() {
        map.cameraPosition.run {
            if (azimuth <= STICK_NORTH_AZIMUTH_OFFSET || azimuth >= 360f - STICK_NORTH_AZIMUTH_OFFSET) {
                map.move(
                    CameraPosition(
                        target, zoom, 0f, tilt
                    ), CAMERA_ANIMATION, null
                )
            }
        }
    }

    companion object {
        private val CAMERA_ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
        private const val STICK_NORTH_AZIMUTH_OFFSET = 10f
        private const val DEFAULT_ZOOM = 15f

        private const val KEY_CAMERA_POSITION_TARGET = "camera_position_target"
        private const val KEY_CAMERA_POSITION_ZOOM = "camera_position_zoom"
        private const val KEY_CAMERA_POSITION_AZIMUTH = "camera_position_azimuth"
        private const val KEY_CAMERA_POSITION_TILT = "camera_position_tilt"
    }
}
