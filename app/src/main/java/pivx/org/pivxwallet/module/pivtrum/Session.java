package pivx.org.pivxwallet.module.pivtrum;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * Created by furszy on 6/12/17.
 */

public class Session {

    private int sessionId;
    private Socket socket;

    public Session(int sessionId,Socket socket) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    /**
     * Write json objects
     *
     * @param jsonObject
     * @throws IOException
     */
    public void write(JSONObject jsonObject) throws IOException {
        if (!socket.isOutputShutdown()) {
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(jsonObject.toString()+'\n');
            bw.flush();
        }else {
            throw new IllegalArgumentException("socket output stream is not enabled");
        }
    }


}
