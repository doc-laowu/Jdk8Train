package firstyarnapp;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;

import java.io.IOException;
import java.util.*;

public class AppClient {

    public static void main(String[] args) {

        try {
            if (AppClient.run(args)) {
                System.out.println("Application completed successfully");
            } else {
                System.out.println("Application Failed / Killed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean run(String[] args) throws IOException, YarnException, InterruptedException {
        final Path jarPath = new Path(args[0]);
        final int n = Integer.valueOf(args[1]);

        // 创建Yarn客户端
        YarnConfiguration conf = new YarnConfiguration();
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();

        // 创建yarn app
        YarnClientApplication app = yarnClient.createApplication();
        GetNewApplicationResponse appResponse = app.getNewApplicationResponse();

        // 获取集群最大的内存资源
        int maxMem = appResponse.getMaximumResourceCapability().getMemory();
        // 获取集群最大的Vcore资源
        int maxVCores = appResponse.getMaximumResourceCapability().getVirtualCores();

        ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
        ApplicationId appId = appContext.getApplicationId();
        // 设置是否在attempt失败时关闭container
        appContext.setKeepContainersAcrossApplicationAttempts(false);
        appContext.setApplicationName("firstApp");

        // 这里存储要执行的资源文件
        Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
        FileStatus scFileStatus = FileSystem.get(conf).getFileStatus(jarPath);
        LocalResource scRsrc =  LocalResource.newInstance(
                ConverterUtils.getYarnUrlFromPath(jarPath),
                LocalResourceType.FILE,
                LocalResourceVisibility.APPLICATION,
                scFileStatus.getLen(),
                scFileStatus.getModificationTime()
        );
        localResources.put("first-yarn-app.jar", scRsrc);

        // 设置环境变量
        Map<String, String> env = new HashMap<String, String>();
        StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$$())
                .append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
        for (String c : conf.getStrings(
                YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
            classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
            classPathEnv.append(c.trim());
        }
        classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./log4j.properties");
        if (conf.getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, false)) {
            classPathEnv.append(':');
            classPathEnv.append(System.getProperty("java.class.path"));
        }
        env.put("CLASSPATH", classPathEnv.toString());

        // 这里拼接命令
        Vector<CharSequence> vargs = new Vector<CharSequence>(30);
        vargs.add(ApplicationConstants.Environment.JAVA_HOME.$$() + "/bin/java");
        // Set Xmx based on am memory size
        vargs.add("-Xmx" + 128 + "m");
        // Set class name
        vargs.add("yarnapp.ApplicationMaster");
        // jar args
        vargs.add(args[0]);
        vargs.add(String.valueOf(n));
        // Set params for Application Master
        vargs.add("--container_memory " + String.valueOf(128));
        vargs.add("--container_vcores " + String.valueOf(1));
        vargs.add("--num_containers " + String.valueOf(1));
        vargs.add("--priority " + String.valueOf(0));

        vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stdout");
        vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stderr");

        StringBuilder command = new StringBuilder();
        for (CharSequence str : vargs) {
            command.append(str).append(" ");
        }
        List<String> commands = new ArrayList<String>();
        commands.add(command.toString());
        // 这里设置AppMaster启动的上下文
        ContainerLaunchContext amContainer = ContainerLaunchContext.newInstance(
                localResources, env, commands, null, null, null);
        // 设置AppMasrter的内存和core
        Resource capability = Resource.newInstance(128, 1);
        appContext.setResource(capability);

        appContext.setAMContainerSpec(amContainer);
        Priority pri = Priority.newInstance(0);
        appContext.setPriority(pri);
        appContext.setQueue("batch_etl");
        yarnClient.submitApplication(appContext);

        ApplicationReport appReport = yarnClient.getApplicationReport(appId);
        YarnApplicationState appState = appReport.getYarnApplicationState();
        while (appState != YarnApplicationState.FINISHED
                && appState != YarnApplicationState.KILLED
                && appState != YarnApplicationState.FAILED) {
            Thread.sleep(100); // 这里每次休眠100ms循环获取
            appReport = yarnClient.getApplicationReport(appId);
            appState = appReport.getYarnApplicationState();
        }
        if (appState == YarnApplicationState.FINISHED) {
            return true;
        } else {
            return false;
        }
    }

}
