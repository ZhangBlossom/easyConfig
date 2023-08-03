package blossom.project.easyconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author: 张锦标
 * @date: 22023/6/30 9:01
 * EastConfigApplication类
 */
@EnableAsync
@SpringBootApplication
public class EasyConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyConfigServerApplication.class,args);
        System.out.println("----------EasyConfig项目启动成功-----------");
    }
}
