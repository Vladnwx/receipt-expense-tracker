package com.qrcode.scanner.ui.receiptlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qrcode.scanner.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptListFragment : Fragment() {

    private val viewModel: ReceiptListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ReceiptListScreen(
                    viewModel = viewModel,
                    onReceiptClick = { receiptId ->
                        val bundle = Bundle().apply { putLong("receiptId", receiptId) }
                        findNavController().navigate(R.id.receiptDetailFragment, bundle)
                    }
                )
            }
        }
    }
}
