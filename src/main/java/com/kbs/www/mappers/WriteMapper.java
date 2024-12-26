package com.kbs.www.mappers;

import com.kbs.www.entities.FaveInfoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WriteMapper {

    int insertAdminWrite(FaveInfoEntity adminPage);
}
