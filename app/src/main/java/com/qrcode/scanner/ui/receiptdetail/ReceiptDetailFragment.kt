package com.qrcode.scanner.ui.receiptdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qrcode.scanner.R
import com.qrcode.scanner.databinding.FragmentReceiptDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ReceiptDetailFragment : Fragment() {

    private var _binding: FragmentReceiptDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceiptDetailViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val receipt = state.receipt ?: return@observe

            binding.retailer.text = receipt.retailerName ?: "Чек"
            binding.amount.text = String.format("%.2f ₽", receipt.amount)

            val paymentDate = if (receipt.date > 0L) {
                dateFormat.format(Date(receipt.date))
            } else {
                getString(R.string.no_date)
            }
            binding.paymentDate.text = paymentDate

            val raw = state.raw
            val scanDate = if (raw != null) {
                dateFormat.format(Date(raw.scannedAt))
            } else {
                "—"
            }
            binding.scanDate.text = scanDate

            binding.fiscalInfo.text = "ФН: ${receipt.fiscalDriveNumber}\nФД: ${receipt.fiscalDocumentNumber}\nФП: ${receipt.fiscalSign}"
            binding.itemsCount.text = getString(R.string.items_count_format, state.items.size)

            val itemsText = state.items.joinToString("\n") { item ->
                "  • ${item.name} — ${item.quantity} × ${formatPrice(item.price)} = ${formatPrice(item.amount)}"
            }
            binding.itemsList.text = itemsText
        }
    }

    private fun formatPrice(value: Double): String = String.format("%.2f ₽", value)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
