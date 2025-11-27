package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    int insert(User record);
    User selectByPrimaryKey(Long id);
    int updateByPrimaryKeySelective(User record);
    int deleteByPrimaryKey(Long id);
    List<User> selectAll();
}
