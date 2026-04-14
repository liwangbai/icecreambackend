MyBatis XML映射文件目录

此目录用于存放MyBatis的XML映射文件。

命名规范：
- 每个Mapper接口对应一个XML文件
- XML文件名与Mapper接口名相同，如：UserMapper.java -> UserMapper.xml
- 建议按模块组织子目录，如：mapper/user/UserMapper.xml

文件结构示例：
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.icecream.backend.mapper.UserMapper">
    <!-- SQL映射定义 -->
</mapper>

配置说明：
- 在application.yml中配置了：mybatis.mapper-locations=classpath:mapper/**/*.xml
- 此配置会自动扫描此目录下的所有XML文件