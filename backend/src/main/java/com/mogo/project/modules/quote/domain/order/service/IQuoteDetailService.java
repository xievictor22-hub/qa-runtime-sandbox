package com.mogo.project.modules.quote.domain.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import org.springframework.web.multipart.MultipartFile;

public interface IQuoteDetailService extends IService<QuoteDetail> {



    /**
     * 级联删除：删除主行及其子件
     */
    boolean removeDetailAndItems(Long id);


}