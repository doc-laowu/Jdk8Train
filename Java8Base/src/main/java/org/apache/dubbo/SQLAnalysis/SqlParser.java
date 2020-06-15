package org.apache.dubbo.SQLAnalysis;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;

import java.util.List;
import java.util.Map;

/**
 * @Title: SqlParser
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/311:31
 */
public class SqlParser {
    private static List<SQLStatement> getSQLStatementList(String sql) {
        String dbType = JdbcConstants.MYSQL;
        String result = SQLUtils.format(sql, JdbcConstants.MYSQL);
        return SQLUtils.parseStatements(result, dbType);
    }

    public static void main(String[] args) {
        String updateSql = "UPDATE emp SET firstname = 'laowu' WHERE firstname IN ('lebron', 'marvin')";

        String insertSql = "insert into table_test_2 (farendma, hesuandm, hesuanmc, weihguiy, weihjigo, weihriqi, shijchuo) values " +
                "('99996','HS205301','代码1','S####','101001','20140101',1414673101376), " +
                "('99996','HS205401','代码2','S####','101001','20140101',1414673101376)";

        List<SQLStatement> sqlStatementList = getSQLStatementList(insertSql);
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();

        for(SQLStatement stmt : sqlStatementList){

            stmt.accept(visitor);

            // 获取表名
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            for(Map.Entry<TableStat.Name, TableStat> tableStatEntry: tables.entrySet()){

                String tablename = tableStatEntry.getKey().getName();
                String operation = tableStatEntry.getValue().toString();

                System.out.println("tablename:"+tablename);
                System.out.println("operation:"+operation);

                if("Update".equals(operation)){

                    MySqlUpdateStatement updateStatement = (MySqlUpdateStatement)stmt;

                    //获取列的名称
                    List<SQLUpdateSetItem> items = updateStatement.getItems();
                    items.forEach(O->{
                        System.out.println(O.getColumn()+"="+O.getValue());
                    });

                    // 获取条件
                    SQLExpr whereExpr = updateStatement.getWhere();
                    if(whereExpr instanceof SQLInListExpr){
                        // SQLInListExpr 指 run_id in ('1', '2') 这一情况
                        SQLInListExpr inListExpr = (SQLInListExpr)whereExpr;
                        List<SQLExpr> valueExprs = inListExpr.getTargetList();

                        valueExprs.forEach(O->{

                            System.out.println(O.toString()+"\t");

                        });

                    }else if(whereExpr instanceof SQLBetweenExpr) {

                        SQLBetweenExpr betweenExpr = (SQLBetweenExpr) whereExpr;

                        Map<String, Object> attributes2 = betweenExpr.getAttributes();
                        attributes2.forEach((k, v)->{
                            System.out.println(k+":"+v);
                        });

                        Map<String, Object> attributes = betweenExpr.getBeginExpr().getAttributes();
                        attributes.forEach((k, v)->{
                            System.out.println(k+":"+v);
                        });

                        Map<String, Object> attributes1 = betweenExpr.getBeginExpr().getAttributes();
                        attributes1.forEach((k, v)->{
                            System.out.println(k+":"+v);
                        });
                    }else if(whereExpr instanceof SQLBinaryOpExpr){
                        // SQLBinaryOpExpr 指 run_id = '1' 这一情况
                        SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) whereExpr;
                        SQLExpr left = binaryOpExpr.getLeft();
                        System.out.println(binaryOpExpr.getLeft() + " " + binaryOpExpr.getOperator().name + " " + binaryOpExpr.getRight());
                    }

                }else if("Insert".equals(operation)){

                    MySqlInsertStatement insertStatement = (MySqlInsertStatement)stmt;
                    //获取列的名称
                    List<SQLExpr> columnExprs = insertStatement.getColumns();
                    System.out.println("列的名称为：");
                    for(SQLExpr expr : columnExprs){
                        System.out.print(expr + "\t");
                    }
                    System.out.println();

                    //获取插入的值
                    List<SQLInsertStatement.ValuesClause> valuesClauseList = insertStatement.getValuesList();
                    System.out.println("值分别是：");
                    for(SQLInsertStatement.ValuesClause valuesClause : valuesClauseList){
                        List<SQLExpr> valueExprList = valuesClause.getValues();
                        for(SQLExpr expr : valueExprList){

                            System.out.print(expr + " ");
                        }
                        System.out.println();
                    }
                }
            }
        }
    }


}