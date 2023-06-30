package blossom.project.easyconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author: 张锦标
 * @date: 2023/6/30 9:01
 * EastConfigApplication类
 */
@EnableAsync
@SpringBootApplication
public class EasyConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyConfigApplication.class,args);
        System.out.println("----------EasyConfig项目启动成功-----------");
    }
}
