package com.hoc.flowmvi.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeLeftToDeleteCallback(context: Context, private val onSwipedCallback: (Int) -> Unit) :
  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
  private val background: ColorDrawable = ColorDrawable(Color.parseColor("#f44336"))
  private val iconDelete =
    ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_white_24)!!

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ) = false

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    val position = viewHolder.bindingAdapterPosition
    if (position != RecyclerView.NO_POSITION) {
      onSwipedCallback(position)
    }
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    val itemView = viewHolder.itemView

    when {
      dX < 0 -> {
        val iconMargin = (itemView.height - iconDelete.intrinsicHeight) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + iconDelete.intrinsicHeight

        val iconRight = itemView.right - iconMargin
        val iconLeft = iconRight - iconDelete.intrinsicWidth

        iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        background.setBounds(
          itemView.right + dX.toInt() - 8,
          itemView.top,
          itemView.right,
          itemView.bottom
        )
      }
      else -> background.setBounds(0, 0, 0, 0)
    }
    background.draw(c)
    iconDelete.draw(c)
  }
}
