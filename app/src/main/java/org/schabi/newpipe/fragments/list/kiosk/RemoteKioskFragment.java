package org.schabi.newpipe.fragments.list.kiosk;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.evernote.android.state.State;

import org.schabi.newpipe.AppMode;
import org.schabi.newpipe.R;
import org.schabi.newpipe.error.ErrorInfo;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.fragments.list.BaseListInfoFragment;
import org.schabi.newpipe.local.remotekiosk.RemoteKioskRepository;
import org.schabi.newpipe.util.KioskTranslator;
import org.schabi.newpipe.util.OnClickGesture;

import io.reactivex.rxjava3.core.Single;

public class RemoteKioskFragment extends BaseListInfoFragment<StreamInfoItem, RemoteKioskInfo> {
    @State
    String kioskId = "";
    String kioskTranslatedName;

    public static RemoteKioskFragment getInstance(final String kioskId) {
        final RemoteKioskFragment instance = new RemoteKioskFragment();
        instance.kioskId = kioskId;
        instance.setInitialData(
                ServiceList.YouTube.getServiceId(),
                AppMode.getKioskOpenUrl(kioskId),
                kioskId
        );
        return instance;
    }

    public RemoteKioskFragment() {
        super(UserAction.REQUESTED_KIOSK);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kioskTranslatedName = KioskTranslator.getTranslatedKioskName(kioskId, requireContext());
        name = kioskTranslatedName;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kiosk, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null && useAsFrontPage) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected Single<RemoteKioskInfo> loadResult(final boolean forceReload) {
        return RemoteKioskRepository.loadKioskInfo(requireContext(), kioskId, kioskTranslatedName);
    }

    @Override
    protected Single<ListExtractor.InfoItemsPage<StreamInfoItem>> loadMoreItemsLogic() {
        return Single.error(new IllegalStateException("Remote kiosk pagination is not supported"));
    }

    @Override
    protected boolean hasMoreItems() {
        return false;
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        infoListAdapter.setOnStreamSelectedListener(new OnClickGesture<>() {
            @Override
            public void selected(final StreamInfoItem selectedItem) {
                openInYoutube(selectedItem.getUrl());
            }

            @Override
            public void held(final StreamInfoItem selectedItem) {
                showInfoItemDialog(selectedItem);
            }
        });
    }

    @Override
    public void handleResult(@NonNull final RemoteKioskInfo result) {
        super.handleResult(result);
        name = kioskTranslatedName;
        setTitle(kioskTranslatedName);
    }

    private void openInYoutube(final String url) {
        final Uri uri = Uri.parse(url);
        final Intent appIntent = new Intent(Intent.ACTION_VIEW, uri);
        appIntent.setPackage("com.google.android.youtube");
        try {
            startActivity(appIntent);
        } catch (final ActivityNotFoundException ignored) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (final Exception e) {
                showSnackBarError(new ErrorInfo(e, UserAction.UI_ERROR, "Open remote kiosk item"));
            }
        }
    }
}
