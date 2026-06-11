package org.hikikomori.community.config;

import org.hikikomori.community.search.dto.SearchDto;
import org.hikikomori.community.search.model.SortType;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, SearchDto.Type.class,
                s -> SearchDto.Type.valueOf(s.trim().toUpperCase()));
        registry.addConverter(String.class, SortType.class,
                s -> SortType.valueOf(s.trim().toUpperCase()));
    }
}
