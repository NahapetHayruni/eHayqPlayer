package am.ehayq.ehayqplayer;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class TcpDataSource extends BaseDataSource {

    public static final class TcpDataSourceException extends DataSourceException {
        public TcpDataSourceException(Throwable cause, @PlaybackException.ErrorCode int errorCode) {
            super(cause, errorCode);
        }
    }

    public static final int TCP_PORT_UNSET = -1;
    public static final int TIMEOUT = 5000;

    @Nullable private Uri uri;
    @Nullable private Socket socket;
    @Nullable private InputStream reader;
    private boolean opened;

    public TcpDataSource() {
        super(/* isNetwork= */ true);
    }

    @Override
    public long open(DataSpec dataSpec) throws TcpDataSourceException {
        uri = dataSpec.uri;
        String host = checkNotNull(uri.getHost());
        int port = uri.getPort();
        transferInitializing(dataSpec);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            reader = socket.getInputStream();
        } catch (SecurityException e) {
            throw new TcpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NO_PERMISSION);
        } catch (IOException e) {
            throw new TcpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
        }
        opened = true;
        transferStarted(dataSpec);
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws TcpDataSourceException {
        if (length == 0) {
            return 0;
        }
        try {
            return reader.read(buffer, offset, length);
        } catch (IOException e) {
            throw new TcpDataSourceException(e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED);
        }
    }

    @Override
    @Nullable
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() {
        uri = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
        if (opened) {
            opened = false;
            transferEnded();
        }
    }

    public int getLocalPort() {
        if (socket == null) {
            return TCP_PORT_UNSET;
        }
        return socket.getLocalPort();
    }
}