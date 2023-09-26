package com.obrekht.maps.ui.map

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.obrekht.maps.R
import com.obrekht.maps.databinding.BottomSheetPlacemarkOptionsBinding
import com.obrekht.maps.utils.viewBinding

class PlacemarkOptionsFragment :
    BottomSheetDialogFragment(R.layout.bottom_sheet_placemark_options) {

    private val binding by viewBinding(BottomSheetPlacemarkOptionsBinding::bind)
    private val args: PlacemarkOptionsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.title.text = if (args.placemarkId == 0L) {
            getString(R.string.add_placemark_title)
        } else {
            "Placemark ${args.placemarkId}"
        }
    }
}