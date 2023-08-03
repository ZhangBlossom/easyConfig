package blossom.project.ec.client.config;



import java.util.ArrayList;
import java.util.List;

public class ChangeConfigData {

    private String env;
    private List<ConfigData> addList;

    private String projectName;

    private long version;

    private List<ConfigData> updateList;

    private List<ConfigData> removeList;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<ConfigData> getAddList() {
        return addList;
    }


    public List<ConfigData> getUpdateList() {
        return updateList;
    }


    public List<ConfigData> getRemoveList() {
        return removeList;
    }


    public ChangeConfigData() {
        this.addList = new ArrayList<>();
        this.updateList = new ArrayList<>();
        this.removeList = new ArrayList<>();
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

    @Override
    public String toString() {
        return "ChangeConfigData{" +
                "env='" + env + '\'' +
                ", addList=" + addList +
                ", projectName='" + projectName + '\'' +
                ", version=" + version +
                ", updateList=" + updateList +
                ", removeList=" + removeList +
                '}';
    }
}
