package blossom.project.ec.client.property;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:06
 * ConfigCenterClient类
 */
public class PropertyCenterClient {

    private List<ConfigProperty> propertyList = new CopyOnWriteArrayList();

    public PropertyCenterClient() {
        this.propertyList.add(new EnvironmentProperty());
        this.propertyList.add(new SystemProperty());
        this.propertyList.add(new UserDirProperty());
        this.propertyList.add(new ClassPathProperty());
    }

    private String getByOrder(String name) {
        Iterator i$ = this.propertyList.iterator();

        String value;
        do {
            if (!i$.hasNext()) {
                return null;
            }

            ConfigProperty propertySources = (ConfigProperty)i$.next();
            value = (String)propertySources.get(name);
        } while(!StringUtils.isNotBlank(value));

        return value;
    }

    public String getEnvironment() {
        return this.getByOrder("environment");
    }

    public String getProjectName() {
        return this.getByOrder("projectName");
    }

    public String getGroup() {
        return this.getByOrder("group");
    }
    public String getServerAddr() {
        return this.getByOrder("serverAddr");
    }
    public String getServerAddrWs() {
        return this.getByOrder("serverAddrWs");
    }

}
