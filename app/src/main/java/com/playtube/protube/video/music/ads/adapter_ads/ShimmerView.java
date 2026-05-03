package com.playtube.protube.video.music.ads.adapter_ads;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class ShimmerView extends View {
    private static final int BASE_COLOR = Color.rgb(232, 237, 245);
    private static final int HIGHLIGHT_COLOR = Color.rgb(248, 251, 255);
    private static final long ANIMATION_DURATION_MS = 1200L;

    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private float shimmerOffset;
    private float cornerRadius;
    @Nullable
    private ValueAnimator animator;

    public ShimmerView(Context context) {
        super(context);
        init();
    }

    public ShimmerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShimmerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        basePaint.setColor(BASE_COLOR);
        cornerRadius = getResources().getDisplayMetrics().density * 10f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startShimmer();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopShimmer();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(0f, 0f, w, h);
        startShimmer();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, basePaint);
        shimmerPaint.setShader(new LinearGradient(
                shimmerOffset - getWidth(),
                0f,
                shimmerOffset,
                0f,
                new int[]{Color.TRANSPARENT, HIGHLIGHT_COLOR, Color.TRANSPARENT},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, shimmerPaint);
    }

    private void startShimmer() {
        if (!isAttachedToWindow() || getWidth() <= 0 || animator != null) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, getWidth() * 2f);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            shimmerOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopShimmer() {
        if (animator == null) {
            return;
        }
        animator.cancel();
        animator = null;
        shimmerPaint.setShader(null);
    }
}
