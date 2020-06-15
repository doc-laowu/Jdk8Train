package org.apache.dubbo.SQLAnalysis;

import org.datayoo.moql.MoqlException;
import org.datayoo.moql.sql.SqlDialectType;
import org.datayoo.moql.translator.MoqlTranslator;
import org.junit.jupiter.api.Test;

/**
 * @Title: SQL2Dsl
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/417:36
 */
public class SQL2Dsl {

    public static void main(String[] args) {

        String sql = "select ip.src, max(ip.sport), min(ip.sport) from ip3 ip group by ip.src order by ip.src desc limit 10 ";

        try {

            //将sql语句串转换为ELASTICSEARCH方言的语法串
            long start = System.currentTimeMillis();
            String es = MoqlTranslator.translateMoql2Dialect(sql, SqlDialectType.ELASTICSEARCH);
            es = es.trim();
            long end = System.currentTimeMillis();

            System.out.println(end-start);

            //打印输出转换后的语法串

            System.out.println(es);

        } catch (MoqlException e) {

            e.printStackTrace();

        }

    }

    @Test
    void test(){



    }

}
