package call_monitor;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;

public class ProcessCallMonitor {
    public static final int DELAY = 50;
    public static final int THRESHOLD = 60;

    private ThreadMXBean threadMXBean;

    public void connect(JMXServiceURL url) throws IOException {
        final JMXConnector connector = JMXConnectorFactory.connect(url);
        final MBeanServerConnection remote = connector.getMBeanServerConnection();
        threadMXBean = ManagementFactory.newPlatformMXBeanProxy(remote, THREAD_MXBEAN_NAME, ThreadMXBean.class);
    }

    public void run() {
        boolean init = true;
        while (true) {
            long []ids = threadMXBean.getAllThreadIds();
            ThreadInfo []infos = threadMXBean.getThreadInfo(ids, Integer.MAX_VALUE);
            update(infos, init);
            init = false;
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    Map<Long, ThreadCallMonitor> callMonitorMap = new HashMap<>();

    private void update(ThreadInfo[] threadInfos, boolean init) {
        synchronized (System.out) {
            for (ThreadInfo info : threadInfos) {
                if (info == null) {
                    return;
                }
                ThreadCallMonitor monitor = callMonitorMap.get(info.getThreadId());
                if (monitor == null) {
                    monitor = new ThreadCallMonitor();
                    callMonitorMap.put(info.getThreadId(), monitor);
                }

                monitor.update(info, init);
            }
        }
    }

}
