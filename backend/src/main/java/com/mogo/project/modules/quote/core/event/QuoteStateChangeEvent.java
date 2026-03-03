package com.mogo.project.modules.quote.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 定义一个事件，交给监听器取使用，当前为报价单状态修改事件
 */
@Getter
public class QuoteStateChangeEvent extends ApplicationEvent {

    private final Long quoteId;
    private final String action; // 可选：记录是哪个动作触发的

    public QuoteStateChangeEvent(Object source, Long quoteId, String action) {
        super(source);
        this.quoteId = quoteId;
        this.action = action;
    }
}
