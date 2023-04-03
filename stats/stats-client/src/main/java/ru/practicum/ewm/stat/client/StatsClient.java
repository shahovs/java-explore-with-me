package ru.practicum.ewm.stat.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stat.dto.HitDtoRequest;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatsClient {
    protected final RestTemplate restTemplate;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public HitShortWithHitsDtoResponse[] getStatArray(LocalDateTime start, LocalDateTime end, String[] uris,
                                                      Boolean unique) {
        Map<String, Object> parameters = getParametersMap(start, end, uris, unique);
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<HitShortWithHitsDtoResponse[]> responseEntity =
                restTemplate.getForEntity(path, HitShortWithHitsDtoResponse[].class, parameters);
        HitShortWithHitsDtoResponse[] result = responseEntity.getBody();
        return result;
    }

    private static Map<String, Object> getParametersMap(LocalDateTime start, LocalDateTime end, String[] uris,
                                                        Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(DATE_TIME_FORMATTER),
                "end", end.format(DATE_TIME_FORMATTER),
                "uris", uris,
                "unique", unique);
        return parameters;
    }

    public List<HitShortWithHitsDtoResponse> getStatList(LocalDateTime start, LocalDateTime end,
                                                                         String[] uris, Boolean unique) {
        Map<String, Object> parameters = getParametersMap(start, end, uris, unique);
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<List<HitShortWithHitsDtoResponse>> responseEntity = restTemplate.exchange(
                path,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<HitShortWithHitsDtoResponse>>() {},
                parameters);
        return responseEntity.getBody();
    }

    // Данный метод - альтернатива двум предыдущим методам getStatArray и getStatList.
    public ResponseEntity<Object> getStatObject(LocalDateTime start, LocalDateTime end, String[] uris,
                                                Boolean unique) {
        Map<String, Object> parameters = getParametersMap(start, end, uris, unique);
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    // две взаимозаменяемых версии метода postStat (используются оба)
    public ResponseEntity<?> postStatMonolith(String app, String uri, String ip, LocalDateTime timestamp) {
        HitDtoRequest hitDtoRequest = new HitDtoRequest(null, app, uri, ip, timestamp.format(DATE_TIME_FORMATTER));
        HttpEntity<HitDtoRequest> httpEntity = new HttpEntity<>(hitDtoRequest);
        ResponseEntity<?> response = restTemplate.postForEntity("/hit", httpEntity, ResponseEntity.class);
        return response;
    }

    public ResponseEntity<Object> postStat(String app, String uri, String ip, LocalDateTime timestamp) {
        HitDtoRequest hitDtoRequest = new HitDtoRequest(null, app, uri, ip, timestamp.format(DATE_TIME_FORMATTER));
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDtoRequest);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path,
                                                          @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, null);

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
