package com.coride;

import com.coride.config.SpringConfigurator;
import com.coride.constant.JwtClaimsConstant;
import com.coride.dto.CarpoolRequestDTO;
import com.coride.dto.ConfirmDTO;
import com.coride.dto.RideOfferDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import com.coride.entity.ConfirmationState;
import com.coride.properties.JwtProperties;
import com.coride.message.MessagingServiceTemplate;
import com.coride.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import static com.coride.service.driver.impl.DriverRideServiceImpl.confirmations;


@Component
@ServerEndpoint(value = "/request/ws", configurator = SpringConfigurator.class)
@Slf4j
@AllArgsConstructor
public class RideWebSocketServer {
    public static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private MessagingServiceTemplate messagingServiceTemplate;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 从配置中获取HTTP请求参数
        Map<String, List<String>> queryParams = (Map<String, List<String>>) config.getUserProperties().get("queryParams");

        // 获取token和accountType参数
        String token = queryParams.get("token").get(0);
        String accountType = queryParams.get("accountType").get(0);
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        String id = claims.get(JwtClaimsConstant.USER_ID).toString();
        log.info(accountType + " ID=" + id + " established connection.");
        switch (accountType){
            case "Carpooler":
                id = "C" + id;
                break;
            case "Driver":
                id = "D" + id;
        }
        sessionMap.put(id, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) throws JsonProcessingException, JSONException {
        JSONObject jsonObject = new JSONObject(message);
        String messageType = jsonObject.getString("messageType");

        switch (messageType){
            case "request":
                rideRequest(message);
                break;

            case "confirm":
                rideConfirm(message);
                break;

        }

      }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        String userIdToRemove = null;
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().equals(session)) {
                userIdToRemove = entry.getKey();
                break;
            }
        }
        if (userIdToRemove != null) {
            sessionMap.remove(userIdToRemove);
            log.info("Session removed for user: " + userIdToRemove);
        }
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * receieve and process ride request from driver and carpooler
     * @param message
     * @throws JSONException
     * @throws JsonProcessingException
     */


    public void rideRequest(String message) throws JSONException, JsonProcessingException {
        JSONObject jsonObject = new JSONObject(message);
        String accountType = jsonObject.getString("accountType");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Claims claims = null;
        Long id = null;

        switch (accountType){
            case "Carpooler":
                CarpoolRequestDTO carpoolRequestDTO = mapper.readValue(message, CarpoolRequestDTO.class);

                CarpoolRequest carpoolRequest = new CarpoolRequest();
                BeanUtils.copyProperties(carpoolRequestDTO, carpoolRequest);

                claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), carpoolRequestDTO.getToken());
                id = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                carpoolRequest.setCarpoolerId(id);

                //carpoolerRideService.request(carpoolRequest);
                messagingServiceTemplate.sendPassengerRequest(carpoolRequest);

                break;

            case "Driver":
                RideOfferDTO rideOfferDTO = mapper.readValue(message, RideOfferDTO.class);
                CarpoolGroup carpoolGroup = new CarpoolGroup();
                BeanUtils.copyProperties(rideOfferDTO, carpoolGroup);

                claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), rideOfferDTO.getToken());
                id = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                carpoolGroup.setIdDriver(id);

                //driverRideService.rideMatch(carpoolGroup, sessionMap);
                messagingServiceTemplate.sendDriverRequest(carpoolGroup);

                break;
        }
    }

    public void rideConfirm(String message) throws JsonProcessingException {
        messagingServiceTemplate.sendConfirmation(message);

    }

}
