package com.qrcode.scanner.ui.receiptlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.qrcode.scanner.R
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptStatus
import com.qrcode.scanner.databinding.FragmentReceiptListBinding
import com.qrcode.scanner.databinding.ItemReceiptBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ReceiptListFragment : Fragment() {

    private var _binding: FragmentReceiptListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceiptListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ReceiptAdapter { receipt ->
            val bundle = Bundle().apply { putLong("receiptId", receipt.id) }
            findNavController().navigate(R.id.receiptDetailFragment, bundle)
        }
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_check_receipts) {
                viewModel.checkReceipts()
                true
            } else false
        }

        viewModel.receipts.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.isChecking.observe(viewLifecycleOwner) { checking ->
            val menuItem = binding.toolbar.menu.findItem(R.id.action_check_receipts)
            menuItem?.isEnabled = !checking
        }
        viewModel.checkResult.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                viewModel.consumeCheckResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ReceiptAdapter(
    private val onClick: (ReceiptEntity) -> Unit
) : ListAdapter<ReceiptEntity, ReceiptAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceiptBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ViewHolder(private val binding: ItemReceiptBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun bind(item: ReceiptEntity, onClick: (ReceiptEntity) -> Unit) {
            binding.retailer.text = item.retailerName ?: "Чек №${item.id}"
            binding.amount.text = String.format("%.2f ₽", item.amount)
            binding.date.text = dateFormat.format(Date(item.date))
            binding.fiscalInfo.text = "ФН: ${item.fiscalDriveNumber}"
            val statusText = when (item.status) {
                ReceiptStatus.Pending.name -> "Ожидает проверки"
                ReceiptStatus.Checked.name -> "Проверен"
                ReceiptStatus.Failed.name -> "Ошибка проверки"
                else -> ""
            }
            binding.status.text = statusText
            binding.status.visibility = if (statusText.isEmpty()) View.GONE else View.VISIBLE
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ReceiptEntity>() {
        override fun areItemsTheSame(a: ReceiptEntity, b: ReceiptEntity) = a.id == b.id
        override fun areContentsTheSame(a: ReceiptEntity, b: ReceiptEntity) = a == b
    }
}
