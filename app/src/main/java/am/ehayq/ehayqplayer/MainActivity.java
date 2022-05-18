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


public class MainActivity extends AppCompatActivity {
    StyledPlayerView exoPlayerView;

    // creating a variable for exoplayer
    private ExoPlayer exoPlayer;
    public static final int PORT_MIN = 2000;
    public static final int PORT_MAX = 8000;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FFmpeg.cancel();
        exoPlayerView = findViewById(R.id.player_view);
        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayerView.setPlayer(exoPlayer);
        exoPlayerView.requestFocus();
        Button b = findViewById(R.id.playButton);
        port = PORT_MIN;
        Log.e("LLL", "" + port);
        b.setOnClickListener(view -> {
            FFmpeg.cancel();
            ++port;
            if (port > PORT_MAX) {
                port = PORT_MIN;
            }
            Log.e("LLL", "" + port);
            EditText mEdit   = (EditText)findViewById(R.id.editTextTextEmailAddress);
            String video_path = mEdit.getText().toString();
            String cmd = "-re -i " + video_path +  " -c:v copy -f mpegts tcp://localhost:" + port + "\\?listen";
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
            try {
                TcpDataSource streamingSource = new TcpDataSource();
                DataSource.Factory tcpDataSourceFactory = () -> streamingSource;
                MediaSource tcpMediaSource = new ProgressiveMediaSource.Factory(tcpDataSourceFactory,TsExtractor.FACTORY)
                        .createMediaSource(MediaItem.fromUri(Uri.parse("tcp://localhost:" + port)));
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.setMediaSource(tcpMediaSource);
                exoPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}