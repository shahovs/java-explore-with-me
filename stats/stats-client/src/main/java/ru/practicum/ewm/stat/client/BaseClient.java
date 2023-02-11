package ru.practicum.ewm.stat.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.stat.dto.HitDtoRequest;

import java.util.Map;

public class BaseClient {
    protected final RestTemplate restTemplate;

    public BaseClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory("${stats-server.url}"))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> getStat(String start, String end, String[] uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start, "end", end, "uris", uris, "unique", unique
        );
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    public ResponseEntity<Object> postStat(String app, String uri, String ip, String timestamp) {
        HitDtoRequest hitDtoRequest = new HitDtoRequest();
        hitDtoRequest.setApp(app);
        hitDtoRequest.setUri(uri);
        hitDtoRequest.setIp(ip);
        hitDtoRequest.setTimestamp(timestamp);
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDtoRequest);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path,
                                                          @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body);

        ResponseEntity<Object> responseEntity;
        try {
            if (parameters != null) {
                responseEntity = restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                responseEntity = restTemplate.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(responseEntity);
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

}
