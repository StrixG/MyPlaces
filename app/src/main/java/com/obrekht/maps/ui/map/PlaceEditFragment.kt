package com.obrekht.maps.ui.map

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.obrekht.maps.R
import com.obrekht.maps.databinding.BottomSheetPlaceEditBinding
import com.obrekht.maps.utils.viewBinding
import kotlinx.coroutines.launch

class PlaceEditFragment :
    BottomSheetDialogFragment(R.layout.bottom_sheet_place_edit) {

    private val viewModel: MapViewModel
            by navGraphViewModels(R.id.map_fragment) { MapViewModel.Factory }

    private val binding by viewBinding(BottomSheetPlaceEditBinding::bind)
    private val args: PlaceEditFragmentArgs by navArgs()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheetDialog = (requireDialog() as BottomSheetDialog)
        bottomSheetDialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        with (binding) {
            if (args.placeId == 0L) {
                title.text = getString(R.string.add_placemark_title)
            } else {
                title.text = getString(R.string.placemark)
                buttonDelete.isVisible = true
            }

            editTextDescription.setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP ->
                        v.parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }

            buttonSave.setOnClickListener {
                viewModel.savePlace(
                    editTextName.text.toString(),
                    editTextDescription.text.toString()
                )
                dismiss()
            }

            buttonDelete.setOnClickListener {
                viewModel.deletePlace(args.placeId)
                dismiss()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    it.selectedPlace?.let { place ->
                        with (binding) {
                            editTextName.setText(place.name)
                            editTextDescription.setText(place.description)
                        }
                    }
                }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.clearPlace()
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.clearPlace()
        super.onDismiss(dialog)
    }
}
