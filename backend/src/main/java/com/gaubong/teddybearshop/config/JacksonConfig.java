package com.gaubong.teddybearshop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class JacksonConfig {

    /**
     * Configure Jackson ObjectMapper to handle Hibernate lazy loading proxies
     * This prevents errors when serializing entities with FetchType.LAZY
     * Uses Hibernate6Module for Spring Boot 3.x (Jakarta EE)
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = converter.getObjectMapper();
        
        // Register Hibernate6Module to handle lazy-loaded entities (for Spring Boot 3.x)
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        
        // Configure options:
        // - Don't force lazy loading (better performance)
        hibernate6Module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        
        // - Serialize identifier for lazy-loaded entities instead of triggering fetch
        hibernate6Module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        
        mapper.registerModule(hibernate6Module);
        
        return converter;
    }
}
