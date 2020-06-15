package ExprDemo;

import ExprDemo.Expr.ExprBaseVisitor;
import ExprDemo.Expr.ExprParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: EvalVisitor
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/1/1517:32
 */
public class EvalVisitor extends ExprBaseVisitor<Integer> {

    Map<String, Integer> memory = new HashMap<String, Integer>();

    @Override
    public Integer visitPrintExpr(ExprParser.PrintExprContext ctx) {

        Integer value = visit(ctx.expr()); // 计算expr子节点的值
        System.out.println(value);  // 打印结果
        return 0; // 上面已经打印了，不返回值了
    }

    /** ID '=' expr NEWLINE **/
    @Override
    public Integer visitAssign(ExprParser.AssignContext ctx) {

        String id = ctx.ID().getText(); // id 在'='的左侧
        int value = visit(ctx.expr()); // 计算右侧的表达式的值
        memory.put(id, value); // 将这个映射关系存储在计算器的内存中
        return value;
    }

    /** '(' expr ')' **/
    @Override
    public Integer visitParens(ExprParser.ParensContext ctx) {
        return visit(ctx.expr()); // 返回子表达式的值
    }

    /** expr op=('*'|'/') expr **/
    @Override
    public Integer visitMulDiv(ExprParser.MulDivContext ctx) {

        int left = visit(ctx.expr(0)); // 计算左侧的子表达式的值
        int right = visit(ctx.expr(1)); // 计算右侧子表达式的值
        if(ctx.op.getType() == ExprParser.MUL)
            return left * right;
        return left / right;
    }

    /** expr op=('+'|'-') expr **/
    @Override
    public Integer visitAddSub(ExprParser.AddSubContext ctx) {

        int left = visit(ctx.expr(0)); // 计算左侧的子表达式的值
        int right = visit(ctx.expr(1)); // 计算右侧子表达式的值
        if(ctx.op.getType() == ExprParser.ADD)
            return left + right;
        return left - right;
    }

    /** ID **/
    @Override
    public Integer visitId(ExprParser.IdContext ctx) {

        String id = ctx.ID().getText();
        if(memory.containsKey(id)) return memory.get(id);
        return 0;
    }

    /** INT **/
    @Override
    public Integer visitInt(ExprParser.IntContext ctx) {

        return Integer.valueOf(ctx.INT().getText());
    }
}
