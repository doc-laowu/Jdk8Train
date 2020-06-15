package ExprDemo;

import ExprDemo.Expr.ExprLexer;
import ExprDemo.Expr.ExprParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Title: ExprJoyRide
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/1/1515:45
 */
public class ExprJoyRide {

    public static void main(String[] args) throws IOException {
        String inputFile = null;

        if(args.length > 0){
            inputFile = args[0];
        }

        InputStream is = System.in;
        if(inputFile != null){
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        ExprLexer lexer = new ExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);
        ExprParser.ProgContext tree = parser.prog();  // 开始语法分析

        EvalVisitor eval = new EvalVisitor();
        eval.visit(tree);

    }

}
