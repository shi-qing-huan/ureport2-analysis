package com.bstek.ureport.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.bstek.ureport.console.UReportServlet;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ImportResource(locations={"classpath:context.xml"})
public class Config {

	@Bean
    public ServletRegistrationBean servletRegistrationBean() {
        // ServletName默认值为首字母小写，即myServlet1
        return new ServletRegistrationBean(new UReportServlet(), "/ureport/*");
    }

    @Primary
    @Bean(name = "defaultDs")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource createDefaultDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Autowired
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate createJdbcTemplate(@Qualifier("defaultDs") DataSource defaultDataSource) {
        return new JdbcTemplate(defaultDataSource);
    }

}
