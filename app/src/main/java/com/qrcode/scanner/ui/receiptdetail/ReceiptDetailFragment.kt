package com.qrcode.scanner.ui.receiptdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.qrcode.scanner.databinding.FragmentReceiptDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptDetailFragment : Fragment() {

    private var _binding: FragmentReceiptDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceiptDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val receipt = state.receipt ?: return@observe
            binding.retailer.text = receipt.retailerName ?: "Чек"
            binding.amount.text = String.format("%.2f ₽", receipt.amount)
            binding.fiscalInfo.text = "ФН: ${receipt.fiscalDriveNumber}  ФД: ${receipt.fiscalDocumentNumber}  ФП: ${receipt.fiscalSign}"
            binding.itemsCount.text = "Товаров: ${state.items.size}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
