package blossom.project.ec.client.utils;

import java.util.List;

/**
 * Created by 张锦标 on 2023/5/19.
 */
public class CmdRequest {
    private String cmdId;
    private String env;
    private String cmdName;
    private String serverRoom;
    private List<String> args;

    public String getServerRoom() {
        return serverRoom;
    }

    public void setServerRoom(String serverRoom) {
        this.serverRoom = serverRoom;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getCmdId() {
        return cmdId;
    }

    public void setCmdId(String cmdId) {
        this.cmdId = cmdId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }
}
