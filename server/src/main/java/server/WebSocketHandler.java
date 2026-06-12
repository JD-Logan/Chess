package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;


public class WebSocketHandler {
    private final DataAccess dataAccess;
    private final Gson gson;

    public WebSocketHandler(DataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    public void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    public void onMessage(WsMessageContext ctx) {

    }

    public void onClose(WsCloseContext ctx) {

    }
}
