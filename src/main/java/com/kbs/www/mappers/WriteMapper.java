package com.kbs.www.mappers;

import com.kbs.www.entities.WriteEntity;
import com.kbs.www.entities.BoardPostsEntity;
import com.kbs.www.entities.ReportsEntity;
import com.kbs.www.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WriteMapper {

    int insertAdminWrite(WriteEntity adminPage);
}
