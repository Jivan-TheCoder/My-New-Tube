package com.playtube.protube.video.music.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.Preference;

import com.playtube.protube.video.music.DownloaderImpl;
import com.playtube.protube.video.music.R;
import org.schabi.newpipe.extractor.NewPipe;
import com.playtube.protube.video.music.player.helper.PlayerHelper;
import com.playtube.protube.video.music.util.InfoCache;
import com.playtube.protube.video.music.util.Localization;
import com.playtube.protube.video.music.util.image.ImageStrategy;
import com.playtube.protube.video.music.util.image.PreferredImageQuality;

import java.util.Locale;

import coil3.SingletonImageLoader;

public class ContentSettingsFragment extends BasePreferenceFragment {
    private String youtubeRestrictedModeEnabledKey;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        youtubeRestrictedModeEnabledKey = getString(R.string.youtube_restricted_mode_enabled);

        addPreferencesFromResourceRegistry();
        hideUnusedPreferences();

        setupAppLanguagePreferences();
        setupContentLocalizationPreferences();
        setupImageQualityPref();
    }

    private void hideUnusedPreferences() {
        hidePreference(R.string.main_page_content_key);
        hidePreference(R.string.show_channel_tabs_key);
        hidePreference(R.string.show_age_restricted_content);
        hidePreference(R.string.youtube_restricted_mode_enabled);
        hidePreference(R.string.show_search_suggestions_key);
        hidePreference(R.string.show_meta_info_key);
        hideFeedCategory();
    }

    private void hidePreference(final int keyResId) {
        final Preference preference = findPreference(getString(keyResId));
        if (preference != null) {
            preference.setVisible(false);
        }
    }

    private void hideFeedCategory() {
        final Preference feedCategory = findPreference("content_feed_category");
        if (feedCategory != null) {
            feedCategory.setVisible(false);
        }
    }

    private void setupAppLanguagePreferences() {
        final Preference appLanguagePref = requirePreference(R.string.app_language_key);
        // Android 13+ allows to set app specific languages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appLanguagePref.setVisible(false);

            final Preference newAppLanguagePref =
                    requirePreference(R.string.app_language_android_13_and_up_key);
            newAppLanguagePref.setSummaryProvider(preference -> {
                final Locale loc = AppCompatDelegate.getApplicationLocales().get(0);
                return loc != null ? loc.getDisplayName() : getString(R.string.systems_language);
            });
            newAppLanguagePref.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                        .setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                startActivity(intent);
                return true;
            });
            newAppLanguagePref.setVisible(true);
            return;
        }

        appLanguagePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final String language = (String) newValue;
            final String systemLang = getString(R.string.default_localization_key);
            final String tag = systemLang.equals(language) ? null : language;
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
            return true;
        });
    }

    private void setupImageQualityPref() {
        requirePreference(R.string.image_quality_key).setOnPreferenceChangeListener(
            (preference, newValue) -> {
                ImageStrategy.setPreferredImageQuality(PreferredImageQuality
                    .fromPreferenceKey(requireContext(), (String) newValue));
                final var loader = SingletonImageLoader.get(preference.getContext());
                loader.getMemoryCache().clear();
                loader.getDiskCache().clear();
                Toast.makeText(preference.getContext(),
                                R.string.thumbnail_cache_wipe_complete_notice, Toast.LENGTH_SHORT)
                        .show();
                return true;
            });
    }

    private void setupContentLocalizationPreferences() {
        requirePreference(R.string.content_language_key).setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    applyContentLocalization(preference.getContext(), (String) newValue, null);
                    return true;
                });
        requirePreference(R.string.content_country_key).setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    applyContentLocalization(preference.getContext(), null, (String) newValue);
                    return true;
                });
    }

    private void applyContentLocalization(final Context context) {
        applyContentLocalization(context, null, null);
    }

    private void applyContentLocalization(final Context context,
                                          final String newLanguageCode,
                                          final String newCountryCode) {
        final String defaultKey = context.getString(R.string.default_localization_key);
        final String languageCode = newLanguageCode != null
                ? newLanguageCode
                : Localization.getPreferredLocale(context).toLanguageTag();
        final String countryCode = newCountryCode != null
                ? newCountryCode
                : Localization.getPreferredContentCountry(context).getCountryCode();

        final Locale preferredLocale = defaultKey.equals(languageCode)
                ? Locale.getDefault()
                : Locale.forLanguageTag(languageCode);
        final String resolvedCountryCode = defaultKey.equals(countryCode)
                ? Locale.getDefault().getCountry()
                : countryCode;

        NewPipe.setupLocalization(
                org.schabi.newpipe.extractor.localization.Localization.fromLocale(
                        preferredLocale),
                new org.schabi.newpipe.extractor.localization.ContentCountry(
                        resolvedCountryCode));
        InfoCache.getInstance().clearCache();
        PlayerHelper.resetFormat();
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if (preference.getKey().equals(youtubeRestrictedModeEnabledKey)) {
            final Context context = getContext();
            if (context != null) {
                DownloaderImpl.getInstance().updateYoutubeRestrictedModeCookies(context);
            } else {
                Log.w(TAG, "onPreferenceTreeClick: null context");
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final Context context = requireContext();
        applyContentLocalization(context);
    }
}
