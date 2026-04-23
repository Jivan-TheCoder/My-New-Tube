package org.schabi.newpipe.fragments.list.kiosk;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.ListInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RemoteKioskInfo extends ListInfo<StreamInfoItem> {
    public RemoteKioskInfo(final int serviceId,
                           final String id,
                           final String sourceUrl,
                           final String name,
                           final List<StreamInfoItem> items) {
        super(serviceId, new ListLinkHandler(sourceUrl, sourceUrl, id,
                Collections.emptyList(), null), name);
        setRelatedItems(new ArrayList<>(items));
    }
}
