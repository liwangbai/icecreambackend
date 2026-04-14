package com.icecream.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 示例Mapper接口
 * 展示MyBatis Mapper接口的基本结构和用法
 *
 * 注意：
 * 1. 使用@Mapper注解标记为MyBatis Mapper
 * 2. 方法名与XML映射文件中的SQL ID对应
 * 3. 使用@Param注解为参数命名，在XML中通过#{参数名}引用
 */
@Mapper
public interface ExampleMapper {

    /**
     * 根据ID查询示例数据
     * @param id 数据ID
     * @return 数据条数
     */
    int countById(@Param("id") Long id);

    /**
     * 插入示例数据
     * @param name 名称
     * @param value 值
     * @return 影响的行数
     */
    int insertExample(@Param("name") String name, @Param("value") String value);

    /**
     * 更新示例数据
     * @param id 数据ID
     * @param value 新值
     * @return 影响的行数
     */
    int updateValueById(@Param("id") Long id, @Param("value") String value);

    /**
     * 删除示例数据
     * @param id 数据ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
}