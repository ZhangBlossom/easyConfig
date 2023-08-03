package blossom.project.ec.client.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:10
 * ConfigProperty类
 */
public class ConfigProperty extends Properties {
    protected Logger logger = LoggerFactory.getLogger(ConfigProperty.class);
    public static final String PROJECT_KEY = "projectName";
    public static final String ENVIRONMENT_KEY = "environment";
    public static final String GROUP_KEY = "group";

    public ConfigProperty() {
    }
}
