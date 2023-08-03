package blossom.project.ec.client.config;

public class ConfigData {

    private String key;
    private String value;
    private String env;
    private String group;
    private String version;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ConfigData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public ConfigData() {

    }
}
