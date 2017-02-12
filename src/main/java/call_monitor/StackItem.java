package call_monitor;

class StackItem {
    private final int stackLevel;
    private final String className;
    private final String fileName;
    private final int lineNumber;
    private final String methodName;
    private final StackItem prev;
    private boolean out;
    private int counter;

    public StackItem(int stackLevel, StackTraceElement stackTraceElement, StackItem prev) {
        this.stackLevel = stackLevel;
        className = stackTraceElement.getClassName();
        fileName = stackTraceElement.getFileName();
        lineNumber = stackTraceElement.getLineNumber();
        methodName = stackTraceElement.getMethodName();
        this.prev = prev;
    }

    public void increment() {
        counter++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackItem stackItem = (StackItem) o;

        if (getLineNumber() != stackItem.getLineNumber()) return false;
        if (getClassName() != null ? !getClassName().equals(stackItem.getClassName()) : stackItem.getClassName() != null) return false;
        if (getFileName() != null ? !getFileName().equals(stackItem.getFileName()) : stackItem.getFileName() != null) return false;
        return getMethodName() != null ? getMethodName().equals(stackItem.getMethodName()) : stackItem.getMethodName() == null;

    }

    @Override
    public int hashCode() {
        int result = getClassName() != null ? getClassName().hashCode() : 0;
        result = 31 * result + (getFileName() != null ? getFileName().hashCode() : 0);
        result = 31 * result + getLineNumber();
        result = 31 * result + (getMethodName() != null ? getMethodName().hashCode() : 0);
        return result;
    }

    public String getClassName() {
        return className;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public StackItem getPrev() {
        return prev;
    }

    public int getCounter() {
        return counter;
    }

    public int getStackLevel() {
        return stackLevel;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }
}
