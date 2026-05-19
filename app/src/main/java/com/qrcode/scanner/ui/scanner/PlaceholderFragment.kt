package com.qrcode.scanner.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class PlaceholderFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "PlaceholderFragment"
            setTextColor(0xFFFF0000.toInt())
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(0xFFE6E6FA.toInt()) // lavender
    }
}
