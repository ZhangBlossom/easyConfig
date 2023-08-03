package blossom.project.easyconfig.config;

import blossom.project.ec.db.datasource.AcceptableDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author: 张锦标
 * @date: 22023/7/5 15:10
 * DataBaseConfig类
 */
@Configuration
public class DataBaseConfig {
    //向容器注入数据源
    @Bean("acceptableDataSource")
    public AcceptableDataSource acceptableDataSource(){
        AcceptableDataSource ads = new AcceptableDataSource();
        ads.setDbName("towelove");
        ads.init();
        ads.setConnectionProperties(new HashMap<String, String>() {{
            put("serverTimezone", "GMT%2B8");
        }});
        return ads;
    }
}
