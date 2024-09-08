package com.coride;


import com.coride.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtTool;

    private final JwtProperties jwtProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取Request
        ServerHttpRequest request = exchange.getRequest();
        // 2.判断是否不需要拦截
        if(isExclude(request.getPath().toString())){
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
        // 3.获取请求头中的token
        String token = null;
        List<String> headers = request.getHeaders().get("token");
        if (!headers.isEmpty()) {
            token = headers.get(0);
        }
        // 4.校验并解析token
        Claims userId = null;
        try {
            userId = jwtTool.parseJWT(jwtProperties.getAdminSecretKey(), token);
            log.info("Successfully verified");
        } catch (Exception e) {
            // 如果无效，拦截
            ServerHttpResponse response = exchange.getResponse();

            response.setRawStatusCode(401);
            log.info("JWT verification failed");
            return response.setComplete();
        }

        // TODO 5.如果有效，传递用户信息
        System.out.println("userId = " + userId);
        // 6.放行
        return chain.filter(exchange);
    }

    private boolean isExclude(String antPath) {
        for (String pathPattern : jwtProperties.getExcludedPath()) {
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}