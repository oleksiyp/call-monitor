package call_monitor;

import java.lang.management.ThreadInfo;
import java.util.*;

class ThreadCallMonitor {
    Stack<StackItem> stack = new Stack<>();
    Stack<StackItem> secondStack = new Stack<>();
    String threadName;

    public void update(ThreadInfo info, boolean init) {
        threadName = info.getThreadName();
        List<StackTraceElement> elements = new ArrayList<>();
        elements.addAll(Arrays.asList(info.getStackTrace()));
        Collections.reverse(elements);

        int j = 0;
        for (int i = 0; i < elements.size(); i++) {
            StackItem item = new StackItem(i,
                    elements.get(i),
                    i > 0 ? stack.get(i - 1) : null);

            if (prePush(i, item, stack)) {
                stack.push(item);
            } else {
                stack.get(i).increment();
            }
            item = stack.get(i);
            if (item.getCounter() > ProcessCallMonitor.THRESHOLD) {
                while (j <= i) {
                    if (prePush(j, item, secondStack)) {
                        secondStack.push(item);
                        out(item);
                    }
                    j++;
                }
            }
        }
    }

    static StackItem last;
    private void out(StackItem item) {
        if (item.isOut()) {
            return;
        }
        if (item.getPrev() != last) {
            System.out.println();
        }
        last = item;
        System.out.printf("%30s %4d %50s.%-30s %30s%n",
                cut(threadName, 30),
                item.getStackLevel(),
                cut(item.getClassName(), 50, false),
                cut(item.getMethodName(), 30),
                cut(item.getFileName(), 30));
        item.setOut(true);
    }

    private static String cut(String str, int sz) {
        return cut(str, sz, true);
    }

    private static String cut(String str, int sz, boolean start) {
        if (str == null) {
            return "-";
        }
        if (str.length() <= sz) {
            return str;
        }
        if (start) {
            return str.substring(0, sz - 3) + "...";
        } else {
            return "..." + str.substring(str.length() - sz + 3);
        }
    }

    private boolean prePush(int i, StackItem item, Stack<StackItem> stack) {
        if (i < stack.size()) {
            StackItem inStackItem = stack.get(i);
            if (inStackItem.equals(item)) {
                return false;
            } else {
                while (i < stack.size()) {
                    stack.pop();
                }
                return true;
            }
        } else {
            return true;
        }
    }
}
