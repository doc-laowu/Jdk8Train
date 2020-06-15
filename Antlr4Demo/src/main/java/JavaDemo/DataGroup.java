package JavaDemo;

import JavaDemo.Data.DataLexer;
import JavaDemo.Data.DataParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Title: DataGroup
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/1/1715:27
 */
public class DataGroup {

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
        DataLexer lexer = new DataLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DataParser parser = new DataParser(tokens); // 传递序列号作为参数
        DataParser.FileContext file = parser.file();// 开始语法分析
    }

}
