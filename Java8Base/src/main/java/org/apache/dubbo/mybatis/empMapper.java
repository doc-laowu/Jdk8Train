package org.apache.dubbo.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Title: empMapper
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/5/2813:39
 */
public interface empMapper {

    void inertEmp(@Param("emp") emp emp);

    int updateEmp(emp emp);

    int updateEmpByid(@Param("emp") emp emp, @Param("empid") Long empid);

    emp getEmpById(emp emp);

    int insertBatch(@Param("emps") List<emp> emps);
}
