package com.bstek.ureport.sample;

import com.bstek.ureport.Utils;
import com.bstek.ureport.definition.datasource.BuildinDatasource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Description 内置数据源
 * @Author hans
 * @CreateDate 2022-9-6
 */
@Component("innerDS-mysql")
public class BuildinReportDS implements BuildinDatasource{

    @Autowired
    private DataSource dataSource;

    @Override
    public String name() {
        return "innerDs";
    }

    @Override
    public Connection getConnection() {
        // 获取配置的数据源
        Connection connection= null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }
}
