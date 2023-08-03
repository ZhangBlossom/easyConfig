package blossom.project.ec.client.config;

import java.util.List;

/**
 * 张锦标
 * 项目配置类
 *
 */
public class ProjectConfig {

    private String projectName;
    private List<ConfigData> config;
    private long version;
    private String env;
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<ConfigData> getConfig() {
        return config;
    }

    public void setConfig(List<ConfigData> config) {
        this.config = config;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
