package org.schabi.newpipe.ads;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.schabi.newpipe.R;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.ArrayList;
import java.util.List;

public final class RatingManager {
    private static final String PREF_NAME = "MyAppRatings";
    private static final String KEY_RATING_DONE = "user_rating_done";

    private RatingManager() { }

    public interface RateDialogListener {
        void onRateDialogDismiss(boolean ratedNow);
    }

    public static void setRating(@NonNull final Context context) {
        final SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_RATING_DONE, true).apply();
    }

    public static boolean getRating(@NonNull final Context context) {
        final SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_RATING_DONE, false);
    }

    public static void rateDialog(@NonNull final Activity activity,
                                  @NonNull final RateDialogListener listener) {
        if (getRating(activity)) {
            listener.onRateDialogDismiss(false);
            return;
        }

        final Dialog rateDialog = new Dialog(activity, R.style.full_dialog);
        rateDialog.setCancelable(false);
        rateDialog.setCanceledOnTouchOutside(false);

        final View view = LayoutInflater.from(activity).inflate(R.layout.rate_dialog, null);
        rateDialog.setContentView(view);

        final LottieAnimationView lottieView = view.findViewById(R.id.rate_lottie);
        final TextView tvRate = view.findViewById(R.id.tv_rate);
        final TextView tvLater = view.findViewById(R.id.tv_later);
        final List<ImageView> stars = new ArrayList<>(5);
        stars.add(view.findViewById(R.id.iv_star1));
        stars.add(view.findViewById(R.id.iv_star2));
        stars.add(view.findViewById(R.id.iv_star3));
        stars.add(view.findViewById(R.id.iv_star4));
        stars.add(view.findViewById(R.id.iv_star5));

        // Uses a hosted lottie animation; if loading fails, hide the animation view gracefully.
        lottieView.setAnimationFromUrl(
                "https://assets2.lottiefiles.com/packages/lf20_touohxv0.json");
        lottieView.loop(true);
        lottieView.playAnimation();
        lottieView.addLottieOnCompositionLoadedListener(composition -> lottieView.setVisibility(View.VISIBLE));
//        lottieView.addLottieOnFailureListener(result -> lottieView.setVisibility(View.GONE));

        final int selectedColor = ContextCompat.getColor(activity, R.color.hower_color);
        final int unselectedColor = ContextCompat.getColor(activity, R.color.gray);
        final int[] selectedRating = {0};

        final View.OnClickListener starClick = clickedView -> {
            final int clickedIndex = stars.indexOf(clickedView) + 1;
            if (clickedIndex <= 0) {
                return;
            }
            selectedRating[0] = clickedIndex;
            renderStars(stars, selectedRating[0], selectedColor, unselectedColor);
        };
        for (final ImageView star : stars) {
            star.setOnClickListener(starClick);
        }
        renderStars(stars, 0, selectedColor, unselectedColor);

        tvRate.setOnClickListener(v -> {
            if (selectedRating[0] <= 0) {
                Toast.makeText(activity, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            setRating(activity);
            rateDialog.dismiss();

            if (selectedRating[0] >= 4) {
                showRateApp(activity);
            } else {
                Toast.makeText(activity,
                        "Thanks for your feedback. We will keep improving.",
                        Toast.LENGTH_SHORT).show();
            }
            listener.onRateDialogDismiss(true);
        });

        tvLater.setOnClickListener(v -> {
            rateDialog.dismiss();
            listener.onRateDialogDismiss(false);
        });

        rateDialog.show();
    }

    private static void renderStars(@NonNull final List<ImageView> stars, final int selectedCount,
                                    final int selectedColor, final int unselectedColor) {
        for (int i = 0; i < stars.size(); i++) {
            final ImageView star = stars.get(i);
            final boolean selected = i < selectedCount;
            star.setImageResource(selected ? R.drawable.ic_star_filled : R.drawable.ic_stars);
            star.setColorFilter(selected ? selectedColor : unselectedColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public static void showRateApp(@NonNull final Activity activity) {
        final ReviewManager reviewManager = ReviewManagerFactory.create(activity);
        final Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                final ReviewInfo reviewInfo = task.getResult();
                reviewManager.launchReviewFlow(activity, reviewInfo);
            } else {
                Toast.makeText(activity, "Unable to open review right now", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
