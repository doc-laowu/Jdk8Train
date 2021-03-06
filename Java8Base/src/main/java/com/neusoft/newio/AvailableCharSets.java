package com.neusoft.newio;



import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;

/**
 * @Title: AvailableCharSets
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/4/2314:41
 */
public class AvailableCharSets {

    public static void main(String[] args) {

        SortedMap<String, Charset> charSets = Charset.availableCharsets();

        Iterator<String> it = charSets.keySet().iterator();

        while(it.hasNext()){

            String csName = it.next();

            System.out.println(csName);

            Iterator aliases = charSets.get(csName).aliases().iterator();

            if(aliases.hasNext()){
                System.out.println(aliases.next());

                if(aliases.hasNext())
                    System.out.print(", ");
            }
            System.out.println();

        }

    }

}
