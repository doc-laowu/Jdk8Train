package com.neusoft.guava;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Title: GuavaCacheTest
 * @ProjectName Jdk8Train
 * @Description: TODO guava的github地址:https://github.com/google/guava/wiki/CachesExplained
 * @Author yisheng.wu
 * @Date 2019/12/2615:32
 */
public class GuavaCacheTest {

    /**
      *
      * 使用CacheBuilder创建的Caches 不会自动执行清理和回收工作 相反，它会在在写操作或者读操作期间，只执行小量的维护maintenance操作。
      *
      *
      *
      **/
    private static Cache<String,String> cache = CacheBuilder.newBuilder()
            // 设置最大的长度
            .maximumSize(3)
            // 如果不同的条目具有不同权重,设置最大的权重值，当超过时会回收内存空间
            .maximumWeight(100000L)
            // 如果不同的条目具有不同权重,来指定一个权重
            .weigher(new Weigher<String, String>(){
                @Override
                public int weigh(String s, String s2) {
                    return 0;
                }
            })
            // 对象多久没有被访问后过期
//                .expireAfterAccess(3, TimeUnit.SECONDS)
            // 对象被写入到缓存后多久过期
            .expireAfterWrite(3, TimeUnit.SECONDS)
            /**
              *  刷新和回收不太一样，正如LoadingCache.refresh(K)所说的，刷新一个键key的话，会为这个键加载新的值，这可能是异步的。
              *  当键正在被刷新时，会依然把旧值返回给调用者。对比之下，回收会强制要求获取值的操作先等待直到新值被加载进去。
              *  如果在刷新过程中出现了什么异常，那么会依然把旧值保存在cache缓存中，并且会针对这个异常做日志记录同时把异常吞掉。
              *  在刷新时，通过重写CacheLoader.reload(K,V)方法， CacheLoader可以指定一个智能的行为：它允许你在计算新值的过程中，仍然还可以继续使用旧值。
              **/
            .refreshAfterWrite(2, TimeUnit.SECONDS)
            // 可以通过weakKeys和weakValues方法指定Cache只保存对缓存记录key和value的弱引用。这样当没有其他强引用指向key和value时，key和value对象就会被垃圾回收器回收。
            .weakValues()
            // 当元素被移除的时候设置监听器,同步监听的
            .removalListener(new RemovalListener<String, String>() {
                @Override
                public void onRemoval(RemovalNotification<String, String> removalNotification) {
                    System.out.println(removalNotification.getKey() + ":" + removalNotification.getValue() + " removed");
                }
            })
            // 开启统计信息开关
            .recordStats()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    Thread.sleep(1000); //休眠1s，模拟加载数据
                    System.out.println(key + " is loaded from a cacheLoader!");
                    return key + "'s value";
                }

                @Override
                public ListenableFuture<String> reload(String key, String oldValue) throws Exception {

//                    if (neverNeedsRefresh(key)) {
//                        return Futures.immediateFuture(prevGraph);
//                    } else {
//                        // asynchronous!
//                        ListenableFutureTask<Graph> task = ListenableFutureTask.create(new Callable<Graph>() {
//                            public Graph call() {
//                                return getGraphFromDatabase(key);
//                            }
//                        });
//                        executor.execute(task);
//                        return task;
//                    }

                    return super.reload(key, oldValue);
                }
            });


    public static void main(String[] args) throws ExecutionException {

        // 往cache中put数据
        cache.put("k1", "000");
        cache.put("k2", "002");
        cache.put("k3", "003");

        // 批量取数据
        cache.getAllPresent(Arrays.asList("k1", "k2", "k3"));

        // 批量删除key值
//        cache.invalidateAll(Arrays.asList("k1", "k2"));

        // 删除key值
//        cache.invalidate("k1");


        /*
         *
         *
         * 模拟两个县线程同时获取一个key值，当不存在时加载数据到缓存中，结果为：最后只有一个线程能加载成功
         *
         *
         */
//        new Thread(new Runnable() {
//            public void run() {
//                System.out.println("thread1");
//                try {
//                    String value = cache.get("key", new Callable<String>() {
//                        public String call() throws Exception {
//                            System.out.println("load1"); //加载数据线程执行标志
//                            Thread.sleep(1000); //模拟加载时间
//                            return "auto load by Callable";
//                        }
//                    });
//                    System.out.println("thread1 " + value);
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            public void run() {
//                System.out.println("thread2");
//                try {
//                    String value = cache.get("key", new Callable<String>() {
//                        public String call() throws Exception {
//                            System.out.println("load2"); //加载数据线程执行标志
//                            Thread.sleep(1000); //模拟加载时间
//                            return "auto load by Callable";
//                        }
//                    });
//                    System.out.println("thread2 " + value);
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        /**
          * 当调用LoadingCache的get方法时，如果缓存不存在对应key的记录，则CacheLoader中的load方法会被自动调用从外存加载数据，
          * load方法的返回值会作为key对应的value存储到LoadingCache中，并从get方法返回。
          **/
        cache.getIfPresent("key1");
        cache.getIfPresent("key2");
        cache.getIfPresent("key3");
    }

}
