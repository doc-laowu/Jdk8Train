package yarnapp;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Client {

	public static void main(String[] args) throws Exception {
		try {
			Client clientObj = new Client();
			if (clientObj.run(args)) {
				System.out.println("Application completed successfully");
			} else {
				System.out.println("Application Failed / Killed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean run(String[] args) throws Exception {
		// 读取YARNConfiguration并初始化YARNClient：与ApplicationMaster相似，客户端还使用YARNConfiguration类加载Hadoop-YARN配置文件并读取指定的输入参数。
        // 客户端在客户端节点上启动YARNClient服务。
        // 在此示例中，前两个参数直接传递到ApplicationMaster的ContainerLaunchContext，第三个参数是作业资源（带有ApplicationMaster的jar文件）的HDFS路径：
		final String command = args[0];
		final int n = Integer.valueOf(args[1]);
		final Path jarPath = new Path(args[2]);

		System.out.println("Initializing YARN configuration");
		YarnConfiguration conf = new YarnConfiguration();
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		yarnClient.start();

		// 连接到ResourceManager并请求一个新的应用程序ID：客户端连接到ResourceManager服务并请求一个新的应用程序。
        // 请求的响应（即YarnClientApplication – GetNewApplicationResponse）包含新的应用程序ID以及群集的最小和最大资源容量。
		System.out.println("Requesting ResourceManager for a new Application");
		YarnClientApplication app = yarnClient.createApplication();

		// 为Application Master定义ContainerLaunchContext：应用程序的第一个容器是ApplicationMaster的容器。
        // 客户端定义一个ContainerLaunchContext，该容器包含用于启动ApplicationMaster服务的信息。
        // ContainerLaunchContext将包含以下信息：
		System.out.println("Initializing ContainerLaunchContext for ApplicationMaster container");
		ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);

		// 为ApplicationMaster设置jar：NodeManager应该能够找到ApplicationMaster jar文件。
        // jar文件放置在HDFS上，并由NodeManager作为LocalResource进行访问，如以下代码所示：
		System.out.println("Adding LocalResource");
		LocalResource appMasterJar = Records.newRecord(LocalResource.class);
		FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
		appMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
		appMasterJar.setSize(jarStat.getLen());
		appMasterJar.setTimestamp(jarStat.getModificationTime());
		appMasterJar.setType(LocalResourceType.FILE);
		appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC);

		// 为ApplicationMaster设置CLASSPATH：shell命令可能需要运行ApplicationMaster，这需要一些环境变量。 客户端可以指定环境变量列表。
		System.out.println("Setting environment");
		Map<String, String> appMasterEnv = new HashMap<String, String>();
		// TODO 这里设置yarn的classPath, 没有就yarn默认的安装位置的
		for (String c : conf.getStrings(
				YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), c.trim(), File.pathSeparator);
		}

		Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), Environment.PWD.$() + File.separator + "*", File.pathSeparator);

		// 设置ApplicationMaster的资源要求：资源要求定义为ApplicationMaster所需的内存和CPU内核。
		System.out.println("Setting resource capability");
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(256);
		capability.setVirtualCores(1);

		// 启动ApplicationMaster服务的命令：在此示例中，ApplicationMaster是一个Java程序，因此客户端将定义一个Java jar命令来启动ApplicationMaster服务。
		System.out.println("Setting command to start ApplicationMaster service");
		amContainer.setCommands(Collections.singletonList("/usr/local/jdk1.8.0_121/bin/java"
				+ " -Xmx256M" + " yarnapp.ApplicationMaster"
				+ " " + command + " " + String.valueOf(n) + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
				+ " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/stderr"));
		amContainer.setLocalResources(Collections.singletonMap(
				"first-yarn-app.jar", appMasterJar));
		amContainer.setEnvironment(appMasterEnv);

		// 创建ApplicationSubmissionContext：客户端为应用程序定义ApplicationSubmissionContext。 提交上下文包含诸如应用程序名称，队列，优先级等信息。
		System.out.println("Initializing ApplicationSubmissionContext");
		ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
		appContext.setApplicationName("first-yarn-app");
		appContext.setApplicationType("YARN");
		appContext.setAMContainerSpec(amContainer);
		appContext.setResource(capability);
		appContext.setQueue("batch_etl");

		// 提交应用程序并等待完成：客户端提交应用程序并等待应用程序完成。 它向ResourceManager请求应用程序状态。
		ApplicationId appId = appContext.getApplicationId();
		System.out.println("Submitting application " + appId);
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
