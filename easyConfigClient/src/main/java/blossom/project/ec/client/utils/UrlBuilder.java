package blossom.project.ec.client.utils;

/**
 * Created by 张锦标
 * 用于获取最后的配置中心url
 *
 */
public class UrlBuilder {

    public static  String DEFAULT_CONFIG_CENTER_SERVER_ADDR = "localhost:52013";
    public static  String DEFAULT_CONFIG_CENTER_WS_SERVER_ADDR = "localhost:52014";

    private static String userConfigCenterAddr;
    private static String userConfigCenterUrlWsAddr;

    static {
        //得到配置中心的SERVER地址
        userConfigCenterAddr = String.format("http://%s/",
               DEFAULT_CONFIG_CENTER_SERVER_ADDR);

        userConfigCenterUrlWsAddr = String.format("http://%s/",
                DEFAULT_CONFIG_CENTER_WS_SERVER_ADDR);
    }
    public static String getWebSocketUrl() {
        return userConfigCenterUrlWsAddr;
    }

    public static String getServerAddrUrl() {
        return userConfigCenterAddr;
    }

    public static String getConfigUrl(String projectName, String env, String group) {
        if (group == null || group.equals("")) {
            group = "Default";
        }
        return userConfigCenterAddr + encoder("easyconfig/center/") +
                encoder(env) + "/" + encoder(group)
                + "/" + encoder(projectName);
    }

    private static String encoder(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (Throwable ignored) {
        }
        return null;
    }
}
