package com.vuln.board.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * React Server Components(RSC) 스트림 API.
 * /api/rsc/** 요청을 RSC 렌더 서버로 프록시하여, 단일 포트(8080/8888)로 RSC 기능을 제공한다.
 */
@RestController
@RequestMapping("/api/rsc")
public class RscProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rsc.server.url}")
    private String rscServerUrl;

    @RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/rsc")) {
            path = path.substring("/api/rsc".length());
        }
        if (path.isEmpty()) {
            path = "/";
        }
        String query = request.getQueryString();
        String targetUrl = rscServerUrl.replaceAll("/$", "") + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase("Host") && !name.equalsIgnoreCase("Connection"))
                .forEach(name -> headers.add(name, request.getHeader(name)));

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> entity = new HttpEntity<>(body != null ? body : new byte[0], headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(targetUrl),
                    method,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                if (!key.equalsIgnoreCase("Transfer-Encoding")) {
                    values.forEach(v -> responseHeaders.add(key, v));
                }
            });
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (ResourceAccessException e) {
            // RSC 서버 연결 실패 (연결 거부, 타임아웃 등)
            String msg = "RSC server unreachable: " + rscServerUrl + " — " + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"" + msg.replace("\"", "\\\"") + "\"}").getBytes(StandardCharsets.UTF_8));
        } catch (HttpServerErrorException e) {
            // RSC 서버가 5xx 반환 (역직렬화 오류 등)
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            // 기타 예외 (로그용 메시지 포함해 500 반환)
            String msg = "RSC proxy error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"" + msg.replace("\"", "\\\"") + "\"}").getBytes(StandardCharsets.UTF_8));
        }
    }
}
