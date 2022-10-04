package cse340.undo.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * This is an abstract class which serves to provide an interface for a general CircleColorPickerView
 * which is a view that allows users to choose colors and provides a method to register
 * event listeners.
 */
public class ColorPickerView extends AbstractColorPickerView {
    /* ********************************************************************************************** *
     * All of your applications state (the model) and methods that directly manipulate it are here    *
     * This does not include mState which is the literal state of your PPS, which is inherited
     * ********************************************************************************************** */

    /**
     * Your model should be private to the application, but the application needs a way to set
     * the color of the CircleColorPickerView, so we provide a setter of the color for the app
     */
    public void setColor(int newColor) {
        mCurrentColor = newColor;
    }

    // Returns the current color of the model
    public int getColor() { return mCurrentColor; }

    /**
     * Update the local model (color) for this colorpicker view
     *
     * @param x The x location that the user selected
     * @param y The y location that the user selected
     */
    protected void updateModel(float x, float y) { }



    /* ********************************************************************************************** *
     *                               <End of model declarations />
     * ********************************************************************************************** */


    /** Used the state to keep track of the PPS state for ColorPicker. */
    protected enum State { START, INSIDE }
    protected State mState = State.START;

    /** Used the EssentialGeometry to keep track of whether the pointer is inside the color picker. */
    protected enum EssentialGeometry {INSIDE, OUTSIDE }

    /**
     * Calculate the essential geometry given an event.
     *
     * @param event Motion event to compute geometry for, most likely a touch.
     * @return EssentialGeometry value.
     */
    protected EssentialGeometry essentialGeometry(MotionEvent event) {
        return EssentialGeometry.INSIDE;
    }

    /* ********************************************************************************************** *
     *                               <End of other fields and constants declarations />
     * ********************************************************************************************** */

    /* ********************************************************************************************** *
     *                               Constructor
     * ********************************************************************************************** */

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* ********************************************************************************************** *
     *                               Event Handling
     * ********************************************************************************************** */

    /**
     * This method should only be implemented once, in ColorPickerVIew
     * Your CircleColorPickerView and MyColorPickerView will both use this same method
     *
     * @param event The event that is passed in
     * @return Whether you consumed it or not
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EssentialGeometry geometry = essentialGeometry(event);

         switch (mState) {
             // Start => 1st time touch event occurs
             case START:
                 switch (geometry) {
                     // Inside => touch event occurs inside circle
                     case INSIDE:
                         updateModel(event.getX(), event.getY());
                         // Checks to see if touch even is over (i.e. finger up)
                         if (MotionEvent.ACTION_UP == event.getAction()) {
                             invokeColorChangeListeners(mCurrentColor);
                         } else {
                             mState = State.INSIDE;
                         }
                         invalidate();
                         return true;
                     // Outside =>
                     case OUTSIDE:
                     default:
                         return false;
                 }
             // Inside => touch event is continuing inside circle
             case INSIDE:
                 switch (geometry) {
                     case INSIDE:
                         updateModel(event.getX(), event.getY());
                         break;
                     case OUTSIDE:
                     default:
                         break;
                 }
                 // Touch event end check
                 if (MotionEvent.ACTION_UP == event.getAction()) {
                     invokeColorChangeListeners(mCurrentColor);
                     mState = State.START;
                 }
                 invalidate();
                 return true;
             default:
                 return false;
         }
    }
}
