package com.test.mapper;

import com.test.entity.TestCase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TestCaseMapper {

    @Insert("INSERT INTO test_case(title, module, priority, precondition, steps, expected_result, status, creator, create_time, update_time) " +
            "VALUES(#{title}, #{module}, #{priority}, #{precondition}, #{steps}, #{expectedResult}, #{status}, #{creator}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TestCase testCase);

    @Update("UPDATE test_case SET title=#{title}, module=#{module}, priority=#{priority}, precondition=#{precondition}, " +
            "steps=#{steps}, expected_result=#{expectedResult}, status=#{status}, creator=#{creator}, update_time=#{updateTime} WHERE id=#{id}")
    int updateById(TestCase testCase);

    @Delete("DELETE FROM test_case WHERE id=#{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM test_case WHERE id=#{id}")
    TestCase selectById(Long id);

    @Select("SELECT * FROM test_case ORDER BY id DESC")
    List<TestCase> selectList();

    @Select("SELECT * FROM test_case WHERE title LIKE CONCAT('%', #{keyword}, '%') OR module LIKE CONCAT('%', #{keyword}, '%') ORDER BY id DESC")
    List<TestCase> search(String keyword);

    @Delete("DELETE FROM test_case")
    int deleteAll();
}
