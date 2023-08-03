package blossom.project.easyconfig.config;

import com.google.common.eventbus.EventBus;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author: 张锦标
 * @date: 2023/7/16 10:02
 * EventBus类
 */
public class EventBusConfig {
    public EventBus eventBus;
    DeferredResult deferredResult;
}
