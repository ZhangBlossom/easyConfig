package blossom.project.ec.client.config;

/**
 * Created by cm41643 on 2023/2/7.
 *
 */

public class ConfigVersion {

    private String projectName;

    private long version = -1;

    private String env;

    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public ConfigVersion() {
    }

    public ConfigVersion(String projectName, String env, String serverRoom) {
        this.projectName = projectName;
        this.env = env;
        this.group = serverRoom;
    }

    public ConfigVersion(String projectName, long version, String env) {
        this.projectName = projectName;
        this.version = version;
        this.env = env;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigVersion)) {
            return false;
        }

        ConfigVersion that = (ConfigVersion) o;

        if (version != that.version) {
            return false;
        }
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) {
            return false;
        }
        return env != null ? env.equals(that.env) : that.env == null;
    }

    @Override
    public int hashCode() {
        int result = projectName != null ? projectName.hashCode() : 0;
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + (env != null ? env.hashCode() : 0);
        return result;
    }
}
