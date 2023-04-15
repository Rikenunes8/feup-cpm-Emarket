package com.emarket.customer.views

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import java.lang.Integer.min

class SquaredCardView(ctx: Context, attrs: AttributeSet) : CardView(ctx, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(size, size)
    }
}
