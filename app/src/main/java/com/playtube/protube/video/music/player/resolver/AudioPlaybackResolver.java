package com.playtube.protube.video.music.player.resolver;

import static com.playtube.protube.video.music.util.ListHelper.getFilteredAudioStreams;
import static com.playtube.protube.video.music.util.ListHelper.getPlayableStreams;
import static com.playtube.protube.video.music.player.resolver.PlaybackResolver.buildMediaSource;
import static com.playtube.protube.video.music.player.resolver.PlaybackResolver.maybeBuildLiveMediaSource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.source.MediaSource;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;
import com.playtube.protube.video.music.player.helper.PlayerDataSource;
import com.playtube.protube.video.music.player.mediaitem.MediaItemTag;
import com.playtube.protube.video.music.player.mediaitem.StreamInfoTag;
import com.playtube.protube.video.music.util.ListHelper;

import java.util.List;

public class AudioPlaybackResolver implements PlaybackResolver {
    private static final String TAG = AudioPlaybackResolver.class.getSimpleName();

    @NonNull
    private final Context context;
    @NonNull
    private final PlayerDataSource dataSource;
    @Nullable
    private String audioTrack;

    public AudioPlaybackResolver(@NonNull final Context context,
                                 @NonNull final PlayerDataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    /**
     * Get a media source providing audio. If a service has no separate {@link AudioStream}s we
     * use a video stream as audio source to support audio background playback.
     *
     * @param info of the stream
     * @return the audio source to use or null if none could be found
     */
    @Override
    @Nullable
    public MediaSource resolve(@NonNull final StreamInfo info) {
        final MediaSource liveSource = maybeBuildLiveMediaSource(dataSource, info);
        if (liveSource != null) {
            return liveSource;
        }

        final List<AudioStream> audioStreams =
                getFilteredAudioStreams(context, info.getAudioStreams());
        final Stream stream;
        final MediaItemTag tag;

        if (!audioStreams.isEmpty()) {
            final int audioIndex =
                    ListHelper.getAudioFormatIndex(context, audioStreams, audioTrack);
            stream = getStreamForIndex(audioIndex, audioStreams);
            tag = StreamInfoTag.of(info, audioStreams, audioIndex);
        } else {
            final List<VideoStream> videoStreams =
                    getPlayableStreams(info.getVideoStreams(), info.getServiceId());
            if (!videoStreams.isEmpty()) {
                final int index = ListHelper.getDefaultResolutionIndex(context, videoStreams);
                stream = getStreamForIndex(index, videoStreams);
                tag = StreamInfoTag.of(info);
            } else {
                return null;
            }
        }

        try {
            return buildMediaSource(
                    dataSource, stream, info, PlaybackResolver.cacheKeyOf(info, stream), tag);
        } catch (final ResolverException e) {
            Log.e(TAG, "Unable to create audio source", e);
            return null;
        }
    }

    @Nullable
    Stream getStreamForIndex(final int index, @NonNull final List<? extends Stream> streams) {
        if (index >= 0 && index < streams.size()) {
            return streams.get(index);
        }
        return null;
    }

    @Nullable
    public String getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(@Nullable final String audioLanguage) {
        this.audioTrack = audioLanguage;
    }
}
