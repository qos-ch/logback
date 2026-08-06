package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.util.GroupContext;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * 自定义 TurboFilter：在日志事件创建前，将 GroupContext 中的 group 注入 MDC。
 * SiftingAppender 会根据 MDC 中的 LOG_GROUP 值路由到不同的日志文件。
 * @author YoranYe
 */
public class GroupRoutingFilter extends TurboFilter {

    private String mdcKey = GroupContext.getMdcKey(); // 默认 "LOG_GROUP"
    private String defaultGroup = "default";

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        // 优先从 GroupContext（ThreadLocal）获取 group
        String group = GroupContext.getGroup();

        // 如果 GroupContext 中没有，则检查 MDC 中是否已有（兼容手动 MDC.put 的场景）
        if (group == null || group.isEmpty()) {
            group = MDC.get(mdcKey);
        }

        // 如果都没有，使用默认值
        if (group == null || group.isEmpty()) {
            group = defaultGroup;
        }

        // 注入 MDC，供 SiftingAppender 的 Discriminator 使用
        MDC.put(mdcKey, group);

        return FilterReply.NEUTRAL;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    // ---- getter/setter（支持在 logback.xml 中配置） ----

    public String getMdcKey() {
        return mdcKey;
    }

    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }
}