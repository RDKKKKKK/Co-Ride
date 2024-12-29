package com.coride.mapper;

import com.coride.dto.CarpoolerInfoDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import com.coride.entity.Carpooler;
import com.coride.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CarpoolerMapper {

    @Insert("insert into carpooler (idCarpooler) values (#{idUser})")
    void insert(User user);

    @Insert("insert into carpool_request (idCarpooler, originLongitude, originLatitude, time, destinationLongitude, destinationLatitude, status, originName, destinationName, originPoint, destinationPoint, departureTime, idOrganization) " +
            " VALUES (#{carpoolerId}, #{originLongitude}, #{originLatitude}, #{requestTime}, #{destinationLongitude}, #{destinationLatitude}, #{status}, #{originName}, #{destinationName}, POINT(#{originLongitude}, #{originLatitude}), POINT(#{destinationLongitude}, #{destinationLatitude}), #{departureTime}, #{idOrganization})")
    void requestCarpool(CarpoolRequest carpoolRequest);

    @Select("select originName, destinationName from carpool_request where idCarpooler = #{idUser} order by time DESC limit 1 ")
    CarpoolerInfoDTO getInfo(Long idUser);

    @Select("select originName from carpool_request where idCarpooler = #{idUser} order by time DESC limit 1 " )
    String getOriginById(Long idUser);

    @Select("select destinationName from carpool_request where idCarpooler = #{idUser} order by time DESC limit 1 " )
    String getDestinationById(Long idUser);

    @Update("update carpool_request set status = #{status} where idCarpooler = #{newPassenger} and status != 'Cancelled' order by departureTime desc limit 1")
    void updateCarpoolerStatus(Long newPassenger, String status);

    @Update("update carpool_request set idCarpoolGroup = (select idCarpoolGroup from carpool_group where idDriver = #{driverId} order by time DESC limit 1) where idCarpooler = #{passengerId} order by time DESC limit 1")
    void addToCarpoolGroup(Long passengerId, Long driverId);

    @Select("select * from user inner join carpooler c on user.idUser = c.idCarpooler where idUser = #{matchedId}")
    Carpooler findById(Long matchedId);

    @Select("SELECT c.idCarpooler " +
            "FROM carpool_request c " +
            "WHERE c.status = 'Available' AND c.idOrganization = #{idOrganization} " +
            "AND ST_Distance_Sphere(c.originPoint, POINT(#{originLongitude}, #{originLatitude})) < 5000 " +
            "AND ST_Distance_Sphere(c.destinationPoint, POINT(#{destinationLongitude}, #{destinationLatitude})) < 5000 " +
            "ORDER BY ST_Distance_Sphere(c.originPoint, POINT(#{originLongitude}, #{originLatitude})) + " +
            "         ST_Distance_Sphere(c.destinationPoint, POINT(#{destinationLongitude}, #{destinationLatitude})) " +
            "ASC LIMIT 1 FOR UPDATE")
    Long findBestMatchID(CarpoolGroup carpoolGroup);

    @Update("update carpool_request set status = #{available} where status = 'Scheduled' and departureTime between #{now} and #{tenMinutesLater}")
    void refreshAllStatus(LocalDateTime now, LocalDateTime tenMinutesLater, String available);

    @Select("select name from user where idUser = #{id}")
    String getCarpoolerNameById(Long id);

    @Select("select * from carpooler c inner join user u on c.idCarpooler = u.idUser where idUser = #{id}")
    Carpooler getCarpoolerAccountById(Long id);

    @Select("select * from carpool_request where idCarpooler = #{id} order by departureTime desc")
    List<CarpoolRequest> getCarpoolerRecordsById(Long id);

    @Select("select name from user inner join driver d on user.idUser = d.idDriver where d.idDriver = " +
            "(select idDriver from carpool_request r inner join carpool_group cg on r.idCarpoolGroup = cg.idCarpoolGroup where r.idCarpoolRequest = #{id})")
    String getDriverNameByCarpoolGroupId(Long id);

    @Update("update carpool_request set status = 'Cancelled' where idCarpooler = #{id} and departureTime = #{departureTime}")
    void cancel(Long id, String departureTime);

    @Select("select idOrganization from user where idUser = #{carpoolerId}")
    int getIdOrganizationByUserId(Long carpoolerId);
}
