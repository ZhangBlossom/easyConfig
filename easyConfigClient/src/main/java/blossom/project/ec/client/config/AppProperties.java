package blossom.project.ec.client.config;

import blossom.project.ec.client.property.PropertyCenterClient;
import blossom.project.ec.client.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:02
 * AppProperties类
 */
public class AppProperties {
    private static Logger logger = LoggerFactory.getLogger(AppProperties.class);

    private static String projectName;
    private static String environment;
    private static String group;

    private static String serverAddr;
    private static String serverAddrWs;

    //读取所有的可能的配置来源
    static {
        PropertyCenterClient clientEnvironment = new PropertyCenterClient();
        projectName = clientEnvironment.getProjectName();
        environment = clientEnvironment.getEnvironment();
        group = clientEnvironment.getGroup();
        serverAddr = clientEnvironment.getServerAddr();
        serverAddrWs = clientEnvironment.getServerAddrWs();
        if (StringUtils.isNotEmpty(serverAddr)) {
            UrlBuilder.DEFAULT_CONFIG_CENTER_SERVER_ADDR = serverAddr;
        }
        if (StringUtils.isNotEmpty(serverAddrWs)) {
            UrlBuilder.DEFAULT_CONFIG_CENTER_WS_SERVER_ADDR = serverAddrWs;
        }
        if (StringUtils.isBlank(group)) {
            group = "Default";
        }
        if (!StringUtils.isBlank(projectName) && !StringUtils.isBlank(environment)) {
            logger.info("read the property projectName: " + projectName + ", environment: " + environment + ", group" +
                    ":" + " " + group +  ", serverAddr: " + serverAddr + ", serverAddrWs" +
                    ": " + serverAddrWs);
        } else {
            RuntimeException ex =
                    new RuntimeException("无法从环境变量,-D参数,工作目录的配置文件," + "classpath中的配置文件中加载参数,请确认配置信息是否正确" + "." +
                            "特别注意:projectName和environment不能为空");
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public AppProperties() {
    }

    public static String getGroup() {
        return group;
    }

    public static String getProjectName() {
        return projectName;
    }

    public static String getEnvironment() {
        return environment;
    }


}
