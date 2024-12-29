package com.coride.config;

import org.springframework.context.ApplicationContext;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;
import java.util.List;
import java.util.Map;

//让 WebSocket 端点支持 Spring 的依赖注入
public class SpringConfigurator extends Configurator {

    // Spring应用上下文持有器，用于在创建WebSocket端点实例时注入依赖
    private static volatile ApplicationContext context;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringConfigurator.context = applicationContext;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 获取查询参数
        Map<String, List<String>> queryParams = request.getParameterMap();
        // 将查询参数存储在用户属性中
        sec.getUserProperties().put("queryParams", queryParams);
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (context != null) {
            return context.getBean(endpointClass);
        } else {
            return super.getEndpointInstance(endpointClass); // Spring上下文未设置时的备选方案
        }
    }
}
