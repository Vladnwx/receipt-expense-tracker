package com.qrcode.scanner.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.databinding.FragmentCategoriesBinding
import com.qrcode.scanner.databinding.ItemCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoryAdapter(
            onDelete = { viewModel.delete(it) }
        )
        binding.recyclerView.adapter = adapter
        viewModel.categories.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val input = TextInputEditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Новая категория")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text?.toString()?.trim()
                if (!name.isNullOrBlank()) viewModel.add(name)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategoryAdapter(
    private val onDelete: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryEntity, onDelete: (CategoryEntity) -> Unit) {
            binding.name.text = item.name
            binding.deleteButton.setOnClickListener { onDelete(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.id == b.id
        override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
    }
}
