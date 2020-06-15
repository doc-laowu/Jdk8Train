package org.apache.dubbo.SQLAnalysis;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Title: SqlParser2Dsl
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/419:22
 */
public class SqlParser2Dsl {

    private final static String dbType = JdbcConstants.MYSQL;

    private SearchSourceBuilder builder;

    public SqlParser2Dsl(SearchSourceBuilder builder) {
        this.builder = builder;
    }

    /**
     * 将SQL解析为ES查询
     */
    public SearchSourceBuilder parse(String sql) throws Exception {
        if (Objects.isNull(sql)) {
            throw new IllegalArgumentException("输入语句不得为空");
        }
        sql = sql.trim().toLowerCase();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
        if (Objects.isNull(stmtList) || stmtList.size() != 1) {
            throw new IllegalArgumentException("必须输入一句查询语句");
        }
        // 使用Parser解析生成AST
        SQLStatement stmt = stmtList.get(0);
        if (!(stmt instanceof SQLSelectStatement)) {
            throw new IllegalArgumentException("输入语句须为Select语句");
        }
        SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) stmt;
        SQLSelectQuery sqlSelectQuery = sqlSelectStatement.getSelect().getQuery();
        SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;

        SQLExpr whereExpr = sqlSelectQueryBlock.getWhere();

        // 生成ES查询条件
        BoolQueryBuilder bridge = QueryBuilders.boolQuery();
        bridge.must();

        QueryBuilder whereBuilder = whereHelper(whereExpr); // 处理where
        bridge.must(whereBuilder);
        SQLOrderBy orderByExpr = sqlSelectQueryBlock.getOrderBy(); // 处理order by
        if (Objects.nonNull(orderByExpr)) {
            orderByHelper(orderByExpr, bridge);
        }
        builder.query(bridge);
        return builder;
    }

    /**
     * 处理所有order by字段
     *
     * @param orderByExpr
     */
    private void orderByHelper(SQLOrderBy orderByExpr, BoolQueryBuilder bridge) {
        List<SQLSelectOrderByItem> orderByList = orderByExpr.getItems(); // 待排序的列
        for (SQLSelectOrderByItem sqlSelectOrderByItem : orderByList) {
            if (sqlSelectOrderByItem.getType() == null) {
                sqlSelectOrderByItem.setType(SQLOrderingSpecification.ASC); // 默认升序
            }
            String orderByColumn = sqlSelectOrderByItem.getExpr().toString();
            builder.sort(orderByColumn,
                    sqlSelectOrderByItem.getType().equals(SQLOrderingSpecification.ASC) ? SortOrder.ASC
                            : SortOrder.DESC);
        }
    }

    /**
     * 递归遍历“where”子树
     *
     * @return
     */
    private QueryBuilder whereHelper(SQLExpr expr) throws Exception {
        if (Objects.isNull(expr)) {
            throw new NullPointerException("节点不能为空!");
        }
        BoolQueryBuilder bridge = QueryBuilders.boolQuery();
        if (expr instanceof SQLBinaryOpExpr) { // 二元运算
            SQLBinaryOperator operator = ((SQLBinaryOpExpr) expr).getOperator(); // 获取运算符
            if (operator.isLogical()) { // and,or,xor
                return handleLogicalExpr(expr);
            } else if (operator.isRelational()) { // 具体的运算,位于叶子节点
                return handleRelationalExpr(expr);
            }
        } else if (expr instanceof SQLBetweenExpr) { // between运算
            SQLBetweenExpr between = ((SQLBetweenExpr) expr);
            boolean isNotBetween = between.isNot(); // between or not between ?
            String testExpr = between.testExpr.toString();
            String fromStr = formatSQLValue(between.beginExpr.toString());
            String toStr = formatSQLValue(between.endExpr.toString());
            if (isNotBetween) {
                bridge.must(QueryBuilders.rangeQuery(testExpr).lt(fromStr).gt(toStr));
            } else {
                bridge.must(QueryBuilders.rangeQuery(testExpr).gte(fromStr).lte(toStr));
            }
            return bridge;
        } else if (expr instanceof SQLInListExpr) { // SQL的 in语句，ES中对应的是terms
            SQLInListExpr siExpr = (SQLInListExpr) expr;
            boolean isNotIn = siExpr.isNot(); // in or not in?
            String leftSide = siExpr.getExpr().toString();
            List<SQLExpr> inSQLList = siExpr.getTargetList();
            List<String> inList = new ArrayList<>();
            for (SQLExpr in : inSQLList) {
                String str = formatSQLValue(in.toString());
                inList.add(str);
            }
            if (isNotIn) {
                bridge.mustNot(QueryBuilders.termsQuery(leftSide, inList));
            } else {
                bridge.must(QueryBuilders.termsQuery(leftSide, inList));
            }
            return bridge;
        }
        return bridge;
    }

    /**
     * 逻辑运算符，目前支持and,or
     *
     * @return
     * @throws Exception
     */
    private QueryBuilder handleLogicalExpr(SQLExpr expr) throws Exception {
        BoolQueryBuilder bridge = QueryBuilders.boolQuery();
        SQLBinaryOperator operator = ((SQLBinaryOpExpr) expr).getOperator(); // 获取运算符
        SQLExpr leftExpr = ((SQLBinaryOpExpr) expr).getLeft();
        SQLExpr rightExpr = ((SQLBinaryOpExpr) expr).getRight();

        // 分别递归左右子树，再根据逻辑运算符将结果归并
        QueryBuilder leftBridge = whereHelper(leftExpr);
        QueryBuilder rightBridge = whereHelper(rightExpr);
        if (operator.equals(SQLBinaryOperator.BooleanAnd)) {
            bridge.must(leftBridge).must(rightBridge);
        } else if (operator.equals(SQLBinaryOperator.BooleanOr)) {
            bridge.should(leftBridge).should(rightBridge);
        }
        return bridge;
    }

    /**
     * 大于小于等于正则
     *
     * @param expr
     * @return
     */
    private QueryBuilder handleRelationalExpr(SQLExpr expr) {
        SQLExpr leftExpr = ((SQLBinaryOpExpr) expr).getLeft();
        if (Objects.isNull(leftExpr)) {
            throw new NullPointerException("表达式左侧不得为空");
        }
        String leftExprStr = leftExpr.toString();
        String rightExprStr = formatSQLValue(((SQLBinaryOpExpr) expr).getRight().toString()); // TODO:表达式右侧可以后续支持方法调用
        SQLBinaryOperator operator = ((SQLBinaryOpExpr) expr).getOperator(); // 获取运算符
        QueryBuilder queryBuilder;
        switch (operator) {
            case GreaterThanOrEqual:
                queryBuilder = QueryBuilders.rangeQuery(leftExprStr).gte(rightExprStr);
                break;
            case LessThanOrEqual:
                queryBuilder = QueryBuilders.rangeQuery(leftExprStr).lte(rightExprStr);
                break;
            case Equality:
                queryBuilder = QueryBuilders.boolQuery();
                TermQueryBuilder eqCond = QueryBuilders.termQuery(leftExprStr, rightExprStr);
                ((BoolQueryBuilder) queryBuilder).must(eqCond);
                break;
            case GreaterThan:
                queryBuilder = QueryBuilders.rangeQuery(leftExprStr).gt(rightExprStr);
                break;
            case LessThan:
                queryBuilder = QueryBuilders.rangeQuery(leftExprStr).lt(rightExprStr);
                break;
            case NotEqual:
                queryBuilder = QueryBuilders.boolQuery();
                TermQueryBuilder notEqCond = QueryBuilders.termQuery(leftExprStr, rightExprStr);
                ((BoolQueryBuilder) queryBuilder).mustNot(notEqCond);
                break;
            case RegExp: // 对应到ES中的正则查询
                queryBuilder = QueryBuilders.boolQuery();
                RegexpQueryBuilder regCond = QueryBuilders.regexpQuery(leftExprStr, rightExprStr);
                ((BoolQueryBuilder) queryBuilder).mustNot(regCond);
                break;
            case NotRegExp:
                queryBuilder = QueryBuilders.boolQuery();
                RegexpQueryBuilder notRegCond = QueryBuilders.regexpQuery(leftExprStr, rightExprStr);
                ((BoolQueryBuilder) queryBuilder).mustNot(notRegCond);
                break;
            case Like:
                queryBuilder = QueryBuilders.boolQuery();
                MatchPhraseQueryBuilder likeCond = QueryBuilders.matchPhraseQuery(leftExprStr,
                        rightExprStr.replace("%", ""));
                ((BoolQueryBuilder) queryBuilder).must(likeCond);
                break;
            case NotLike:
                queryBuilder = QueryBuilders.boolQuery();
                MatchPhraseQueryBuilder notLikeCond = QueryBuilders.matchPhraseQuery(leftExprStr,
                        rightExprStr.replace("%", ""));
                ((BoolQueryBuilder) queryBuilder).mustNot(notLikeCond);
                break;
            default:
                throw new IllegalArgumentException("暂不支持该运算符!" + operator.toString());
        }
        return queryBuilder;
    }


    private String formatSQLValue(String value){

        return value;
    }

    public static void main(String[] args) {

        String sql = "select time, date from test where time >= 1 and time <= 10";
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        try {
            searchSourceBuilder = new SqlParser2Dsl(searchSourceBuilder).parse(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(searchSourceBuilder);

    }

}
