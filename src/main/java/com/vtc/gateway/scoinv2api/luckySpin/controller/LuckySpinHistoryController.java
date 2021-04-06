/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vtc.common.AbstractController;
import com.vtc.common.constant.Constant;
import com.vtc.common.dto.request.AbstractResquest;
import com.vtc.common.utils.JsonMapperUtils;
import com.vtc.gateway.scoinv2api.luckySpin.service.LuckySpinHistoryService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jul 2, 2019
 */
@RestController
public class LuckySpinHistoryController extends AbstractController<LuckySpinHistoryService> {
    
    @GetMapping("/anonymous/lucky-spin-history/all")
    public ResponseEntity<?> getSpinHistoryAno(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                               String type_item,
                                            AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        return response(toResult(service.getSpinHistory(luckySpinId, type_item, request), service.countSpinHistory(null, null, null, null)));
    }
    
    @GetMapping("/lucky-spin-history/all")
    public ResponseEntity<?> getSpinHistory(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                            String type_item,
                                            AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        return response(toResult(service.getSpinHistory(luckySpinId, type_item, request), service.countSpinHistory(null, null, null, null)));
    }
    
    @GetMapping("/lucky-spin-history")
    public ResponseEntity<?> getSpinHistoryByType(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                              @RequestParam(value = "type_gift", required=false) String typeGift,
                                              AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(typeGift));
        List<String> typeGifts = new ArrayList<String>();
        typeGifts.add(typeGift);
        return response(toResult(service.getSpinHistoryByType(luckySpinId, typeGift, request),
                service.countSpinHistoryHasUser(luckySpinId, typeGifts)));
    }
    
    @GetMapping("/lucky-spin-history/tudo")
    public ResponseEntity<?> getSpinTudo(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                         AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        List<String> typeGifts = new ArrayList<String>();
        typeGifts.add(Constant.LUCKYSPIN_GIFT_SCOIN_CARD);
        typeGifts.add(Constant.LUCKYSPIN_GIFT_SCOIN);
        return response(toResult(service.getSpinTudo(luckySpinId, request),
                service.countSpinHistoryHasUser(luckySpinId, typeGifts)));
    }
    
    @GetMapping("/lucky-spin-history/turn")
    public ResponseEntity<?> getSpinTurn(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                         AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        return response(toResult(service.getTurnSpinHistory(luckySpinId, request),
                service.countTurnSpinHistory(luckySpinId)));
    }
    
    @GetMapping("/lucky-spin-history/buy-turn")
    public ResponseEntity<?> getSpinTurnBuyHistory(@RequestParam(value = "lucky_spin_id") Long luckySpinId,
                                         AbstractResquest request) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        return response(toResult(service.getSpinTurnBuyHistory(luckySpinId, request),
                service.countSpinTurnBuyHistory(luckySpinId)));
    }
    
    @PostMapping("/lucky-spin-history/create-history-fake")
    public ResponseEntity<?> createHistoryFake(@ModelAttribute MultipartFile file) throws IOException, InvalidFormatException {
        return response(toResult(service.createSpinHistoryFake(file)));
    }
    
    @GetMapping("/anonymous/lucky-spin-history/gift-quantity-exist")
    public ResponseEntity<?> getReportGiftQuantityExist(@RequestParam(value = "lucky_spin_id") Long luckySpinId) {
        LOGGER.info("===============REQUEST============== \n {}", JsonMapperUtils.toJson(luckySpinId));
        return response(toResult(service.giftQuantityExist(luckySpinId)));
    }

}
