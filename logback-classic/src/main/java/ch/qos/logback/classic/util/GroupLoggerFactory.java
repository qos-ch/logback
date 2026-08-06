package ch.qos.logback.classic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带分组功能的日志工厂。
 * 模拟 LoggerFactory.getLogger(Class, String group) 的效果。
 *
 * 使用方式：
 * <pre>
 *   Logger logger = GroupLoggerFactory.getLogger(OrderService.class, "order");
 *   try {
 *       logger.info("处理订单...");
 *   } finally {
 *       GroupLoggerFactory.clearGroup();
 *   }
 * </pre>
 *
 * @author YoranYe
 */
public class GroupLoggerFactory {

    /**
     * 获取带分组的 Logger。
     * 日志将输出到 logs/{group}/ 目录下。
     *
     * @param clazz 当前类
     * @param group 日志分组名称（如 "order", "user", "payment"）
     * @return SLF4J Logger 实例
     */
    public static Logger getLogger(Class<?> clazz, String group) {
        // 将 group 设置到 ThreadLocal 中，TurboFilter 会自动读取并注入 MDC
        GroupContext.setGroup(group);
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取带分组的 Logger（使用 logger name 而非 Class）。
     */
    public static Logger getLogger(String name, String group) {
        GroupContext.setGroup(group);
        return LoggerFactory.getLogger(name);
    }

    /**
     * 手动设置当前线程的日志分组。
     * 适用于 AOP、拦截器等场景。
     */
    public static void setGroup(String group) {
        GroupContext.setGroup(group);
    }

    /**
     * 清除当前线程的日志分组。
     * 必须在 finally 块中调用，防止线程复用时 MDC 污染。
     */
    public static void clearGroup() {
        GroupContext.clear();
        org.slf4j.MDC.remove(GroupContext.getMdcKey());
    }
}