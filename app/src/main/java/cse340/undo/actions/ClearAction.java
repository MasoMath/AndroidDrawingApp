package cse340.undo.actions;

import android.support.annotation.NonNull;

import cse340.undo.app.ClearView;
import cse340.undo.app.DrawingView;

/**
 * Reversible action which renders a stroke in DrawingView.
 */
public class ClearAction extends AbstractReversibleViewAction {
    private ClearView mClearView;

    /**
     * Creates an action that clears the canvas.
     */
    public ClearAction() { }

    /**
     * Renders the stroke in the given view.
     *
     * @param view  DrawingView in which to render the stroke.
     */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        mClearView = new ClearView(view.getContext());
        view.addView(mClearView);
    }

    /**
     * De-renders the stroke in the given view.
     *
     * @param view  DrawingView in which to de-render the stroke.
     */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        if (view.indexOfChild(mClearView) < 0) {
            throw new IllegalStateException("ClearView not found");
        }

        view.removeView(mClearView);
    }


    /**
     * Invalidates the screen.
     */
    @Override
    public void invalidate() {
        mClearView.invalidate();
    }

    @NonNull
    @Override
    public String toString() {
        return "Clearing current drawing canvas";
    }

}
