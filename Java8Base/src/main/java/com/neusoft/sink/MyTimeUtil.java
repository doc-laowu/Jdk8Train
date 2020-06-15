package com.neusoft.sink;

import org.apache.commons.lang.time.FastDateFormat;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Title: MyTimeUtil
 * @ProjectName Real_Dws_Data_Statistics
 * @Description: 用户自定义事件工具
 * @Author gaosen
 * @Date 2019/3/2512:24
 */
public class MyTimeUtil implements Serializable {

    private static final FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    private static final FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:00");

    /**
     * TODO 格式化时间字符串
     * @param strTime
     * @return
     * @throws ParseException
     */
    public static String formatTime(String strTime) throws ParseException {

        String timeRet = fdf.format(fdf.parseObject(strTime));
        return timeRet;
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 格式化时间字符串
      * @Date 11:00 2019/3/26
      * @Param [timestamp]
      * @return java.lang.String
      **/
    public static Timestamp formatTime2TimeStamp(Long timestamp) throws Exception {

        try {

            Timestamp ts = new Timestamp(timestamp);
            return ts;
        }catch (Exception e){
            throw new Exception("transfer the unix timestamp to the sql timestamp error!");
        }
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 将字符串的时间转为时间戳
      * @Date 10:37 2019/3/27
      * @Param [timestamp]
      * @return java.sql.Timestamp
      **/
    public static Timestamp formatTime2TimeStamp(String timestamp) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatTime2TimeStamp(sdf.parse(timestamp).getTime());
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String Time1Min() {

        return fdf2.format(new Date());
    }

    public static Long CurrentTimeLong() {

        return new Date().getTime();
    }

    public static String CurrentTimeStr() {

        return fdf.format(new Date());
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 将时间字符串转化为毫秒的格式
      * @Date 11:04 2019/4/9
      * @Param [strTime]
      * @return java.lang.Long
      **/
    public static Long formatTime2Mills(String strTime) throws ParseException {

        return ((Date)fdf.parseObject(strTime)).getTime();
    }

}
