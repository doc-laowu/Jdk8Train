package org.apache.dubbo.SQLAnalysis;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import org.mapdb.Fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: MyVisitor
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/319:12
 */
public class MyVisitor extends MySqlOutputVisitor {

    private String tablename;
    private List<Fun.Tuple3<String, String, String>> whereMapList = new ArrayList<>();
    private List<Map<String, Object>> columnMapList = new ArrayList<>();

    public MyVisitor(Appendable appender) {
        super(appender);
    }

    /*
      * @Author: yisheng.wu
      * @Description TODO 遍历insert
      * @Date 20:02 2020/6/3
      * @Param [x]
      * @return boolean
      **/
    @Override
    public boolean visit(MySqlInsertStatement x) {
        return super.visit(x);
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 获取表名
      * @Date 20:18 2020/6/3
      * @Param [x]
      * @return boolean
      **/
    @Override
    public boolean visit(SQLExprTableSource x) {

        tablename = x.getName().getSimpleName();
        return super.visit(x);
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 遍历update
      * @Date 20:02 2020/6/3
      * @Param [x]
      * @return boolean
      **/
    @Override
    public boolean visit(MySqlUpdateStatement x) {

        Map<String, Object> colMap = new HashMap<>();

        // 获取列名称
        List<SQLUpdateSetItem> items = x.getItems();
        for (SQLUpdateSetItem item : items) {
            colMap.put(item.getColumn().toString(), item.getValue());
        }

        columnMapList.add(colMap);

        return super.visit(x);
    }

    @Override
    public boolean visit(SQLBinaryOpExpr expr) {

        if (isIdentifier(expr.getLeft()) && isValue(expr.getRight())) {

            String name = expr.getOperator().getName();
            String left = expr.getLeft().toString();
            String right = expr.getRight().toString();

            Fun.Tuple3<String, String, String> pair = new Fun.Tuple3<>(left, name, right);
            whereMapList.add(pair);

        }else if(isBetween(expr.getLeft()) && isBetween(expr.getRight())){



        }

        return super.visit(expr);
    }

    @Override
    public boolean visit(SQLInListExpr expr) {
        if (isIdentifier(expr.getExpr())) {

            List<SQLExpr> targetList = expr.getTargetList();

            targetList.forEach(O->{
            });
        }
        return super.visit(expr);
    }

    @Override
    public boolean visit(SQLBetweenExpr x) {
        if(isIdentifier(x.getBeginExpr()) && isIdentifier(x.getEndExpr())){

        }

        return super.visit(x);
    }

    private boolean isIdentifier(SQLExpr expr) {
        return expr.getClass() == SQLIdentifierExpr.class;
    }

    private boolean isBetween(SQLExpr expr){
        return expr.getClass() == SQLBetweenExpr.class;
    }

    private boolean isValue(SQLExpr expr) {
        Class<?> clazz = expr.getClass();
        return clazz == SQLVariantRefExpr.class
                || clazz == SQLIntegerExpr.class
                || clazz == SQLNumberExpr.class
                || clazz == SQLCharExpr.class
                || clazz == SQLBooleanExpr.class;
    }

}
