package org.apache.dubbo.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @Title: MyInterceptor
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/5/2814:14
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MyInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        Class<?> clazz = parameter.getClass();
        if (!clazz.getSuperclass().isInstance(Object.class)){
            Class<?> superclass = clazz.getSuperclass();
            updateFeild(superclass.getDeclaredFields(),parameter,sqlCommandType);
        }else {
            updateFeild(parameter.getClass().getDeclaredFields(),parameter,sqlCommandType);
        }

        String sql = mappedStatement.getBoundSql(parameter).getSql();

        System.out.println(sql);

        return invocation.proceed();

    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private void updateFeild(Field[] declaredFields, Object parameter, SqlCommandType sqlCommandType) throws IllegalAccessException {

        DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = fmt.format(new Date());

        for (Field field: declaredFields){

            if (SqlCommandType.INSERT.equals(sqlCommandType)){
                if (field.getName().equals("created_at")){
                    field.setAccessible(true);
                    field.set(parameter, format);
                }
            }else if (SqlCommandType.UPDATE.equals(sqlCommandType)){
                if (field.getName().equals("updated_at")){
                    field.setAccessible(true);
                    field.set(parameter, format);
                }
            }

        }

    }
}
