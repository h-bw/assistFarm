package com.huacai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author huacai
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class HuaCaiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(HuaCaiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  助农扶贫后台管理启动成功   ლ(´ڡ`ლ)ﾞ " +
                ".__                                 .__\n" +
                "|  |__  __ _______      ____ _____  |__|\n" +
                "|  |  \\|  |  \\__  \\   _/ ___\\\\__  \\ |  |\n" +
                "|   Y  \\  |  // __ \\_ \\  \\___ / __ \\|  |\n" +
                "|___|  /____/(____  /  \\___  >____  /__|\n" +
                "     \\/           \\/       \\/     \\/"
        );
    }
}
