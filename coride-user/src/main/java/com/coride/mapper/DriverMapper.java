package com.coride.mapper;

import com.coride.dto.DriverRecordDetailDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Driver;
import com.coride.entity.User;
import com.coride.entity.Vehicle;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Mapper
public interface DriverMapper {
    @Insert("insert into driver (idDriver) values (#{idUser})")
    void insert(User user);


    @Select("select plateNo from vehicle inner join has_vehicle hv on vehicle.idVehicle = hv.idVehicle where idDriver = #{id}")
    List<String> getVehiclesByDriverId(Long id);

    @Select("select seats from vehicle where plateNo = #{plateNo}")
    Integer getSeats(String plateNo);

    @Select("select * from vehicle where plateNo = #{plateNo}")
    Vehicle getVehiclesByPlateNo(String plateNo);

    @Select("select * from user where idUser = #{driverId}")
    Driver getDriverById(Long driverId);

    @Insert("insert into carpool_group (idDriver, plateNo, originLongitude, originLatitude, time, seatsAvailable, destinationLongitude, destinationLatitude, status, oiginPoint, destinationPoint, departureTime, originName, destinationName) " +
            "VALUES (#{idDriver}, #{plateNo}, #{originLongitude}, #{originLatitude}, #{requestTime}, #{seatsAvailable}, #{destinationLongitude}, #{destinationLatitude}, #{status}, POINT(#{originLongitude}, #{originLatitude}), POINT(#{destinationLongitude}, #{destinationLatitude}), #{departureTime}, #{originName}, #{destinationName})")
    void insertCarpoolGroup(CarpoolGroup carpoolGroup);

    @Update("update carpool_group set status = #{status} where idDriver = #{id} and status != 'Cancelled' order by departureTime desc limit 1")
    void updateDriverStatus(String status, Long id);

    @Select("select * from carpool_group where status = 'Scheduled' and departureTime between #{now} and #{tenMinutesLater} order by departureTime desc limit 1")
    CarpoolGroup findAvailableCarpoolGroup(LocalDateTime now, LocalDateTime tenMinutesLater);

    @Select("select * from user u inner join driver d on u.idUser = d.idDriver where idUser = #{id}")
    Driver getDriverAccountById(Long id);

    @Select("select * from carpool_group where idDriver = #{id} order by departureTime desc limit 10")
    List<CarpoolGroup> getDriverRecordById(Long id);

    @Select("select * from vehicle v inner join has_vehicle hv on v.idVehicle = hv.idVehicle where idDriver = #{id}")
    List<Vehicle> getVehiclesInfoByDriverId(Long id);

    @Select("select name from user u inner join carpool_request r on u.idUser = r.idCarpooler where r.idCarpoolGroup = #{id}")
    List<String> getPassengerNamesByCarpoolGroupId(Long id);

    @Select("select idCarpoolGroup from carpool_group where idDriver = #{id} and departureTime = #{time}")
    Long getGroupIdByDriverId(Long id, Date time);

    @Select("select u.name, r.originLatitude, r.originLongitude, r.destinationLatitude, r.destinationLongitude, r.originName, r.destinationName from carpool_request r inner join user u on r.idCarpooler = u.idUser where idCarpoolGroup = #{carpoolGroupId}")
    ArrayList<DriverRecordDetailDTO> getPassengerDetailsByCarpoolGroupId(Long carpoolGroupId);

    @Update("update carpool_group set status = 'Cancelled' where idDriver = #{id} and departureTime = #{time}")
    void cancelRide(Long id, Date time);
}
