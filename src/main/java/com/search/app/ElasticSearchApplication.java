package com.search.app;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.search.app.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.search.app.repository.elastic")
public class ElasticSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchApplication.class, args);
	}

	@Bean
	public ElasticsearchCustomConversions elasticsearchCustomConversions() {
		return new ElasticsearchCustomConversions(
				Arrays.asList(new LocalDateTimeToLongConverter(), new LongToLocalDateTimeConverter()));
	}
}

class LocalDateTimeToLongConverter implements Converter<LocalDateTime, Long> {
	@Override
	public Long convert(LocalDateTime source) {
		return source.atZone(ZoneId.systemDefault()) // Add ZoneId
				.toInstant().toEpochMilli();
	}
}

class LongToLocalDateTimeConverter implements Converter<Long, LocalDateTime> {
	@Override
	public LocalDateTime convert(Long source) {
		return Instant.ofEpochMilli(source) // Create Instant from epoch millis
				.atZone(ZoneId.systemDefault()) // Add ZoneId
				.toLocalDateTime();
	}
}