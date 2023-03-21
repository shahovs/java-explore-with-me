package ru.practicum.ewm.stat.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stat.dto.HitDtoRequest;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import java.util.Map;

public class StatsClient {
    protected final RestTemplate restTemplate;

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // todo можно улучшить; поменять start timestamp на LocalDateTime; тогда для вызова этого метода
    public HitShortWithHitsDtoResponse[] getStatArray(String start, String end, String[] uris, Boolean unique) {
        Map<String, Object> parameters = Map.of("start", start, "end", end, "uris", uris, "unique", unique);
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<HitShortWithHitsDtoResponse[]> responseEntity =
                restTemplate.getForEntity(path, HitShortWithHitsDtoResponse[].class, parameters);
        HitShortWithHitsDtoResponse[] result = responseEntity.getBody();
        return result;
    }

    // этот метод - альтернатива предыдущему методу getStatArray;
    // работать с возвращаемым значением ResponseEntity<Object> не так удобно,
    // поэтому метод в коде не вызывается, но код рабочий, метод проверен

    // todo попробовать заменить тип на ResponseEntity<List<HitShortWithHitsDtoResponse>>
    // и использовать метод template.getForObject вместо exchange (то есть ближе к предыщему методу)
    // еще один вариант - возвращать ParameterizedTypeReference<List<HitShortWithHitsDtoResponse>>
    // https://www.baeldung.com/spring-resttemplate-json-list
    public ResponseEntity<Object> getStatObject(String start, String end, String[] uris, Boolean unique) {
        Map<String, Object> parameters = Map.of("start", start, "end", end, "uris", uris, "unique", unique);
        String path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    // две взаимозаменяемых версии метода postStat (используются обе)

    // todo можно улучшить; поменять String timestamp на LocalDateTime; тогда для вызова этого метода
    // не нужно будет конвертировать дату в строку;
    // для замены нужно будет также поменять тип поля в классе HitDtoRequest
    // тогда по идее у нас будет здесь происходить сериализация HitDtoRequest в json строку в обычном формате
    // даты (с буковой Т), а в контроллере статистики будет происходить десериализация json строки
    // в дату; по идее везде должны действовать настройки по умолчанию (с буквой Т)
    // но нужно проверить, что настройки главного сервиса (класс AppConfig) не вмешиваются в этот процесс
    // иначе мы отсюда отправим дату в формате с пробелом вместо T, а контроллер статистики не сможет
    // это десериализовать // todo по-английски будет Monolith
    // todo не забыть про class HitMapper
    public ResponseEntity<?> postStatMonolit(String app, String uri, String ip, String timestamp) {
        HitDtoRequest hitDtoRequest = new HitDtoRequest(null, app, uri, ip, timestamp);
        HttpEntity<HitDtoRequest> httpEntity = new HttpEntity<>(hitDtoRequest);
        ResponseEntity<?> response = restTemplate.postForEntity("/hit", httpEntity, ResponseEntity.class);
        return response;
    }

    public ResponseEntity<Object> postStat(String app, String uri, String ip, String timestamp) {
        HitDtoRequest hitDtoRequest = new HitDtoRequest(null, app, uri, ip, timestamp);
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
