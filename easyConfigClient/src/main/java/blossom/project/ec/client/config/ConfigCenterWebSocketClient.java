package blossom.project.ec.client.config;

//import com.alibaba.fastjson.JSON;

import blossom.project.easyconfig.websocket.client.WebSocketClient;
import blossom.project.easyconfig.websocket.drafts.Draft_17;
import blossom.project.easyconfig.websocket.handshake.ServerHandshake;
import blossom.project.ec.client.utils.CmdResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URI;
import java.util.function.Consumer;

/**
 * Created by 张锦标 on 2023/5/8.
 *
 */
class ConfigCenterWebSocketClient extends WebSocketClient {

    private Logger logger= LoggerFactory.getLogger(ConfigCenterWebSocketClient.class);
    private Consumer<String> projectChange;
    private Consumer<String> projectKeyChange;
    private Consumer<String> smallFlowChange;

    void setProjectChange(Consumer<String> projectChange) {
        this.projectChange = projectChange;
    }

    public void setSmallFlowChange(Consumer<String> smallFlowChange) {
        this.smallFlowChange = smallFlowChange;
    }

    public void setProjectKeyChange(Consumer<String> projectKeyChange) {
        this.projectKeyChange = projectKeyChange;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    /**
     * 消息监听事件
     * @param msg
     */
    @Override
    public void onMessage(String msg) {

        try {
            Gson gson=new Gson();
            CmdResponse response = gson.fromJson(msg, CmdResponse.class);
            String cmdName =response.getCmd();
            if (cmdName != null && cmdName.equals("projectconfigchange")) {
                String projectName = response.getValue();
                if (projectName != null && projectChange != null){
                    projectChange.accept(projectName);
                }
            }
            if (cmdName != null && cmdName.equals("projectkeyconfigchange")) {
                String projectAndKey = response.getValue();
                if (projectAndKey != null && projectKeyChange != null){
                    projectKeyChange.accept(projectAndKey);
                }
            }

            if (cmdName != null && cmdName.equals("smallFlowconfigchange")) {
                String smallFlowConfig = response.getValue();
                if (smallFlowConfig != null && projectChange != null){
                    smallFlowChange.accept(smallFlowConfig);
                }
            }
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    ConfigCenterWebSocketClient(String serverUri) throws Exception {
        super(new URI(serverUri), new Draft_17(), null, 5000);
    }
}
