package org.apache.dubbo.SQLAnalysis;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @Title: UpdateSQLParser
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/420:01
 */
public class UpdateSQLParser {

    // 存放参数判断consumer_uid和visitor_id
    private HashMap<String, String> params = new HashMap<>();

    /**
     * 将SQL解析为ES查询
     */
    public void parse(String sql) throws Exception {
        if (Objects.isNull(sql)) {
            throw new IllegalArgumentException("输入语句不得为空");
        }

        long start = System.currentTimeMillis();
        sql = sql.trim().toLowerCase();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        long end = System.currentTimeMillis();
        System.out.println("解析SQL:"+(end-start));
        if (Objects.isNull(stmtList) || stmtList.size() != 1) {
            throw new IllegalArgumentException("必须输入一句SQL更新或插入语句");
        }

        // 使用Parser解析生成AST
        stmtList.forEach(stmt->{

            if (!(stmt instanceof SQLInsertStatement || stmt instanceof SQLUpdateStatement)) {
                throw new IllegalArgumentException("输入语句须为Insert或Update语句");
            }

            // 解析插入语句
            if(stmt instanceof SQLInsertStatement){

                SQLInsertStatement insertStmt = (SQLInsertStatement)stmt;
                SQLName tableName = insertStmt.getTableName();

                // 获取列名
                List<SQLExpr> columns = insertStmt.getColumns();

                // 获取插入的值
                List<SQLInsertStatement.ValuesClause> valuesList = insertStmt.getValuesList();

                try {
                    Bulk bulk = assembleIndex(tableName.toString(), columns, valuesList);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            // 解析更新语句
            else if(stmt instanceof SQLUpdateStatement){

                SQLUpdateStatement updateStmt = (SQLUpdateStatement)stmt;
                //获取列的名称
                List<SQLUpdateSetItem> items = updateStmt.getItems();

                // 获取条件
                SQLExpr whereExpr = updateStmt.getWhere();

                try {
                    String s = assembleUpdate(items, whereExpr);
                    System.out.println(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 组装update的script语句
      * @Date 20:44 2020/6/4
      * @Param [items]
      * @return org.elasticsearch.common.xcontent.XContentBuilder
      **/
    private String assembleUpdate(List<SQLUpdateSetItem> items, SQLExpr whereExpr) throws Exception {

        XContentBuilder xContentBuilder = jsonBuilder();

        StringBuilder script = new StringBuilder(200);
        JSONObject params = new JSONObject();

        String consumer_uid = null;
        String visitor_id = null;

        // 组装script字段
        for (int i=0; i<items.size(); i++){
            SQLUpdateSetItem sqlUpdateSetItem = items.get(i);
            String column = sqlUpdateSetItem.getColumn().toString();
            String value = sqlUpdateSetItem.getValue().toString();

            if("consumer_uid".equals(column)){
                if(!"0".equals(value)){
                    consumer_uid = value;
                }
            }else if("visitor_id".equals(column)){
                if(!StringUtils.isEmpty(value)){
                    visitor_id = value;
                }
            }else {
                // 拼装script
                script.append("ctx._source.").append(column).append("=").append("params.").append(column).append(";");
                params.put(column, formatSQLValue(value));
            }

        }

        // 拼装script
        if(null != consumer_uid){
            script.append("ctx._source.").append("consumer_uid").append("=").append("params.").append("consumer_uid").append(";");
            params.put("consumer_uid", formatSQLValue(consumer_uid));
        }else if(null == consumer_uid && null != visitor_id){
            script.append("ctx._source.").append("visitor_id").append("=").append("params.").append("visitor_id").append(";");
            params.put("visitor_id", formatSQLValue(visitor_id));
        }


        // 获取where的条件
        long start = System.currentTimeMillis();
        QueryBuilder queryBuilder = whereHelper(whereExpr);
        long end = System.currentTimeMillis();
        System.out.println("条件解析:"+(end-start));
        // 拼装最后的结果
        xContentBuilder.startObject()
                .startObject("script")
                .field("scource", script.toString())
                .field("params", params)
                .endObject()
                .field("query", queryBuilder)
        .endObject();

        String s = queryBuilder.toString();
        System.out.println("condition:"+s);

        return xContentBuilder.string();
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 组装好index语句
      * @Date 20:30 2020/6/4
      * @Param [tableName, columns, valuesList]
      * @return java.util.List<io.searchbox.core.Index>
      **/
    private Bulk assembleIndex(String tableName, List<SQLExpr> columns, List<SQLInsertStatement.ValuesClause> valuesList) throws IOException {

        String primaryColName = null;
        if("consumer_activity_rel".equals(tableName)){
            primaryColName = "activity_consumer_uid";
        }else if("consumer_buser_rel".equals(tableName)){
            primaryColName = "business_consumer_uid";
        }

        Bulk.Builder builder = new Bulk.Builder();

        for(int i=0; i<valuesList.size(); i++){

            List<SQLExpr> values = valuesList.get(i).getValues();
            XContentBuilder xContentBuilder = jsonBuilder().startObject();
            String _id = null;
            for(int j=0; j<values.size(); j++){
                if(primaryColName.equals(columns.get(j))){
                    _id = values.get(j).toString();
                }
                xContentBuilder.field(columns.get(j).toString(), values.get(j).toString());
            }
            String dsl = xContentBuilder.endObject().toString();
            Index index = new Index.Builder(dsl).index(tableName)
                    .type(tableName)
                    .id(_id)
                    .setHeader("routing", _id)
                    .build();

            builder.addAction(index);
        }

        String data = builder.build().getData(new Gson());
        System.out.println(data);

        return builder.build();
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 判断Where条件
      * @Date 20:32 2020/6/4
      * @Param [expr]
      * @return org.elasticsearch.index.query.QueryBuilder
      **/
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

        if("consumer_uid".equals(leftExprStr)){
            String visitor_id = params.get("visitor_id");

        }else if("visitor_id".equals(leftExprStr)){
            String consumer_uid = params.get("consumer_uid");
        }

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
            default:
                throw new IllegalArgumentException("暂不支持该运算符!" + operator.toString());
        }
        return queryBuilder;
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 对右值的操作
      * @Date 20:32 2020/6/4
      * @Param [value]
      * @return java.lang.String
      **/
    private String formatSQLValue(String value){

        String ret = value
                // 去除单引号
                .replaceAll("'", "")
                // 去除反引号
                .replaceAll("`", "");

        return ret;
    }

    public static void main(String[] args) throws Exception {

        String sql = "UPDATE emp SET firstname = 'laowu', lastname = 'james' WHERE firstname = 'lebron' and (business_consumer_uid = 123213 or position='asdasgf')";

        long start = System.currentTimeMillis();
        new UpdateSQLParser().parse(sql);
        long end = System.currentTimeMillis();
        System.out.println("total:"+(end-start));
    }

}
