package blossom.project.ec.client.property;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:09
 * UserDirProperty类
 */
public class UserDirProperty extends ConfigProperty {
    public UserDirProperty() {
        try {
            String path = System.getProperty("user.dir");
            String proFilePath = path + File.separator + "easyconfig.properties";
            File file = new File(proFilePath);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(proFilePath);
                Properties prop = new Properties();
                prop.load(fis);
                String projectName = prop.getProperty("easy.config.projectName");
                String environment = prop.getProperty("easy.config.environment");
                String group = prop.getProperty("easy.config.group");
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
        } catch (Throwable var9) {
            this.logger.warn("read easyconfig.properties file under user.dir error", var9);
        }

    }
}
