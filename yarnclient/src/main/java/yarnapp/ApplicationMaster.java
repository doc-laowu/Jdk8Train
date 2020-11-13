package yarnapp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Records;

import java.util.Collections;

public class ApplicationMaster {


	public static void main(String[] args) throws Exception {
        //读取YARN配置和输入参数：ApplicationMaster使用YARNConfiguration类加载Hadoop-YARN配置文件并读取指定的输入参数。
        // 对于此示例，第一个参数是shellCommand（例如/ bin / date），第二个参数是将在应用程序执行期间启动的numofContainers：
		System.out.println("Running ApplicationMaster");
		final String shellCommand = args[0];
		final int numOfContainers = Integer.valueOf(args[1]);
		Configuration conf = new YarnConfiguration();

		// 初始化AMRMClient和NMClient客户端：ApplicationMaster首先创建并初始化与ResourceManager服务AMRMClient和NodeManager服务NMClient的通信接口，
        // 如以下代码所示：
		System.out.println("Initializing AMRMCLient");
		AMRMClient<ContainerRequest> rmClient = AMRMClient.createAMRMClient();
		rmClient.init(conf);
		rmClient.start();

		System.out.println("Initializing NMCLient");
		NMClient nmClient = NMClient.createNMClient();
		nmClient.init(conf);
		nmClient.start();

		// 向ResourceManager注册尝试：ApplicationMaster将其自身注册到ResourceManager服务。
        // 它需要为尝试指定主机名，端口和跟踪URL。 成功注册后，ResourceManager会将应用程序状态移至RUNNING
		System.out.println("Register ApplicationMaster");
		rmClient.registerApplicationMaster(NetUtils.getHostname(), 0, "");

		// 定义ContainerRequest并添加容器的请求：客户端根据内存和内核（org.apache.hadoop.yarn.api.records.Resource）定义工作容器的执行要求。
        // 客户端还可以指定工作容器的优先级，首选的节点列表以及用于资源位置的机架。 客户端创建一个ContainerRequest引用，并在调用allocate（）方法之前添加请求：
		Priority priority = Records.newRecord(Priority.class);
		priority.setPriority(0);

		System.out.println("Setting Resource capability for Containers");
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);
		for (int i = 0; i < numOfContainers; ++i) {
			ContainerRequest containerRequested = new ContainerRequest(capability, null, null, priority, true);
			// 资源，节点，机架，优先级和放松位置标志
			rmClient.addContainerRequest(containerRequested);
		}

		// 请求分配，定义ContainerLaunchContext并启动容器：ApplicationMaster请求ResourceManager分配所需的容器，并将应用程序的当前进度通知给ResourceManager。
        // 因此，在第一个分配请求期间进度指示器的值为0。来自ResourceManager的响应包含分配的容器数。 ApplicationMaster为每个分配的容器创建ContainerLaunchContext，
        // 并请求相应的NodeManager启动该容器。 它将等待容器的执行。 在此示例中，为启动容器而执行的命令被指定为ApplicationMaster的第一个参数（/ bin / date命令）：
		int allocatedContainers = 0;
		System.out.println("Requesting container allocation from ResourceManager");
		while (allocatedContainers < numOfContainers) {
			AllocateResponse response = rmClient.allocate(0);
			for (Container container : response.getAllocatedContainers()) {
				++allocatedContainers;
				// 通过创建ContainerLaunchContext启动容器
				ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);
				ctx.setCommands(Collections.singletonList(shellCommand + " 1>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stdout" + " 2>"
						+ ApplicationConstants.LOG_DIR_EXPANSION_VAR
						+ "/stderr"));
				System.out.println("Starting container on node : " + container.getNodeHttpAddress());
				nmClient.startContainer(container, ctx);
			}
			Thread.sleep(100);
		}

		// 完成后，从ResourceManager取消注册ApplicationMaster：分配响应还包含已完成容器的列表。 一旦在响应中获取的所有容器开始在不同的NodeManager上执行，ApplicationMaster将等待其完成。
        // ContainerStatus类提供正在执行的容器的当前状态。 要注销ApplicationMaster，请在AMRMClient参考上调用unregisterApplicationMaster（）方法。
        // 通过unregister调用，ApplicationMaster发送应用程序的最终状态，应用程序消息和最终应用程序跟踪URL作为参数：
		int completedContainers = 0;
		while (completedContainers < numOfContainers) {
			AllocateResponse response = rmClient.allocate(completedContainers / numOfContainers);

			for (ContainerStatus status : response.getCompletedContainersStatuses()) {
				++completedContainers;
				System.out.println("Container completed : " + status.getContainerId());
				System.out.println("Completed container " + completedContainers);
			}
			Thread.sleep(100);
		}
		// 最后取消注册该AppMaster
		rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");

	}

}
