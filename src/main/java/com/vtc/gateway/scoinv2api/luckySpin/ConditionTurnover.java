/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.vtc.common.constant.Constant;
import com.vtc.common.dao.entity.LuckySpin;
import com.vtc.common.dao.entity.LuckySpinItem;
import com.vtc.common.dao.entity.LuckySpinItemOfLuckySpin;
import com.vtc.common.dao.entity.TopupCardHistory;
import com.vtc.common.dao.entity.TurnoverLandmark;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.TopupCardHistoryRepository;
import com.vtc.common.dao.repository.TurnoverLandmarkRepository;
import com.vtc.common.utils.DateUtils;
import com.vtc.gateway.scoinv2api.luckySpin.service.LuckySpinHistoryService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jul 18, 2019
 */
@Component
public class ConditionTurnover {
    
//    @Autowired
//    LuckySpinSettingRepository luckySpinSettingRepo;
    
    @Autowired
    TurnoverLandmarkRepository turnoverRepo;
    
    @Autowired
    LuckySpinHistoryService luckySpinHistoryService;
    
    @Autowired
    TopupCardHistoryRepository topupCardHistoryRepo;
    
    static Logger             LOGGER = LoggerFactory.getLogger(ConditionTurnover.class);
    
    public boolean checkPickItemFolowTurnover(LuckySpin luckySpin, long totalTurnover,LuckySpinItem item) {

        boolean flagItemQuantityBalance = false;
        List<TurnoverLandmark> turnoverLandmarks = turnoverRepo.
                findByGameAndGameIdAndItemId(luckySpin.getType(), luckySpin.getId(), item.getId());
        if (CollectionUtils.isEmpty(turnoverLandmarks)) return true;
        
        for(TurnoverLandmark turnoverLandmark : turnoverLandmarks) {
            if (totalTurnover <= turnoverLandmark.getTurnoverLandmark()) {
                flagItemQuantityBalance = true;
                
                List<String> typeGifts = new ArrayList<String>();
                typeGifts.add(item.getType());
                int TOTAL_QUANTITY_ITEM_RECEIVED = 0;
                if (item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN)
                      || item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN_CARD)) {
                    TOTAL_QUANTITY_ITEM_RECEIVED = luckySpinHistoryService.
                        countSpinHistory(luckySpin.getId(), typeGifts,item.getValue(),  null);
                } else {
                    TOTAL_QUANTITY_ITEM_RECEIVED = luckySpinHistoryService.
                            countSpinHistory(luckySpin.getId(), typeGifts, null,  null);
                }
                
//                String itemTypeSave = item.getType();
//                if (item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN_CARD)) 
//                    itemTypeSave += "_" + String.valueOf(item.getValue());
//                LuckySpinSetting luckySpinSetting = luckySpinSettingRepo.
//                        findByLuckySpinAndKeyNameAndStatus(luckySpin, itemTypeSave, Constant.STATUS_ACTIVE);
//                if (!ObjectUtils.isEmpty(luckySpinSetting)) TOTAL_QUANTITY_ITEM_RECEIVED = (int) luckySpinSetting.getIntValue();
                
                //Limit item by TURNOVER
                if (TOTAL_QUANTITY_ITEM_RECEIVED >= turnoverLandmark.getLimitQuantity()) {
                    flagItemQuantityBalance = false;
                    break;
                }
                
                //Limit item by DAY
                if (turnoverLandmark.getLimitType() == Constant.LUCKYSPIN_TURNOVER_LIMIT_TYPE_DAY) { 
                    int totalReceivedDay = 0;
                        if (item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN)
                            || item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN_CARD)) {
                            totalReceivedDay = luckySpinHistoryService.countSpinHistory(luckySpin.getId(), typeGifts, item.getValue(), new Date());
                        } else {
                            totalReceivedDay = luckySpinHistoryService.countSpinHistory(luckySpin.getId(), typeGifts, null, new Date());
                        }
                        
                    int dayToEndEvent = DateUtils.
                            toStringDaysBetweenTwoDay(new Date(), luckySpin.getEndDate(), DateUtils.DATE_MYSQL_FORMAT).size();
                    int totalLimitItemDay = 
                            (turnoverLandmark.getLimitQuantity() - TOTAL_QUANTITY_ITEM_RECEIVED) / dayToEndEvent;
                    if (totalReceivedDay >= totalLimitItemDay) {
                        flagItemQuantityBalance = false;
                        break;
                    }
                }
                break;
            }
        };
        
        return flagItemQuantityBalance;
        
    }

    public boolean checkPickUserFolowTopupCard(UserInfo userInfo, LuckySpinItem item) {
        long scoinId = userInfo.getUserVTC().getScoinId();
        if (scoinId < 1) {
            LOGGER.info("Accoout Empty or error");
            return false;
        }
        Long sumTopupCard = topupCardHistoryRepo.sumCardValueByScoinId(scoinId);
        if (ObjectUtils.isEmpty(sumTopupCard)) sumTopupCard = (long) 0;
        if (sumTopupCard < item.getValue() * 10) {
            return false;
        }
        
        return true;
    }
    
    public long pickRatioItemFolowPersonalTurnover(long userTopupCost, List<LuckySpinItemOfLuckySpin> bigItems) {
      // list landmark topup value from value item personal
         List<Long> landmarksPersonalTurnover = bigItems.stream()
                                                     .map(value -> value.getItem().getValue()*5)
                                                     .collect(Collectors.toList());
         
         
         long landmark = 0;
         for (int i = 0; i < landmarksPersonalTurnover.size() ; i++) {
             if (landmarksPersonalTurnover.get(i) < userTopupCost) continue;
             if (landmarksPersonalTurnover.get(i) == userTopupCost) {
                 landmark = landmarksPersonalTurnover.get(i);
                 break;
             }
             
             landmark = landmarksPersonalTurnover.get(i - 1);
             break;
         };
         
         if (landmark == 0) landmark = landmarksPersonalTurnover.get(landmarksPersonalTurnover.size() - 1);
         
         return landmark;
     }
    
    public long pickRatioItemFolowPersonalTurnover(long userTopupCost) {
        List<Long> personalTurnovers = new ArrayList<Long>();
        personalTurnovers.add((long) 25000000);
        
        long landmark = 0;
        for (int i = 0; i < personalTurnovers.size() ; i++) {
            if (personalTurnovers.get(i) < userTopupCost) continue;
            if (personalTurnovers.get(i) == userTopupCost) {
                landmark = personalTurnovers.get(i);
                break;
            }
            
            landmark = personalTurnovers.get(i - 1);
            break;
        };
        
        return landmark;
    }
    
    public int turnSpinAddTopupCardScoin(TopupCardHistory topupCard) {
        long cardValue = topupCard.getTotalPayment();
        int turnAddNew = 0;
        if (cardValue < 50000) return 0;
        if (cardValue < 1000000) {
            turnAddNew = (int) cardValue / 50000;
        } else if (cardValue < 5000000) {
            turnAddNew = ((int) cardValue / 50000) + (int) cardValue / 500000;
        } else if (cardValue == 5000000) {
            turnAddNew = ((int) cardValue / 10000) + (int) (2 * cardValue / 500000);
        }
        return turnAddNew;
    }

}
