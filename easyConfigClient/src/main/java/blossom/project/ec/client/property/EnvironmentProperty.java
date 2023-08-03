package blossom.project.ec.client.property;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:08
 * EnvironmentProperty类
 */
public class EnvironmentProperty extends ConfigProperty {
    //从环境变量读取配置
    public EnvironmentProperty() {
        String projectName = System.getenv("ECNAME");
        String environment = System.getenv("ECENVIRONMENT");
        String group = System.getenv("ECGROUP");
        if (StringUtils.isNotBlank(projectName)) {
            this.put("projectName", projectName);
        }

        if (StringUtils.isNotBlank(environment)) {
            this.put("environment", environment);
        }

        if (StringUtils.isNotBlank(group)) {
            this.put("group", group);
        }

    }

}
