/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;

import com.vtc.common.dao.entity.LuckySpinHistory;
import com.vtc.common.dto.request.AbstractResquest;
import com.vtc.common.dto.response.GetLuckySpinHistoryResponse;
import com.vtc.common.dto.response.GiftQuantityExistResponse;
import com.vtc.common.dto.response.LuckySpinTurnBuyHistoryResponse;
import com.vtc.common.dto.response.LuckySpinTurnHistoryResponse;
import com.vtc.common.dto.response.SpinHistoryGetLuckyResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jul 2, 2019
 */
public interface LuckySpinHistoryService extends AbstractInterfaceService<LuckySpinHistory, Long> {
    
    List<SpinHistoryGetLuckyResponse> getSpinHistory(Long luckySpinId, String typeItem, AbstractResquest request)
            throws ScoinBusinessException;
    
    List<SpinHistoryGetLuckyResponse> getSpinHistoryByType(Long luckySpinId, String typeGift, AbstractResquest request);
    
    List<SpinHistoryGetLuckyResponse> getSpinTudo(Long luckySpinId, AbstractResquest request);
    
    List<LuckySpinTurnHistoryResponse> getTurnSpinHistory(Long luckySpinId, AbstractResquest request);
    
    List<LuckySpinTurnBuyHistoryResponse> getSpinTurnBuyHistory(Long luckySpinId, AbstractResquest request);
    
    int countSpinHistory(Long luckySpinId, List<String> typeGifts, Long value, Date date);
    
    int countSpinHistoryHasUser(Long luckySpinId, List<String> typeGifts);
    
    int countTurnSpinHistory(Long luckySpinId);
    
    int countSpinTurnBuyHistory(Long luckySpinId);
    
    String createSpinHistoryFake(MultipartFile file) throws IOException, InvalidFormatException ;
    
    List<GiftQuantityExistResponse> giftQuantityExist(Long luckySpinId) throws ScoinBusinessException;

}
