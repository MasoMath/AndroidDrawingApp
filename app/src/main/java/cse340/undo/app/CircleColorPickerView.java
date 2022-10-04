package cse340.undo.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import android.view.MotionEvent;

import android.support.v4.graphics.ColorUtils;
import cse340.undo.R;

/**
 * This is a subclass of AbstractColorPickerView, that is, this View implements a ColorPicker.
 *
 * There are several class fields, enums, callback classes, and helper functions which have
 * been implemented for you.
 *
 * PLEASE READ AbstractColorPickerView.java to learn about these.
 */
public class CircleColorPickerView extends ColorPickerView {

    /**
     * Update the local model (color) for this colorpicker view
     *
     * @param x The x location that the user selected
     * @param y The y location that the user selected
     */
    protected void updateModel(float x, float y) {
        setColor(getColorFromAngle(Math.toDegrees(getTouchAngle(x, y))));
    }

    /* ********************************************************************************************** *
     *                               <End of model declarations />
     * ********************************************************************************************** */

    /* ********************************************************************************************** *
     * You may create any constants you wish here.                                                     *
     * You may also create any fields you want, that are not necessary for the state but allow       *
     * for better optimized or cleaner code                                                           *
     * ********************************************************************************************** */
    /** Helper fields for keeping track of view geometry. */
    protected float mCenterX, mCenterY, mRadius;

    /** Ratio between radius of the thumb handle and mRadius, the radius of the wheel. */
    protected static final float RADIUS_TO_THUMB_RATIO = 0.085f;

    private float mCenterCircleRadius, mThumbRadius;

    private Paint mBrush;

    /* ********************************************************************************************** *
     *                               <End of other fields and constants declarations />
     * ********************************************************************************************** */

    /**
     * Constructor of the ColorPicker View
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. This value may be null.
     */
    public CircleColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageResource(R.drawable.color_wheel);

        mBrush = new Paint();
        mBrush.setStyle(Paint.Style.FILL);
    }

    /**
     * Draw the ColorPicker on the Canvas
     * @param canvas the canvas that is drawn upon
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Creates the center circle
        mBrush.setColor(mCurrentColor);
        canvas.drawCircle(mCenterX, mCenterY, mCenterCircleRadius, mBrush);

        // Grabs angle for thumb
        float theta = getAngleFromColor(mCurrentColor);
        float radialDistance = mRadius - mThumbRadius;

        mBrush.setColor(Color.WHITE);
        // Assigns correct Alpha levels for the thumb
        if (mState == State.INSIDE) {
            mBrush.setAlpha(255 / 2);
            // Draws thumb at correct position in polar representation
            canvas.drawCircle(
                    radialDistance * ((float) Math.cos(theta)) + mCenterX,
                    radialDistance * ((float) Math.sin(theta)) + mCenterY,
                    mThumbRadius, mBrush
            );
            mBrush.setAlpha(255);
        } else {
            // Draws thumb at correct position in polar representation
            canvas.drawCircle(
                    radialDistance * ((float) Math.cos(theta)) + mCenterX,
                    radialDistance * ((float) Math.sin(theta)) + mCenterY,
                    mThumbRadius, mBrush
            );
        }
    }

    /**
     * Called when this view should assign a size and position to all of its children.
     * @param changed This is a new size or position for this view
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // finds the min between width and height and sets radius, x, and y
        // calls on get_() return nonzero since super.onLayout was called
        mRadius = Math.min(getWidth(), getHeight()) / 2f;
        mCenterX = mRadius;
        mCenterY = mRadius;

        // Calculates radii of thumb and center circle
        mThumbRadius = mRadius * RADIUS_TO_THUMB_RATIO;
        mCenterCircleRadius = mRadius - 2f * mThumbRadius + 1f;
    }

    /**
     * Calculate the essential geometry given an event.
     *
     * @param event Motion event to compute geometry for, most likely a touch.
     * @return EssentialGeometry value.
     */
    @Override
    protected EssentialGeometry essentialGeometry(MotionEvent event) {
        // Grabs the x,y coordinates
        float xPos = event.getX() - mCenterX;
        float yPos = event.getY() - mCenterY;
        if ((xPos * xPos + yPos * yPos) <= (mRadius * mRadius)) {
            return EssentialGeometry.INSIDE;
        } else {
            return EssentialGeometry.OUTSIDE;
        }
    }

    /* ********************************************************************************************** *
     *                               <Helper Functions />
     * ********************************************************************************************** */

    /**
     * Converts from a color to angle on the wheel.
     *
     * @param color RGB color as integer.
     * @return Position of this color on the wheel in radians.
     * @see #getTouchAngle(float, float)
     */
    public static float getAngleFromColor(int color) {
        float[] HSL = new float[3];
        ColorUtils.colorToHSL(color, HSL);
        return (float) Math.toRadians(HSL[0] - 90f);
    }

    /***
     * Calculate the angle of the selection on color wheel given a touch.
     *
     * @param touchX Horizontal position of the touch event.
     * @param touchY Vertical position of the touch event.
     * @return Angle of the touch, in radians.
     */
    protected float getTouchAngle(float touchX, float touchY) {
        return (float) Math.atan2(touchY - mCenterY, touchX - mCenterX);
    }

    // Returns the Color corresponding to the angle passed as a double
    public int getColorFromAngle(double angle) {
        float[] HSL = new float[3];
        ColorUtils.colorToHSL(mCurrentColor, HSL);
        if (angle < -90) {
            angle = angle + 360f;
        }
        HSL[0] = ((float) angle) + 90f;
        return ColorUtils.HSLToColor(HSL);
    }
}
