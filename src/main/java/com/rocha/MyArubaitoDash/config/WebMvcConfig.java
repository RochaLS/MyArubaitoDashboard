package com.rocha.MyArubaitoDash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new RequestInterceptor()).addPathPatterns("/**").excludePathPatterns(List.of("/login", "/api/worker/add"));
//    }
}
