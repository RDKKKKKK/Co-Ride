package com.coride.mapper;

import com.coride.dto.AccountManagementDTO;
import com.coride.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    void insert(User user);

    @Select("select emailSuffix from organization where idOrganization = #{idOrganization}")
    String getSuffix(User user);

    @Select("select * from user inner join carpooler on idUser = idCarpooler where accountNo = #{accountNo}")
    User getCarpoolerByAccountNo(String accountNo);

    @Select("select * from user inner join driver d on user.idUser = d.idDriver where accountNo = #{accountNo}")
    User getDriverByAccountNo(String accountNo);


    @Select("select u.name, o.organizationName, u.accountNo from user u inner join organization o on u.idOrganization = o.idOrganization where idUser = #{id}")
    AccountManagementDTO getAccount(Long id);

    @Update("update user set name = #{name}, password = #{password} where idUser = #{id}")
    void updateAccount(Long id, AccountManagementDTO accountManagementDTO);
}
