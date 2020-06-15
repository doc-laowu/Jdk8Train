package com.neusoft.newio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Title: GetChannal
 * @ProjectName MyJdbcSink
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/4/2314:26
 */
public class GetChannal {

    private static final int BSIZE = 1024;

    public static void main(String[] args) throws IOException {

        String Filepath= "E:\\data.txt";

        FileChannel fc = new FileOutputStream(Filepath).getChannel();

        //将字节数组封装到缓冲区中。
        fc.write(ByteBuffer.wrap("Hello World!".getBytes()));

        //Closes this channel.
        fc.close();

        fc = new RandomAccessFile(Filepath, "rw").getChannel();

        fc.position(fc.size());

        fc.write(ByteBuffer.wrap("Some More".getBytes()));

        fc.close();

        fc = new FileInputStream(Filepath).getChannel();

        ByteBuffer buff = ByteBuffer.allocateDirect(BSIZE);

        fc.read(buff);

        /*翻转这个缓冲区。极限设置为当前位置，然后
         *位置设为零。如果标记被定义，那么它就是
         *丢弃。*/
        //prepare for writting
        buff.flip();

        //prepare for reading
        buff.clear();

        while (buff.hasRemaining())
            System.out.print((char)buff.get());

    }

}
