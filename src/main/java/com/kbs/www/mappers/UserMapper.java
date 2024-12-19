package com.kbs.www.mappers;

import com.kbs.www.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    UserEntity[] selectAllUser();

    UserEntity selectUserByEmail(@Param("userEmail") String userEmail);

    int updateWarning(UserEntity user);

    int selectUserCount();

    UserEntity[] selectUserPage(@Param(value = "limitCount") int limitCount,
                                @Param(value = "offsetCount") int offsetCount);

    int selectUserCountBySearch(@Param(value = "filter") String filter,
                                @Param(value = "keyword") String keyword);

    UserEntity[] selectUserBySearch(@Param(value = "filter") String filter,
                                    @Param(value = "keyword") String keyword,
                                    @Param(value = "limitCount") int limitCount,
                                    @Param(value = "offsetCount") int offsetCount);
}
