package com.jeva.jeva.images


import android.content.Context
import android.widget.ImageView

//la clase Icon genera un ImageView de dimensiones cuadradas.
class Icon(context: Context) :  ImageView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var measuredWidth: Int = measuredWidth
        var measuredHeight: Int = measuredHeight
        if (measuredWidth>measuredHeight) {
            setMeasuredDimension(measuredHeight,measuredHeight)
        } else {
            setMeasuredDimension(measuredWidth,measuredWidth)
        }
    }

    fun setImage(resource: Int) {
        this.setImageResource(resource)
        this.scaleType = ImageView.ScaleType.CENTER_CROP
    }
}