package JavaDemo;


import JavaDemo.Rows.RowsLexer;
import JavaDemo.Rows.RowsParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Title: Col
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/1/1714:47
 */
public class Col {

    public static void main(String[] args) throws IOException {

        String inputFile = null;

        if(args.length > 0){
            inputFile = args[1];
        }

        InputStream is = System.in;
        if(inputFile != null){
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        RowsLexer lexer = new RowsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        int col = Integer.parseInt(args[0]);
        RowsParser parser = new RowsParser(tokens, col); // 传递序列号作为参数
        parser.setBuildParseTree(false);
        parser.file(); // 开始语法分析
    }

}
