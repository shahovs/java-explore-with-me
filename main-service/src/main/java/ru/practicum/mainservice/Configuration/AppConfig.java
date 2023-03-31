package ru.practicum.mainservice.Configuration;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.stat.client.StatsClient;

import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.simpleDateFormat(DATE_TIME_FORMAT);
            jacksonObjectMapperBuilder.serializers(
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            jacksonObjectMapperBuilder.deserializers(
                    new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        };
    }

    @Bean
    public StatsClient baseClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
        return new StatsClient(restTemplate);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
