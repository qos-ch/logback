package ch.qos.logback.classic.util;

/**
 * 基于 ThreadLocal 的日志分组上下文。
 * 用于在调用日志前设置 group，TurboFilter 会自动读取并注入 MDC。
 */
public class GroupContext {

    private static final String MDC_KEY = "LOG_GROUP";
    private static final ThreadLocal<String> GROUP_HOLDER = new ThreadLocal<String>();

    /**
     * 设置当前线程的日志分组
     */
    public static void setGroup(String group) {
        GROUP_HOLDER.set(group);
    }

    /**
     * 获取当前线程的日志分组
     */
    public static String getGroup() {
        return GROUP_HOLDER.get();
    }

    /**
     * 清除当前线程的日志分组（必须在 finally 中调用，防止内存泄漏）
     */
    public static void clear() {
        GROUP_HOLDER.remove();
    }

    /**
     * 获取 MDC 中使用的 key
     */
    public static String getMdcKey() {
        return MDC_KEY;
    }
}