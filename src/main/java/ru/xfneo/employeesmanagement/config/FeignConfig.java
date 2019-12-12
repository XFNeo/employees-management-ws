package ru.xfneo.employeesmanagement.config;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.xfneo.employeesmanagement.client.DepartmentClient;

@Configuration
public class FeignConfig {

    @Value("${departments.service.url}")
    private String departmentServiceUrl;

    @Bean
    public DepartmentClient departmentClientService() {
        return Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Logger.ErrorLogger())
                .logLevel(Logger.Level.FULL)
                .target(DepartmentClient.class, departmentServiceUrl);
    }

}
