package net.linlan.stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 * Application main for spring boot jar
 * Filename:EntApplication.java
 * Desc: the main entrance of WDC to run, to debug
 *
 * @author Linlan
 * CreateTime:2017-10-15 2:32 PM
 *
 * @version 1.0
 * @since 1.0
 *
 */
@SpringBootApplication
public class EntApplication extends SpringBootServletInitializer
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(EntApplication.class, args);
        System.out.println("------- 平台启动成功 -------");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EntApplication.class);
    }

}