package partha.flowlayoutkotlin

/**
 * Created by PARTHA on 13-07-2017.
 */

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import java.util.*


/**
 *
 * This will arrange all child elements horizontally one next to another according to it's size/length.
 * Help topic blazsolar
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class FlowLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private var mFlow = DEFAULT_FLOW
    private var mChildSpacing = DEFAULT_CHILD_SPACING
    private var mChildSpacingForLastRow = DEFAULT_CHILD_SPACING_FOR_LAST_ROW
    private var mRowSpacing = DEFAULT_ROW_SPACING
    private var mAdjustedRowSpacing = DEFAULT_ROW_SPACING
    private var mRtl = DEFAULT_RTL
    private var mMaxRows = DEFAULT_MAX_ROWS

    private val mHorizontalSpacingForRow = ArrayList<Float>()
    private val mHeightForRow = ArrayList<Int>()
    private val mChildNumForRow = ArrayList<Int>()

    /**
     * Returns whether to allow child views flow to next row when there is no enough space.
     *
     * @return Whether to flow child views to next row when there is no enough space.
     */
    /**
     * Sets whether to allow child views flow to next row when there is no enough space.
     *
     * @param flow true to allow flow. false to restrict all child views in one row.
     */
    var isFlow: Boolean
        get() = mFlow
        set(flow) {
            mFlow = flow
            requestLayout()
        }

    /**
     * Returns the horizontal spacing between child views.
     *
     * @return The spacing, either [FlowLayout.SPACING_AUTO], or a fixed size in pixels.
     */
    /**
     * Sets the horizontal spacing between child views.
     *
     * @param childSpacing The spacing, either [FlowLayout.SPACING_AUTO], or a fixed size in
     * pixels.
     */
    var childSpacing: Int
        get() = mChildSpacing
        set(childSpacing) {
            mChildSpacing = childSpacing
            requestLayout()
        }

    /**
     * Returns the horizontal spacing between child views of the last row.
     *
     * @return The spacing, either [FlowLayout.SPACING_AUTO],
     * [FlowLayout.SPACING_ALIGN], or a fixed size in pixels
     */
    /**
     * Sets the horizontal spacing between child views of the last row.
     *
     * @param childSpacingForLastRow The spacing, either [FlowLayout.SPACING_AUTO],
     * [FlowLayout.SPACING_ALIGN], or a fixed size in pixels
     */
    var childSpacingForLastRow: Int
        get() = mChildSpacingForLastRow
        set(childSpacingForLastRow) {
            mChildSpacingForLastRow = childSpacingForLastRow
            requestLayout()
        }

    /**
     * Returns the vertical spacing between rows.
     *
     * @return The spacing, either [FlowLayout.SPACING_AUTO], or a fixed size in pixels.
     */
    /**
     * Sets the vertical spacing between rows in pixels. Use SPACING_AUTO to evenly place all rows
     * in vertical.
     *
     * @param rowSpacing The spacing, either [FlowLayout.SPACING_AUTO], or a fixed size in
     * pixels.
     */
    var rowSpacing: Float
        get() = mRowSpacing
        set(rowSpacing) {
            mRowSpacing = rowSpacing
            requestLayout()
        }

    /**
     * Returns the maximum number of rows of the FlowLayout.
     *
     * @return The maximum number of rows.
     */
    /**
     * Sets the number of row of the FlowLayout to be at most maxRows size.
     *
     * @param maxRows The maximum number of rows.
     */
    var maxRows: Int
        get() = mMaxRows
        set(maxRows) {
            mMaxRows = maxRows
            requestLayout()
        }

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs, R.styleable.FlowLayout, 0, 0)
        try {
            mFlow = a.getBoolean(R.styleable.FlowLayout_flow, DEFAULT_FLOW)
            try {
                mChildSpacing = a.getInt(R.styleable.FlowLayout_childSpacing, DEFAULT_CHILD_SPACING)
            } catch (e: NumberFormatException) {
                mChildSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_childSpacing, dpToPx(DEFAULT_CHILD_SPACING.toFloat()).toInt())
            }

            try {
                mChildSpacingForLastRow = a.getInt(R.styleable.FlowLayout_childSpacingForLastRow, SPACING_UNDEFINED)
            } catch (e: NumberFormatException) {
                mChildSpacingForLastRow = a.getDimensionPixelSize(R.styleable.FlowLayout_childSpacingForLastRow, dpToPx(DEFAULT_CHILD_SPACING.toFloat()).toInt())
            }

            try {
                mRowSpacing = a.getInt(R.styleable.FlowLayout_rowSpacing, 0).toFloat()
            } catch (e: NumberFormatException) {
                mRowSpacing = a.getDimension(R.styleable.FlowLayout_rowSpacing, dpToPx(DEFAULT_ROW_SPACING))
            }

            mMaxRows = a.getInt(R.styleable.FlowLayout_maxRows, DEFAULT_MAX_ROWS)
            mRtl = a.getBoolean(R.styleable.FlowLayout_rtl, DEFAULT_RTL)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        mHorizontalSpacingForRow.clear()
        mChildNumForRow.clear()
        mHeightForRow.clear()

        var measuredHeight = 0
        var measuredWidth = 0
        val childCount = childCount
        var rowWidth = 0
        var maxChildHeightInRow = 0
        var childNumInRow = 0
        val rowSize = widthSize - paddingLeft - paddingRight
        val allowFlow = widthMode != View.MeasureSpec.UNSPECIFIED && mFlow
        val childSpacing = if (mChildSpacing == SPACING_AUTO && widthMode == View.MeasureSpec.UNSPECIFIED)
            0
        else
            mChildSpacing
        val tmpSpacing = (if (childSpacing == SPACING_AUTO) 0 else childSpacing).toFloat()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }

            val childParams = child.layoutParams
            var horizontalMargin = 0
            var verticalMargin = 0
            if (childParams is ViewGroup.MarginLayoutParams) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, measuredHeight)
                horizontalMargin = childParams.leftMargin + childParams.rightMargin
                verticalMargin = childParams.topMargin + childParams.bottomMargin
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }

            val childWidth = child.measuredWidth + horizontalMargin
            val childHeight = child.measuredHeight + verticalMargin
            if (allowFlow && rowWidth + childWidth > rowSize) { // Need flow to next row
                // Save parameters for current row
                mHorizontalSpacingForRow.add(
                        getSpacingForRow(childSpacing, rowSize, rowWidth, childNumInRow))
                mChildNumForRow.add(childNumInRow)
                mHeightForRow.add(maxChildHeightInRow)
                if (mHorizontalSpacingForRow.size <= mMaxRows) {
                    measuredHeight += maxChildHeightInRow
                }
                measuredWidth = Math.max(measuredWidth, rowWidth)

                // Place the child view to next row
                childNumInRow = 1
                rowWidth = childWidth + tmpSpacing.toInt()
                maxChildHeightInRow = childHeight
            } else {
                childNumInRow++
                rowWidth += (childWidth + tmpSpacing).toInt()
                maxChildHeightInRow = Math.max(maxChildHeightInRow, childHeight)
            }
        }

        // Measure remaining child views in the last row
        if (mChildSpacingForLastRow == SPACING_ALIGN) {
            // For SPACING_ALIGN, use the same spacing from the row above if there is more than one
            // row.
            if (mHorizontalSpacingForRow.size >= 1) {
                mHorizontalSpacingForRow.add(
                        mHorizontalSpacingForRow[mHorizontalSpacingForRow.size - 1])
            } else {
                mHorizontalSpacingForRow.add(
                        getSpacingForRow(childSpacing, rowSize, rowWidth, childNumInRow))
            }
        } else if (mChildSpacingForLastRow != SPACING_UNDEFINED) {
            // For SPACING_AUTO and specific DP values, apply them to the spacing strategy.
            mHorizontalSpacingForRow.add(
                    getSpacingForRow(mChildSpacingForLastRow, rowSize, rowWidth, childNumInRow))
        } else {
            // For SPACING_UNDEFINED, apply childSpacing to the spacing strategy for the last row.
            mHorizontalSpacingForRow.add(
                    getSpacingForRow(childSpacing, rowSize, rowWidth, childNumInRow))
        }

        mChildNumForRow.add(childNumInRow)
        mHeightForRow.add(maxChildHeightInRow)
        if (mHorizontalSpacingForRow.size <= mMaxRows) {
            measuredHeight += maxChildHeightInRow
        }
        measuredWidth = Math.max(measuredWidth, rowWidth)

        if (childSpacing == SPACING_AUTO) {
            measuredWidth = widthSize
        } else if (widthMode == View.MeasureSpec.UNSPECIFIED) {
            measuredWidth = measuredWidth + paddingLeft + paddingRight
        } else {
            measuredWidth = Math.min(measuredWidth + paddingLeft + paddingRight, widthSize)
        }

        measuredHeight += paddingTop + paddingBottom
        val rowNum = Math.min(mHorizontalSpacingForRow.size, mMaxRows)
        val rowSpacing = if (mRowSpacing == SPACING_AUTO.toFloat() && heightMode == View.MeasureSpec.UNSPECIFIED)
            0
        else
            mRowSpacing
        if (rowSpacing == SPACING_AUTO.toFloat()) {
            if (rowNum > 1) {
                mAdjustedRowSpacing = ((heightSize - measuredHeight) / (rowNum - 1)).toFloat()
            } else {
                mAdjustedRowSpacing = 0f
            }
            measuredHeight = heightSize
        } else {
            mAdjustedRowSpacing = rowSpacing as Float
            if (rowNum > 1) {
                measuredHeight = if (heightMode == View.MeasureSpec.UNSPECIFIED)
                    (measuredHeight + mAdjustedRowSpacing * (rowNum - 1)).toInt()
                else
                    Math.min((measuredHeight + mAdjustedRowSpacing * (rowNum - 1)).toInt(),
                            heightSize)
            }
        }

        measuredWidth = if (widthMode == View.MeasureSpec.EXACTLY) widthSize else measuredWidth
        measuredHeight = if (heightMode == View.MeasureSpec.EXACTLY) heightSize else measuredHeight
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        var x = if (mRtl) width - paddingRight else paddingLeft
        var y = paddingTop

        val rowCount = mChildNumForRow.size
        var childIdx = 0
        for (row in 0 until rowCount) {
            val childNum = mChildNumForRow[row]
            val rowHeight = mHeightForRow[row]
            val spacing = mHorizontalSpacingForRow[row]
            var i = 0
            while (i < childNum && childIdx < childCount) {
                val child = getChildAt(childIdx++)
                if (child.visibility == View.GONE) {
                    continue
                } else {
                    i++
                }

                val childParams = child.layoutParams
                var marginLeft = 0
                var marginTop = 0
                var marginRight = 0
                if (childParams is ViewGroup.MarginLayoutParams) {
                    marginLeft = childParams.leftMargin
                    marginRight = childParams.rightMargin
                    marginTop = childParams.topMargin
                }

                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (mRtl) {
                    child.layout(x - marginRight - childWidth, y + marginTop,
                            x - marginRight, y + marginTop + childHeight)
                    x -= (childWidth.toFloat() + spacing + marginLeft.toFloat() + marginRight.toFloat()).toInt()
                } else {
                    child.layout(x + marginLeft, y + marginTop,
                            x + marginLeft + childWidth, y + marginTop + childHeight)
                    x += (childWidth.toFloat() + spacing + marginLeft.toFloat() + marginRight.toFloat()).toInt()
                }
            }
            x = if (mRtl) width - paddingRight else paddingLeft
            y += (rowHeight + mAdjustedRowSpacing).toInt()
        }
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }

    private fun getSpacingForRow(spacingAttribute: Int, rowSize: Int, usedSize: Int, childNum: Int): Float {
        val spacing: Float
        if (spacingAttribute == SPACING_AUTO) {
            if (childNum > 1) {
                spacing = ((rowSize - usedSize) / (childNum - 1)).toFloat()
            } else {
                spacing = 0f
            }
        } else {
            spacing = spacingAttribute.toFloat()
        }
        return spacing
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    companion object {

        private val LOG_TAG = "PARTHA"

        /**
         * Special value for the child view spacing.
         * SPACING_AUTO means that the actual spacing is calculated according to the size of the
         * container and the number of the child views, so that the child views are placed evenly in
         * the container.
         */
        val SPACING_AUTO = -65536

        /**
         * Special value for the horizontal spacing of the child views in the last row
         * SPACING_ALIGN means that the horizontal spacing of the child views in the last row keeps
         * the same with the spacing used in the row above. If there is only one row, this value is
         * ignored and the spacing will be calculated according to childSpacing.
         */
        val SPACING_ALIGN = -65537

        private val SPACING_UNDEFINED = -65538

        private val DEFAULT_FLOW = true
        private val DEFAULT_CHILD_SPACING = 0
        private val DEFAULT_CHILD_SPACING_FOR_LAST_ROW = SPACING_UNDEFINED
        private val DEFAULT_ROW_SPACING = 0f
        private val DEFAULT_RTL = false
        private val DEFAULT_MAX_ROWS = Integer.MAX_VALUE
    }

}
