package com.miaoshaproject;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
//@EnableAutoConfiguration 改为：项目下的包依次往下做扫描
@SpringBootApplication(scanBasePackages = {"com.miaoshaproject"})
@MapperScan("com.miaoshaproject.dao")//表示扫描xx.xx.mapper包下的所有mapper。
@RestController
public class App 
{
    @Autowired
    private UserDOMapper userDOMapper;
    @RequestMapping("/")
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO== null){
            return "用户不存在";
        }
        else{
            return userDO.getName();
        }

    }
    public static void main(String[] args)
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);//tomcat starts on ports: 8080.,启动了一个内嵌的tomcat容器，并且加载进去默认的配置
    }
}
