package call_monitor;

import com.sun.tools.attach.*;

import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CallMonitor {
    private static final String CONNECTOR_ADDRESS =
            "com.sun.management.jmxremote.localConnectorAddress";

    private final boolean allLocal;
    private final List<String> pids;
    private final List<String> urls;

    public CallMonitor(boolean allLocal, List<String> pids, List<String> urls) {
        this.allLocal = allLocal;
        this.pids = pids;
        this.urls = urls;
    }

    private JMXServiceURL getURLForPid(String pid) {
        try {
            // attach to the target application
            final VirtualMachine vm = VirtualMachine.attach(pid);

            // get the connector address
            String connectorAddress =
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

            // no connector address, so we start the JMX agent
            if (connectorAddress == null) {
                String agent = vm.getSystemProperties().getProperty("java.home") +
                        File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent);

                // agent is started, get the connector address
                connectorAddress =
                        vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                assert connectorAddress != null;
            }
            return new JMXServiceURL(connectorAddress);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void main(String[] args) {

        if (args.length == 1 && args[0].equals("-ps")) {
            VirtualMachine.list()
                    .stream()
                    .forEach(desc -> System.out.println(desc.id() + " " + desc.displayName()));
            return;
        }
        List<String> pids = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        boolean allLocal = args.length == 0;
        for (String arg : args) {
            if ("-local".equals(arg)) {
                allLocal = true;
            } else if (arg.matches("^\\d+$")) {
                pids.add(arg);
            } else {
                urls.add(arg);
            }
        }

        CallMonitor monitor = new CallMonitor(allLocal, pids, urls);
        try {
            monitor.run();
        } catch (Exception e) {
            System.err.println("Error: " + e);
            System.exit(1);
        }
    }

    private void run() throws Exception {
        List<JMXServiceURL> jmxUrls = new ArrayList<JMXServiceURL>();
        for (String url : urls) {
            jmxUrls.add(new JMXServiceURL(url));
        }
        for (String pid : pids) {
            jmxUrls.add(getURLForPid(pid));
        }

        for (JMXServiceURL url : jmxUrls) {
            launchProcessCall(url);
        }

        Set<String> pids = new HashSet<>();
        while (true) {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();

            Set<String> newPids = list
                    .stream()
                    .map(VirtualMachineDescriptor::id)
                    .collect(Collectors.toSet());

            Set<String> added = new HashSet<>(newPids);
            added.removeAll(pids);

            added.stream().forEach(pid -> launchProcessCall(getURLForPid(pid)));

            pids = newPids;
        }
    }

    private void launchProcessCall(JMXServiceURL url) {
        if (url == null) {
            return;
        }

        new Thread(() -> {
            ProcessCallMonitor processCallMonitor = new ProcessCallMonitor();
            try {
                processCallMonitor.connect(url);
            } catch (IOException e) {
                System.err.println("Connect error: " + e);
                return;
            }
            processCallMonitor.run();
        }).start();
    }
}
