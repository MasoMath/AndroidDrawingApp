package cse340.undo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

/**
 * Simple little view which clears the current canvas.
 */
@SuppressLint("ViewConstructor")
public class ClearView extends View {
    /**
     * Constructor class
     * @param context the context of this new view
     */
    public ClearView(Context context) {
        super(context);
    }

    /**
     * Clears the canvas to the default Android light
     * theme color.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#FAFAFA"));
    }
}
