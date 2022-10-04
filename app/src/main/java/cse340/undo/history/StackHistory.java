package cse340.undo.history;

import android.support.annotation.NonNull;

import java.util.Deque;
import java.util.LinkedList;

import cse340.undo.actions.AbstractReversibleAction;
import cse340.undo.actions.ClearAction;
import cse340.undo.app.ClearView;

/**
 * Keeps a history of actions that have been done and undone using two stacks. When an item is done,
 * it is pushed onto the undo stack. When an item is undone, it is popped from the undo stack and
 * pushed to the redo stack. The number of history items is limited by the capacity.
 */
public class StackHistory implements AbstractStackHistory {
    /** Data structures for staring undo/redo events. */
    private final Deque<AbstractReversibleAction> mUndoStack, mRedoStack;

    /** Should always be true that mUndoStack.size() + mRedoStack.size() <= capacity. */
    private final int mCapacity;

    /**
     * Initializes empty undo/redo stacks.
     *
     * @param capacity  Maximum size of undo/redo stacks.
     * @throws IllegalStateException if capacity is not positive.
     */
    public StackHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        this.mCapacity = capacity;

        mUndoStack = new LinkedList<>();
        mRedoStack = new LinkedList<>();
    }

    /**
     * Add a reversible event to the history.
     *
     * @param action    Reversible action to be added.
     */
    @Override
    public void addAction(AbstractReversibleAction action) {
        if (mUndoStack.size() + 1 > mCapacity) {
            mUndoStack.removeLast();
        }
        mUndoStack.addFirst(action);
        mRedoStack.clear();
    }

    /**
     * Undoes an action.
     *
     * @return null if there is nothing to undo, otherwise the action to be undone.
     */
    @Override
    public AbstractReversibleAction undo() {
        if (mUndoStack.size() == 0) {
            return null;
        } else {
            AbstractReversibleAction action = mUndoStack.removeFirst();
            mRedoStack.addFirst(action);
            return action;
        }
    }

    /**
     * Redoes an action.
     *
     * @return null if there is nothing to redo, otherwise the action to be redone.
     */
    @Override
    public AbstractReversibleAction redo() {
        if (mRedoStack.size() == 0) {
            return null;
        } else {
            AbstractReversibleAction action = mRedoStack.removeFirst();
            mUndoStack.addFirst(action);
            return action;
        }
    }

    /**
     * Clears the history.
     */
    @Override
    public void clear() {
        mUndoStack.clear();
        mRedoStack.clear();
    }

    /**
     * This method returns the oldest action in the stack,
     * which is to say at the bottom of the stack
     *
     * @return A AbstractReversibleAction object that is the oldest in the stack
     * or null if empty
     */
    public AbstractReversibleAction peekBottom() {
        return mUndoStack.peekLast();
    }

    /**
     * This method returns the last action in the added,
     * which is to say at the top of the stack
     *
     * @return A AbstractReversibleAction object that was most recently added
     * or null if empty
     */
    public AbstractReversibleAction peekTop() {
        return mUndoStack.peekFirst();
    }

    /**
     * @return Returns true if the StackHistory has just received a ClearAction.
     * False otherwise or if the StackHistory can't undo.
     */
    public boolean didClear() {
        return !(peekTop() instanceof ClearAction) && canUndo();
    }

    public int size() {
        return mUndoStack.size();
    }

    /**
     * Is there anything that can be undone?
     *
     * @return True if can undo any actions, false otherwise.
     */
    @Override
    public boolean canUndo() {
        return !mUndoStack.isEmpty();
    }

    /**
     * Is there anything that can be done?
     *
     * @return True if can redo any actions, false otherwise.
     */
    @Override
    public boolean canRedo() {return !mRedoStack.isEmpty();}

    @NonNull
    public String toString() {
        return  "Undo size: " + mUndoStack.size() + ", redo size: " + mRedoStack.size();
    }
}
