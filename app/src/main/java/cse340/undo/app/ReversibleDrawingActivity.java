package cse340.undo.app;

        import android.annotation.SuppressLint;
        import android.content.res.ColorStateList;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.os.Bundle;
        import android.support.annotation.ColorInt;
        import android.support.annotation.IdRes;
        import android.support.constraint.ConstraintSet;
        import android.view.View;
        import android.view.ViewGroup;

        import java.util.Stack;

        import cse340.undo.R;
        import cse340.undo.actions.AbstractAction;
        import cse340.undo.actions.ChangeColorAction;
        import cse340.undo.actions.ChangeThicknessAction;
        import cse340.undo.actions.AbstractReversibleAction;
        import cse340.undo.actions.ClearAction;
        import cse340.undo.history.StackHistory;

public class ReversibleDrawingActivity extends AbstractReversibleDrawingActivity {
    // Default color of the color picker
    private static final int DEFAULT_COLOR = Color.RED;
    // Default thickness of the stroke
    private static final int DEFAULT_THICKNESS = 10;
    // The key to obtain the color stored onDestroy call
    private final String COLOR_BUNDLE_KEY = "coloUr";
    // The key to obtain the thickness stored onDestroy call
    private final String THICK_BUNDLE_KEY = "thicc";

    /** List of menu item FABs for thickness menu. */
    @IdRes
    private static final int[] THICKNESS_MENU_ITEMS = {
            R.id.fab_thickness_0, R.id.fab_thickness_10, R.id.fab_thickness_20, R.id.fab_thickness_30
    };

    /** List of menu item FABs for color menu. */
    @IdRes
    private static final int[] COLOR_MENU_ITEMS = {};

    private ViewGroup mClearMenu;

    /** State variables used to track whether menus are open. */
    private boolean isThicknessMenuOpen;
    private boolean isColorMenuOpen;

    @SuppressLint("PrivateResource")
    private int mMiniFabSize;

    /** Place to store ColorPickerView */
    protected AbstractColorPickerView mColorPickerView;

    // Color change listener
    private AbstractColorPickerView.ColorChangeListener mColorChangeListener = new AbstractColorPickerView.ColorChangeListener() {
        @Override
        public void onColorSelected(int color) {
            updateColor(color);
        }
    };

    /**
     * Creates a new AbstractReversibleDrawingActivity with the default history limit.
     */
    public ReversibleDrawingActivity() {
        super();
    }

    /**
     * Creates a new AbstractReversibleDrawingActivity with the given history limit.
     *
     * @param history Maximum number of history items to maintain.
     */
    public ReversibleDrawingActivity(int history) {
        super(history);
    }

    @Override
    @SuppressLint("PrivateResource")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClearMenu = (ViewGroup) getLayoutInflater().inflate(
                R.layout.clear_menu, mLayout, false
        );
        addMenu(mClearMenu, ConstraintSet.TOP, ConstraintSet.START);
        findViewById(R.id.fab_clear).setOnClickListener((v) -> clear());
        updateMenuButtons();

        // Color picker
        mColorPickerView = findViewById(R.id.circleColorPicker);
        mColorPickerView.addColorChangeListener(mColorChangeListener);

        // We are providing draw with a default thickness and color for the first line
        Paint p = mDrawingView.getCurrentPaint();
        p.setColor(((CircleColorPickerView) mColorPickerView).getColor());
        p.setStrokeWidth(DEFAULT_THICKNESS);
        mDrawingView.setCurrentPaint(p);
        mMiniFabSize = getResources().getDimensionPixelSize(R.dimen.design_fab_size_mini);


        // Add thickness and color menus to the ConstraintLayout. Pass in onColorMenuSelected
        // and onThicknessMenuSelected as the listeners for these menus
        addCollapsibleMenu(
                R.layout.color_menu, ConstraintSet.BOTTOM,
                ConstraintSet.END, COLOR_MENU_ITEMS, this::onColorMenuSelected
        );
        findViewById(R.id.fab_color).setOnClickListener((v) -> {
            enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, isColorMenuOpen);
            isColorMenuOpen = toggleMenu(COLOR_MENU_ITEMS, isColorMenuOpen);
        });

        // Only draw a stroke when none of the collapsible menus are open
        mDrawingView.setOnTouchListener((view, event) -> {
            if (isThicknessMenuOpen) {
                isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
                enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, !isThicknessMenuOpen);
                return true;
            } else if (isColorMenuOpen) {
                isColorMenuOpen = toggleMenu(COLOR_MENU_ITEMS, isColorMenuOpen);
                enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isColorMenuOpen);
                return true;
            } else {
                return mDrawingView.onTouchEvent(event);
            }
        });

        registerActionUndoListener(this::onActionUndo);

        addCollapsibleMenu(
                R.layout.thickness_menu, ConstraintSet.BOTTOM, ConstraintSet.END,
                THICKNESS_MENU_ITEMS, this::onThicknessMenuSelected
        );
        findViewById(R.id.fab_thickness).setOnClickListener((v) ->{
            enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, isThicknessMenuOpen);
            isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
        });

        setStartingColor(savedInstanceState);
    }

    /**
     * Clears all strokes on DrawingView
     */
    private void clear() {
        ClearAction action = new ClearAction();
        doAction(action);
    }

    /** {@inheritDoc}*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deregisterActionUndoListener(this::onActionUndo);
        mColorPickerView.removeColorChangeListener(mColorChangeListener);
    }

    // Reverts color of fab and color picker back to previous state
    private void onActionUndo(AbstractReversibleAction action) {
        if (action instanceof ChangeColorAction) {
            @ColorInt int currColor = mDrawingView.getCurrentPaint().getColor();
            mColorPickerView.setColor(currColor);
            findViewById(R.id.fab_color).setBackgroundTintList(
                    ColorStateList.valueOf(currColor)
            );
        }
    }

    /** {@inheritDoc}*/
    @Override
    protected void updateMenuButtons() {
        super.updateMenuButtons();
        setViewVisibility(mClearMenu, ((StackHistory) mModel).didClear());
    }

    /** {@inheritDoc}*/
    @Override
    protected void doAction(AbstractAction action) {
        if (action == null) {
            return;
        }
        // The following removes all StrokeViews behind a ClearView
        AbstractReversibleAction botAction = ((StackHistory) mModel).peekBottom();
        if ((botAction instanceof ClearAction) &&
                (((StackHistory) mModel).size() == DEFAULT_HISTORY_SIZE)) {
            Stack<AbstractReversibleAction> reAddToCanvas = new Stack<>();
            // Undoes everything in stack, including ClearView and adds to new stack
            while (mModel.canUndo()) {
                AbstractReversibleAction undidAction = mModel.undo();
                undidAction.undoAction(mDrawingView);
                reAddToCanvas.add(undidAction);
            }
            mDrawingView.removeAllViews();
            // Removes the ClearAction and redoes all prior actions
            reAddToCanvas.pop();
            while (!reAddToCanvas.isEmpty()) {
                doAction(reAddToCanvas.pop());
            }
        }
        super.doAction(action);
    }


    /**
     * Private helper function to update the view after the color in the model has changed
     *
     * @param color The new color
     */
    private void updateColor(int color) {
        doAction(new ChangeColorAction(color));
        findViewById(R.id.fab_color).setBackgroundTintList(
                ColorStateList.valueOf(color)
        );
        isColorMenuOpen = toggleMenu(COLOR_MENU_ITEMS, isColorMenuOpen);
        enableCollapsibleMenu(
                R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isColorMenuOpen
        );
    }

    /**
     * Does nothing. Legacy for when COLOR_MENU_ITEMS is nonempty.
     *
     * @param view The FAB the user clicked on
     */
    private void onColorMenuSelected(View view) { }

    /**
     * Callback for creating an action when the user changes the thickness.
     *
     * @param view The FAB the user clicked on.
     */
    private void onThicknessMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.fab_thickness_0:
                doAction(new ChangeThicknessAction(0));
                break;
            case R.id.fab_thickness_10:
                doAction(new ChangeThicknessAction(10));
                break;
            case R.id.fab_thickness_20:
                doAction(new ChangeThicknessAction(20));
                break;
            case R.id.fab_thickness_30:
                doAction(new ChangeThicknessAction(30));
                break;
        }

        // Close the menu.
        isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
        enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS , !isThicknessMenuOpen);
    }

    /**
     * Toggles a collapsible menu. That is, if it's open, it closes it. If it's closed, it opens it.
     *
     * @param items List of IDs of items in the menu, all FABs.
     * @param open  Current state of the menu.
     * @return True if the menu is now open, false otherwise.
     */
    private boolean toggleMenu(@IdRes int[] items, boolean open) {
        enableFAB(R.id.fab_undo, open);
        enableFAB(R.id.fab_redo, open);
        enableFAB(R.id.fab_clear, open);
        if ((!open) && (items.length == 0)) {
            mColorPickerView.setVisibility(View.VISIBLE);
        } else {
            mColorPickerView.setVisibility(View.INVISIBLE);
        }
        if (!open) {
            for (int i = 0; i < items.length; i++) {
                View view = findViewById(items[i]);
                view.animate()
                        .translationY(-3 * mMiniFabSize * (i + 1.5f) / 2.5f)
                        .alpha(1)
                        .withEndAction(() -> view.setClickable(true));
            }
            return true;
        } else {
            for (int item : items) {
                View view = findViewById(item);
                view.setClickable(false);
                view.animate().translationY(0).alpha(0);
            }
            return false;
        }
    }

    /**
     * Disables and enables collapsible menu FABs
     *
     * @param menuId The resID of the menu activation FAB
     * @param menuItems An array of resIDs for the menu item FABs
     * @param enabled true if the menu should be enabled, false if the menu should be disabled
     */
    private void enableCollapsibleMenu(@IdRes int menuId, @IdRes int[] menuItems, boolean enabled) {
        enableFAB(menuId, enabled);
        for (@IdRes int item : menuItems) {
            findViewById(item).setEnabled(enabled);
        }
    }

    /**
     * Disables and enables FABs
     *
     * @param buttonId the resID of the FAB
     * @param enabled true if the button should be enabled, false if the button should be disabled
     */
    private void enableFAB(@IdRes int buttonId, boolean enabled) {
        findViewById(buttonId).setEnabled(enabled);
        if (buttonId != R.id.fab_color) {
            findViewById(buttonId).setBackgroundTintList(ColorStateList.valueOf(enabled ?
                getResources().getColor(R.color.colorAccent) : Color.LTGRAY));
        } else {
            findViewById(buttonId).setBackgroundTintList(ColorStateList.valueOf(enabled ?
                    ((CircleColorPickerView) mColorPickerView).getColor() : Color.LTGRAY));
        }
    }

    /**
     * Sets the starting color of the brush used for mDrawingView and the picker.
     *
     * @param state Bundled state to extract previous color from or null for default.
     */
    protected void setStartingColor(Bundle state) {
        int startingColor;
        if (state == null) {
            startingColor = AbstractColorPickerView.DEFAULT_COLOR;
        } else {
            startingColor = state.getInt(COLOR_BUNDLE_KEY);
        }
        ((AbstractColorPickerView) findViewById(R.id.circleColorPicker)
        ).setColor(startingColor);
        findViewById(R.id.fab_color).setBackgroundTintList(
                ColorStateList.valueOf(startingColor)
        );
        mDrawingView.getCurrentPaint().setColor(startingColor);
    }

    /**
     * Invoked when the activity may be temporarily destroyed, save the instance state here.
     * @param outState State to save out through the bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // only stores the color and thickness of the brush
        outState.putInt(COLOR_BUNDLE_KEY,
                ((CircleColorPickerView) mColorPickerView).getColor());
        outState.putInt(THICK_BUNDLE_KEY,
                ((int) mDrawingView.getCurrentPaint().getStrokeWidth()));
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // only restores the color and thickness of the brush
        int startingColor = savedInstanceState.getInt(COLOR_BUNDLE_KEY);
        ((AbstractColorPickerView) findViewById(R.id.circleColorPicker)
        ).setColor(startingColor);
        mDrawingView.getCurrentPaint().setStrokeWidth(
                (int) savedInstanceState.get(THICK_BUNDLE_KEY)
        );
    }
}
