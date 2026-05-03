package com.playtube.protube.video.music.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowInsetsCompat;

import com.playtube.protube.video.music.R;

public final class FocusAwareCoordinator extends CoordinatorLayout {
    private final Rect childFocus = new Rect();

    public FocusAwareCoordinator(@NonNull final Context context) {
        super(context);
    }

    public FocusAwareCoordinator(@NonNull final Context context,
                                 @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusAwareCoordinator(@NonNull final Context context,
                                 @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestChildFocus(final View child, final View focused) {
        super.requestChildFocus(child, focused);

        if (!isInTouchMode()) {
            if (focused.getHeight() >= getHeight()) {
                focused.getFocusedRect(childFocus);

                ((ViewGroup) child).offsetDescendantRectToMyCoords(focused, childFocus);
            } else {
                focused.getHitRect(childFocus);

                ((ViewGroup) child).offsetDescendantRectToMyCoords((View) focused.getParent(),
                        childFocus);
            }

            requestChildRectangleOnScreen(child, childFocus, false);
        }
    }

    /**
     * Applies window insets to all children, not just for the first who consume the insets.
     * Makes possible for multiple fragments to co-exist. Without this code
     * the first ViewGroup who consumes will be the last who receive the insets
     */
    @Override
    public WindowInsets dispatchApplyWindowInsets(final WindowInsets insets) {
        boolean consumed = false;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final WindowInsets res = child.dispatchApplyWindowInsets(insets);
            if (res.isConsumed()) {
                consumed = true;
            }
        }

        return consumed ? WindowInsetsCompat.CONSUMED.toWindowInsets() : insets;
    }

    /**
     * Adjusts player's controls manually because onApplyWindowInsets doesn't work when multiple
     * receivers adjust its bounds. So when two listeners are present (like in profile page)
     * the player's controls will not receive insets. This method fixes it
     */
    @Override
    public WindowInsets onApplyWindowInsets(final WindowInsets windowInsets) {
        final var windowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets, this);
        final var insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
        final ViewGroup controls = findViewById(R.id.playbackControlRoot);
        if (controls != null) {
            controls.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
        return super.onApplyWindowInsets(windowInsets);
    }
}
