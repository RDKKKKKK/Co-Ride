package com.coride.service.carpooler.impl;

import com.coride.constant.StatusConstant;
import com.coride.dto.RecordDetailDTO;
import com.coride.entity.CarpoolRequest;
import com.coride.mapper.CarpoolerMapper;
import com.coride.service.carpooler.CarpoolerRideService;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CarpoolerRideServiceImpl implements CarpoolerRideService {
    @Autowired
    private CarpoolerMapper carpoolerMapper;
    @Override
    public void request(CarpoolRequest carpoolRequest) {

        carpoolRequest.setRequestTime(LocalDateTime.now());
        carpoolRequest.setStatus(StatusConstant.SCHEDULED);
        carpoolRequest.setOriginName(getLocationName(carpoolRequest.getOriginLatitude(), carpoolRequest.getOriginLongitude()));
        carpoolRequest.setDestinationName(getLocationName(carpoolRequest.getDestinationLatitude(), carpoolRequest.getOriginLongitude()));
        carpoolRequest.setIdOrganization(carpoolerMapper.getIdOrganizationByUserId(carpoolRequest.getCarpoolerId()));

        carpoolerMapper.requestCarpool(carpoolRequest);
    }

    @Override
    public void cancelRide(Long id, RecordDetailDTO recordDetailDTO) {
        carpoolerMapper.cancel(id, recordDetailDTO.getDepartureTime());
    }

    @Override
    public List<CarpoolRequest> record(Long id) {
        return carpoolerMapper.getCarpoolerRecordsById(id);
    }

    @Override
    public String getCarpoolDriverNameByGroupId(Long id) {
        return carpoolerMapper.getDriverNameByCarpoolGroupId(id);
    }

    public String getLocationName(double latitude, double longitude) {
        try {
            String ak = "GKtfRmYn1AlU6NXy0iWIekzgZdF0SkmG";
            String baseUrl = "http://api.map.baidu.com/reverse_geocoding/v3/?";
            String requestUrl = baseUrl + "ak=" + ak + "&output=json&coordtype=wgs84ll&location=" + latitude + "," + longitude +"&extensions_poi=1&radius=100";
            String poiName = "";

            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 解析响应内容
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject result = jsonResponse.getJSONObject("result");

            // 尝试从pois字段获取简短地点名字
            if (result.has("pois") && result.getJSONArray("pois").length() > 0) {
                JSONArray pois = result.getJSONArray("pois");
                JSONObject poi = pois.getJSONObject(0);
                poiName = poi.getString("name");
                log.info("Found poi: " + poiName);
            }
            return poiName;
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown location";
        }
    }
}
