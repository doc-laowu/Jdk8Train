import com.alibaba.fastjson.JSON;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReadDatahubMsg {
    public static void main(String[] args) {

        method(null);

//        readMsg();
    }

    public static void method(String param) {
        switch (param) {
            // 肯定不是进入这里
            case "sth":
                System.out.println("it's sth");
                break;
            // 也不是进入这里
            case "null":
                System.out.println("it's null");
                break;
            // 也不是进入这里
            default:
                System.out.println("default");
        }
    }

    public static void readMsg(){
        while(true){
            Date now = new Date();
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String aa = f.format(now);
            Map map = new HashMap<String,String>();
            map.put("tt" , "25400.986503000015");
            map.put("errcode" , "2002");
            map.put("bc" , "0");
            map.put("code" , "102002");
            map.put("mod","10");
            map.put("vtype","");
            map.put("type","2");
            map.put("vid","6301");
            map.put("src_ip","125.80.137.157");
            map.put("uid","bca53047_6301_eason");
            map.put("sd","http://t-alflvlive01.e.vhall.com");
            map.put("bt","0");
            map.put("bu","1");
            map.put("si","125.80.143.226");
            map.put("from","");
            map.put("tag","login");
            map.put("app_id","bca53047");
            map.put("ver","");
            map.put("biz_role","0");
            map.put("log_session","bca53047_63011564550034594");
            map.put("p","lss_8a65f79b");
            map.put("browser_name","Safari");
            map.put("ndi","");
            map.put("s","bca53047_63011564550034594");
            map.put("flow_type","2");
            map.put("biz_des01","0");
            map.put("pf","3");
            map.put("guid","");
            map.put("topic","");
            map.put("nginx_utc_datetime",aa);
            map.put("vfid","1053");
            map.put("biz_id","-");
            map.put("aid","lss_8a65f79b");
            map.put("fd","vhallyun/lss_8a65f79b.flv");

            String json = JSON.toJSONString(map);
            System.out.println(json);
            WriteKafkaMsg msg = new WriteKafkaMsg();
            msg.writeMsg(json);
        }
    }
}
