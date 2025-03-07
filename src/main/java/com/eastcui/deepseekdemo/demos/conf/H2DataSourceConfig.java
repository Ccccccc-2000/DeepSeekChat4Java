package com.eastcui.deepseekdemo.demos.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;

/**
 * 初始化h2数据库
 */
@Slf4j
@Service
@AutoConfigureAfter(DataSource.class) //DataSource创建完后才初始化此类
public class H2DataSourceConfig {

    //初始化sql
    private static final String schema="classpath:db/schema-h2.sql";

    @Autowired
    DataSource dataSource;

    @Autowired
    ApplicationContextRegister applicationContextRegister; //自定义注册器


    // JDK提供的注解,在方法上加该注解会在项目启动的时候执行该方法,也可以理解为在spring容器初始化的时候执行该方法
    @PostConstruct
    public  void init() throws Exception {
        //初始化本地数据库
        String userHome= "db";//获取系统用户目录
        // 创建一个标识文件,只有在第一次初始化数据库时会创建,如果系统用户目录下有这个文件,就不会重新执行sql脚本
        File f = new File(userHome+ File.separator+"my.lock");
        if(!f.exists()){
            log.info("--------------初始化h2数据----------------------");
            f.createNewFile();
            // 加载资源文件
            Resource resource= (Resource) applicationContextRegister.getResource(schema);
            // 手动执行SQL语句
            ScriptUtils.executeSqlScript(dataSource.getConnection(),resource);
        }
    }
}
