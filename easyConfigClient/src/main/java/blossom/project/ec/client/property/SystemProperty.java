package blossom.project.ec.client.property;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:09
 * SystemProperty类
 */
public class SystemProperty extends ConfigProperty {
    //读取项目system配置
    public SystemProperty() {
        String projectName = System.getProperty("projectName");
        String environment = System.getProperty("environment");
        String group = System.getProperty("group");
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
