package com.playtube.protube.video.music.fragments.list.kiosk;

import android.os.Bundle;
import android.util.Log;

import com.playtube.protube.video.music.R;
import com.playtube.protube.video.music.error.ErrorInfo;
import com.playtube.protube.video.music.error.UserAction;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import com.playtube.protube.video.music.util.ServiceHelper;

public class DefaultKioskFragment extends KioskFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (serviceId < 0) {
            updateSelectedDefaultKiosk();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (serviceId != ServiceHelper.getSelectedServiceId(requireContext())) {
            if (currentWorker != null) {
                currentWorker.dispose();
            }
            updateSelectedDefaultKiosk();
            reloadContent();
        }
    }

    private void updateSelectedDefaultKiosk() {
        try {
            serviceId = ServiceHelper.getSelectedServiceId(requireContext());

            final KioskList kioskList = NewPipe.getService(serviceId).getKioskList();
            kioskId = kioskList.getDefaultKioskId();

            if (serviceId == 0) {
                kioskId = "trending_gaming";
            } else {
                kioskId = kioskList.getDefaultKioskId();
            }
            url = kioskList.getListLinkHandlerFactoryByType(kioskId).fromId(kioskId).getUrl();
            Log.e("DefaultKioskFragment", "jivan url: " + url);

            kioskTranslatedName = getString(R.string.trending);
            name = kioskTranslatedName;
            Log.e("DefaultKioskFragment", "jivan kioskTranslatedName: " + kioskTranslatedName);
            currentInfo = null;
            currentNextPage = null;
        } catch (final ExtractionException e) {
            showError(new ErrorInfo(e, UserAction.REQUESTED_KIOSK,
                    "Loading default kiosk for selected service"));
        }
    }
}
