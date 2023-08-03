package blossom.project.ec.client.property;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author: 张锦标
 * @date: 22023/6/30 11:09
 * ClassPathProperty类
 * 这里的代码负责读取类资源目录下面的easyconfig.properties文件
 * 读取其中几个配置 这些配置读取到之后就可以到对应的配置中心获取配置了
 */
public class ClassPathProperty extends ConfigProperty {
    public ClassPathProperty() {
        try {
            InputStream stream = ClassPathProperty.class
                    .getClassLoader().getResourceAsStream("easyconfig.properties");
            Throwable var2 = null;

            try {
                if (stream != null) {
                    Properties prop = new Properties();
                    prop.load(stream);
                    String projectName = prop.getProperty("easy.config.projectName");
                    String environment = prop.getProperty("easy.config.environment");
                    String group = prop.getProperty("easy.config.group");
                    String serverAddr = prop.getProperty("easy.config.serverAddr");
                    String serverAddrWs = prop.getProperty("easy.config.serverAddrWs");
                    if (StringUtils.isNotBlank(serverAddr)) {
                        this.put("serverAddr", serverAddr);
                    }
                    if (StringUtils.isNotBlank(serverAddrWs)) {
                        this.put("serverAddrWs", serverAddrWs);
                    }
                    if (StringUtils.isNotBlank(projectName)) {
                        this.put("projectName", projectName);
                    }
                    if (StringUtils.isNotBlank(environment)) {
                        this.put("environment", environment);
                    }

                    if (StringUtils.isNotBlank(group)) {
                        this.put("group", group);
                    }

                    return;
                }

                this.logger.warn("there is not exist easy.config.properties file under classPath.");
            } catch (Throwable var16) {
                var2 = var16;
                throw var16;
            } finally {
                if (stream != null) {
                    if (var2 != null) {
                        try {
                            stream.close();
                        } catch (Throwable var15) {
                            var2.addSuppressed(var15);
                        }
                    } else {
                        stream.close();
                    }
                }

            }

        } catch (Throwable var18) {
            this.logger.warn("read easy.config.properties file under classPath error!", var18);
        }
    }
}
