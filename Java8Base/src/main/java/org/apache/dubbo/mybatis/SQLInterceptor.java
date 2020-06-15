package org.apache.dubbo.mybatis;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @Title: SQLInterceptor
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/211:17
 */
@Intercepts({
         @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@SuppressWarnings({"unchecked", "rawtypes"})
public class SQLInterceptor implements Interceptor {

     @Override
     public Object intercept(Invocation invocation) throws Throwable {

         Object proceed = invocation.proceed();

         try {
             final Object[] args = invocation.getArgs();
             // 获取xml中的一个select/update/insert/delete节点，是一条SQL语句
             MappedStatement ms = (MappedStatement) args[0];
             Object parameter = null;
             // 获取参数，if语句成立，表示sql语句有参数，参数格式是map形式
             if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
             }

             // 获取到节点的id,即sql语句的id
             String sqlId = ms.getId();
             // BoundSql就是封装myBatis最终产生的sql类
             BoundSql boundSql = ms.getBoundSql(parameter);
             // 获取节点的配置
             Configuration configuration = ms.getConfiguration();

             ms.getSqlSource();

             // 获取到最终的sql语句
             String sql = showSql(configuration, boundSql);

             List<Long> ids = new ArrayList<>();

             // 获取执行完之后的参数
             if(parameter instanceof Map){
                 Map params = (Map)parameter;
                 Iterator iterator = params.keySet().iterator();
                 while(iterator.hasNext()){
                     Object next = iterator.next();
                     Object o = params.get(next);
                     if(o instanceof emp){
                         Long id = ((emp) o).getId();
                         ids.add(id);
                         System.out.println("id="+id);
                     }else if(o instanceof List){
                         List o1 = (List) o;
                         for(Object obj : o1) {
                             if (obj instanceof emp) {
                                 Long id = ((emp) obj).getId();
                                 ids.add(id);
                                 System.out.println("id=" + id);
                             }
                         }
                     }
                     break;
                 }
             }

//             String ret_sql = convertInsertSQL(sql, "`id`", ids);

             String ret_sql = editInsertSQL(sql, "id", ids);

             MyThreadLocal.put(sqlId, ret_sql);

         } catch (Exception e) {
            e.printStackTrace();
         }

         return proceed;
     }

    // 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
    private static String getParameterValue(Object obj) {
         String value = null;
         if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
         } else if (obj instanceof Date) {
             DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
             value = "'" + formatter.format(new Date()) + "'";
         } else {
             if (obj != null) {
                 value = obj.toString();
             } else {
                value = "";
             }
         }
         return value;
    }

     // 进行？的替换
     public static String showSql(Configuration configuration, BoundSql boundSql) {

         // 获取参数
         Object parameterObject = boundSql.getParameterObject();
         List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
         // sql语句中多个空格都用一个空格代替
         String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
         if (CollectionUtils.isNotEmpty(parameterMappings) && parameterObject != null) {
             // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
             TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
             // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
             if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {

                 System.out.println(Matcher.quoteReplacement(getParameterValue(parameterObject)));

                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
             } else {
                 // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                 MetaObject metaObject = configuration.newMetaObject(parameterObject);
                 for (ParameterMapping parameterMapping : parameterMappings) {
                     String propertyName = parameterMapping.getProperty();
                     if (metaObject.hasGetter(propertyName)) {

                         Object obj = metaObject.getValue(propertyName);

                         System.out.println(propertyName+"="+Matcher.quoteReplacement(getParameterValue(obj)));

                         sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                     } else if (boundSql.hasAdditionalParameter(propertyName)) {
                         // 该分支是动态sql
                         Object obj = boundSql.getAdditionalParameter(propertyName);

                         System.out.println(propertyName+"="+Matcher.quoteReplacement(getParameterValue(obj)));

                         sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                     } else {
                         // 打印出缺失，提醒该参数缺失并防止错位
                         sql = sql.replaceFirst("\\?", "缺失");
                     }
                 }
             }
         }
         return sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private String editInsertSQL(String sql, String colname, List<Long> colvalues){

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement statement = parser.parseStatement();
        MySqlInsertStatement insert = (MySqlInsertStatement)statement;

        SQLNCharExpr colName = new SQLNCharExpr();
        colName.setText(colname);
        insert.addColumn(colName);

        List<SQLInsertStatement.ValuesClause> valuesList = insert.getValuesList();
        for (int i=0; i<valuesList.size(); i++) {
            SQLInsertStatement.ValuesClause valuesClause = valuesList.get(i);
            Long aLong = colvalues.get(i);
            SQLNumberExpr sqlNumberExpr = new SQLNumberExpr();
            sqlNumberExpr.setNumber(aLong);
            valuesClause.addValue(sqlNumberExpr);
        }

        StringBuffer stringBuilder = new StringBuffer(200);
        insert.output(stringBuilder);
        String s = stringBuilder.toString().replaceAll("\n", "").replaceAll("\t", "");

        return s;
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 修改SQL
      * @Date 16:05 2020/6/4
      * @Param [sql]
      * @return java.lang.String
      **/
    private String convertInsertSQL(String sql, String colname, List<Long> colvalues){

        try{
            MySqlStatementParser parser = new MySqlStatementParser(sql);
            SQLStatement statement = parser.parseStatement();
            MySqlInsertStatement insert = (MySqlInsertStatement)statement;
            String tableName = insert.getTableName().getSimpleName().replaceAll("`", "");

            List<SQLExpr> columns = insert.getColumns();
            if(columns == null || columns.size() <= 0)
                return sql;

            if(insert.getQuery() != null)   // insert into tab select
                return sql;

            StringBuilder sb = new StringBuilder(200)   // 指定初始容量可以提高性能
                    .append("INSERT INTO ")
                    .append(tableName).append("(");

            for(int i = 0; i < columns.size(); i++) {
                if(i < columns.size() - 1)
                    sb.append(columns.get(i).toString()).append(", ");
                else
                    sb.append(columns.get(i).toString());
            }

            // 加上主键的列名
            sb.append(", ").append(colname).append(")");

            sb.append(" VALUES");
            List<SQLInsertStatement.ValuesClause> vcl = insert.getValuesList();
            if(vcl != null && vcl.size() > 1){   // 批量insert
                for(int j=0; j<vcl.size(); j++){
                    if(j != vcl.size() - 1)
                        appendValues(vcl.get(j).getValues(), sb, colvalues.get(j).toString()).append(", ");
                    else
                        appendValues(vcl.get(j).getValues(), sb, colvalues.get(j).toString());
                }
            }else{  // 非批量 insert
                List<SQLExpr> valuse = insert.getValues().getValues();
                appendValues(valuse, sb, colvalues.get(0).toString());
            }

            List<SQLExpr> dku = insert.getDuplicateKeyUpdate();
            if(dku != null && dku.size() > 0){
                sb.append(" ON DUPLICATE KEY UPDATE ");
                for(int i=0; i<dku.size(); i++){
                    SQLExpr exp = dku.get(i);
                    if(exp != null){
                        if(i < dku.size() - 1)
                            sb.append(exp.toString()).append(",");
                        else
                            sb.append(exp.toString());
                    }
                }
            }

            return sb.toString();
        }catch(Exception e){
            return sql;
        }
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 拼接sql的value部分
      * @Date 16:39 2020/6/4
      * @Param [sqlExprs, colvalues]
      * @return void
      **/
    private StringBuilder appendValues(List<SQLExpr> sqlExprs, StringBuilder sb, String colvalue){

        sb.append("(");

        sqlExprs.forEach(O->{
            sb.append(O).append(", ");
        });

        sb.append(colvalue).append(")");

        return sb;
    }

}
