// app/src/main/java/com/example/itrainer/ui/categories/CategoriesAdapter.kt
package com.example.itrainer.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.itrainer.R
import com.example.itrainer.data.entities.Category
import com.example.itrainer.databinding.ItemCategoryBinding
import com.example.itrainer.utils.CategoryColors

class CategoriesAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onCategoryClick)
    }

    class CategoryViewHolder private constructor(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, onCategoryClick: (Category) -> Unit) {
            binding.apply {
                categoryChip.text = category.name.uppercase()
                categoryDetails.text = buildCategoryDetails(category)
                categoryPeriodInfo.text = buildPeriodInfo(category)

                // Usar la utilidad de colores
                val categoryColor = CategoryColors.getCategoryColor(category.name)
                categoryColorBar.setBackgroundColor(
                    ContextCompat.getColor(root.context, categoryColor)
                )
                categoryChip.setBackgroundColor(
                    ContextCompat.getColor(root.context, categoryColor)
                )

                root.setOnClickListener { onCategoryClick(category) }
            }
        }

        private fun buildCategoryDetails(category: Category): String {
            return "${category.periodsCount} períodos - " +
                    "${category.minPlayers}-${category.maxPlayers} jugadores - " +
                    "${category.playersPerPeriod} en cancha"
        }

        private fun buildPeriodInfo(category: Category): String {
            return "Min: ${category.minPeriodsPerPlayer} períodos/jugador - " +
                    "Max: ${category.maxPeriodsPerPlayer} períodos/jugador"
        }

        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemCategoryBinding.inflate(inflater, parent, false)
                return CategoryViewHolder(binding)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}