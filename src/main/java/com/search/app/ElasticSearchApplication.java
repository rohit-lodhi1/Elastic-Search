package com.search.app;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.search.app.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.search.app.repository.elastic")
public class ElasticSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchApplication.class, args);
	}
	
	   @Bean
	    @Primary
	    public ElasticsearchCustomConversions customConversions() {
	        return new ElasticsearchCustomConversions(
	                Arrays.asList(new LocalDateTimeConverter())
	        );
	    }

	    public static class LocalDateTimeConverter implements Converter<Long, LocalDateTime> {
	        @Override
	        public LocalDateTime convert(Long source) {
	            return LocalDateTime.ofInstant(Instant.ofEpochMilli(source), ZoneOffset.UTC);
	        }
	    }

}
