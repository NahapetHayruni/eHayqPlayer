package am.ehayq.ehayqplayer;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.UdpDataSource;


public class MainActivity extends AppCompatActivity {
    StyledPlayerView exoPlayerView;

    // creating a variable for exoplayer
    private ExoPlayer exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FFmpeg.cancel();
        exoPlayerView = findViewById(R.id.player_view);
        Button b = findViewById(R.id.playButton);
        b.setOnClickListener(view -> {
            FFmpeg.cancel();
            EditText mEdit   = (EditText)findViewById(R.id.editTextTextEmailAddress);
            String video_path = mEdit.getText().toString();
            String cmd = "-re -i " + video_path + " -c:v copy -tune zerolatency -preset ultrafast -f mpegts udp://127.0.0.1:2000";
            FFmpeg.executeAsync(cmd,
                    (executionId, returnCode) -> {
                        if (returnCode == RETURN_CODE_SUCCESS) {
                            Log.i(Config.TAG, "Command execution completed successfully.");
                        } else if (returnCode == RETURN_CODE_CANCEL) {
                            Log.i(Config.TAG, "Command execution cancelled by user.");
                        } else {
                            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", returnCode));
                            Config.printLastCommandOutput(Log.INFO);
                        }
                    });
        });
        try {
            exoPlayer = new ExoPlayer.Builder(this).build();
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayerView.requestFocus();
            UdpDataSource streamingSource = new UdpDataSource();
            DataSource.Factory udpDataSourceFactory = () -> streamingSource;
            MediaSource udpMediaSource = new ProgressiveMediaSource.Factory(udpDataSourceFactory,TsExtractor.FACTORY)
                    .createMediaSource(MediaItem.fromUri(Uri.parse("tcp://192.168.11.241:8000")));
                    //.createMediaSource(MediaItem.fromUri(Uri.parse("udp://localhost:2000")));
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.setMediaSource(udpMediaSource);
            exoPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}