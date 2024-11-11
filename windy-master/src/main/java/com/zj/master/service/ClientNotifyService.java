package com.zj.master.service;

import com.zj.common.entity.dto.ResultEvent;
import com.zj.master.notify.INotifyEvent;
import com.zj.master.notify.INotifyInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Slf4j
@Service
public class ClientNotifyService {

    private final Map<String, INotifyEvent> notifyEventMap;
    private final List<INotifyInterceptor> notifyInterceptors;

    public ClientNotifyService(List<INotifyEvent> notifyEventList, List<INotifyInterceptor> notifyInterceptors) {
        this.notifyEventMap = notifyEventList.stream()
                .collect(Collectors.toMap(event -> event.type().name(), event -> event));
        this.notifyInterceptors = notifyInterceptors;
    }

    public Boolean notifyEvent(ResultEvent resultEvent) {
        log.info("receive notify event type={} executeId={}", resultEvent.getNotifyType(), resultEvent.getExecuteId());
        String notifyType = resultEvent.getNotifyType().name();
        INotifyEvent notifyEvent = notifyEventMap.get(notifyType);
        if (Objects.isNull(notifyEvent)) {
            log.info("can not find notify type event");
            return false;
        }
        if (CollectionUtils.isNotEmpty(notifyInterceptors)) {
            handleBefore(resultEvent);
            boolean handleResult = notifyEvent.handle(resultEvent);
            handleAfter(resultEvent, handleResult);
            return handleResult;
        }
        return notifyEvent.handle(resultEvent);
    }

    private void handleBefore(ResultEvent resultEvent) {
        for (INotifyInterceptor notifyInterceptor : notifyInterceptors) {
            try {
                notifyInterceptor.before(resultEvent);
            } catch (Exception e) {
                log.info("class={} before handle event ={} error",
                        notifyInterceptor.getClass().getSimpleName(), resultEvent.getNotifyType(), e);
            }
        }
    }

    private void handleAfter(ResultEvent resultEvent, boolean handleResult) {
        for (INotifyInterceptor notifyInterceptor : notifyInterceptors) {
            try {
                notifyInterceptor.after(resultEvent, handleResult);
            } catch (Exception e) {
                log.info("class={} after handle event ={} error",
                        notifyInterceptor.getClass().getSimpleName(), resultEvent.getNotifyType(), e);
            }
        }
    }
}
