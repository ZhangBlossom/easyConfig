package blossom.project.ec.client.config;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by 张锦标 on 2023/5/13.
 *
 */
public class ConfigCenterClient {

    static {
        init();
    }

    public static void init() {
        ConfigCenterService.init();
    }


    public static void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged != null) {
            ConfigCenterService.addConfigChange(configChanged);
        }
    }

    public static void removeConfigChanged(ConfigChanged configChanged) {
        if (configChanged != null) {
            ConfigCenterService.removeConfigChange(configChanged);
        }
    }


    public static String GetStatusInfo() {
        return ConfigCenterService.getStatus();
    }

    /**
     * 获取当前项目，当前环境key
     */
    public static String get(String key) throws Exception {
        Preconditions(key);
        return ConfigCenterService.get(key);
    }

    /**
     * 获取指定项目，和当前项目相同环境下的指定key
     */
    public static String get(String projectName, String key) throws Exception {
        Preconditions(projectName, key);
        return ConfigCenterService.get(projectName, AppProperties.getEnvironment(), key);
    }

    /**
     * 获取指定项目，指定环境下的指定key
     */
    public static String get(String projectName, String env, String key) throws Exception {
        Preconditions(projectName, env, key);
        return ConfigCenterService.get(projectName, env, key);
    }


    public static HashMap<String, String> getAllConfig() throws Exception {
        return ConfigCenterService.getAllConfig(AppProperties.getProjectName());
    }

    /**
     * 参数校验
     * @param params 参数
     * @throws Exception
     */
    private static void Preconditions(String... params) throws Exception {
        for (String p : params) {
            if (StringUtils.isBlank(p)) {
                throw new Exception("参数不能为空或者空字符串");
            }
        }
    }
}
