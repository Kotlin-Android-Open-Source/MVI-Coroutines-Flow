package com.hoc.flowmvi.core_ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.LazyThreadSafetyMode.NONE

class SwipeLeftToDeleteCallback(context: Context, private val onSwipedCallback: (Int) -> Unit) :
  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
  private val background: ColorDrawable by lazy(NONE) {
    ColorDrawable(getColor(context, R.color.swipe_to_delete_background_color))
  }
  private val iconDelete by lazy(NONE) {
    getDrawable(context, R.drawable.ic_baseline_delete_white_24)!!
  }

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder,
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
    isCurrentlyActive: Boolean,
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
      else -> {
        background.setBounds(0, 0, 0, 0)
        iconDelete.setBounds(0, 0, 0, 0)
      }
    }
    background.draw(c)
    iconDelete.draw(c)
  }
}
