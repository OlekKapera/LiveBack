package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView

/**
 * Created by kapera on 17-May-18.
 */
class RoundedTopImageView : ImageView {
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var mBitmap: Bitmap? = null
    private var mMatrix: Matrix? = null
    private val mRadius: Float = convertDpToPixel(10)
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.setColor(Color.BLACK)

        mPath = Path()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mDrawable = drawable
        if (drawable == null) {
            return
        }
        mBitmap = drawableToBitmap(drawable)

        val bDIWidth = mBitmap!!.width
        val bDIHeight = mBitmap!!.height

        //Fit to screen.
        val scale: Float
        if (mHeight / bDIHeight.toFloat() >= mWidth / bDIWidth.toFloat()) {
            scale = mHeight / bDIHeight.toFloat()
        } else {
            scale = mWidth / bDIWidth.toFloat()
        }

        val borderLeft = (mWidth - bDIWidth * scale) / 2
        val borderTop = (mHeight - bDIHeight * scale) / 2

        mMatrix = getImageMatrix()
        val drawableRect = RectF(0f, 0f, bDIWidth.toFloat(), bDIHeight.toFloat())
        val viewRect = RectF(borderLeft, borderTop, bDIWidth * scale + borderLeft, bDIHeight * scale + borderTop)
        mMatrix!!.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)
        invalidate()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    override protected fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mDrawable != null && mHeight > 0 && mWidth > 0) {
            setImageDrawable(mDrawable)
        }
    }

    override protected fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mBitmap == null) {
            return
        }

        canvas.drawColor(Color.TRANSPARENT)

        mPath!!.reset()
        mPath!!.moveTo(0f, mRadius)
        mPath!!.lineTo(0f, canvas.getHeight().toFloat())
        mPath!!.lineTo(canvas.getWidth().toFloat(), canvas.getHeight().toFloat())
        mPath!!.lineTo(canvas.getWidth().toFloat(), mRadius)
        mPath!!.quadTo(canvas.getWidth().toFloat(), 0f, canvas.getWidth() - mRadius, 0f)
        mPath!!.lineTo(mRadius, 0f)
        mPath!!.quadTo(0f, 0f, 0f, mRadius)


        canvas.drawPath(mPath, mPaint)
        canvas.clipPath(mPath)
        canvas.drawBitmap(mBitmap, mMatrix, mPaint)
    }

    protected fun convertDpToPixel(dp: Int): Float {
        val displayMetrics = Resources.getSystem().getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics).toFloat()
    }
}