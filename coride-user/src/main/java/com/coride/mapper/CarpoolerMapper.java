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

    @Select("select * from carpooler c inner join user u on c.idCarpooler = u.idUser where idUser = #{id}")
    Carpooler getCarpoolerAccountById(Long id);

}
