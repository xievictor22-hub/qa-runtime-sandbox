package com.mogo.project.modules.quote.core.listener;

import com.mogo.project.modules.quote.core.event.QuoteStateChangeEvent;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // 监听到事件后，自动执行
    @Async // 建议异步执行，不阻塞主业务线程
    @EventListener
    public void handleQuoteStateChange(QuoteStateChangeEvent event) {
        log.info("监听到报价单[{}]状态变更，动作：{}，正在推送WebSocket...", event.getQuoteId(), event.getAction());

        // 方案 A：列表页专用广播 (发送到固定地址)
        // 所有在看列表页的人都会收到这个消息
        QuoteDetail.QuoteUpdateMessage msg = new QuoteDetail.QuoteUpdateMessage(event.getQuoteId(), "REFRESH");
        messagingTemplate.convertAndSend("/topic/quote-updates", msg); // 注意这里没有 ID 后缀
    }
}
