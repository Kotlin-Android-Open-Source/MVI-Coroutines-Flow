package com.hoc.flowmvi.ui.main

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hoc.flowmvi.ui.main.databinding.ItemRecyclerUserBinding
import com.hoc081098.viewbindingdelegate.inflateViewBinding

internal class UserAdapter :
  ListAdapter<UserItem, UserAdapter.VH>(
    object : DiffUtil.ItemCallback<UserItem>() {
      override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem) =
        oldItem.id == newItem.id

      override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem) = oldItem == newItem
    }
  ) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
    VH(parent inflateViewBinding false)

  override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

  class VH(private val binding: ItemRecyclerUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: UserItem) {
      binding.run {
        nameTextView.text = item.fullName
        emailTextView.text = item.email
        avatarImage.load(item.avatar) {
          crossfade(200)
          placeholder(R.drawable.ic_baseline_person_24)
          error(R.drawable.ic_baseline_person_24)
          transformations(CircleCropTransformation())
        }
      }
    }
  }
}
