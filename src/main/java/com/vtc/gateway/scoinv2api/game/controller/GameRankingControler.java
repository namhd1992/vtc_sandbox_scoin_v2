/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.game.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vtc.common.AbstractController;
import com.vtc.common.dto.request.GameRankingAwardRequest;
import com.vtc.common.utils.JsonMapperUtils;
import com.vtc.gateway.scoinv2api.game.service.GameRankingService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 12, 2020
 */
@RestController
@RequestMapping("/game-ranking")
public class GameRankingControler extends AbstractController<GameRankingService> {
    
    @GetMapping("/get")
    public ResponseEntity<?> getGameRanking(@RequestParam(name = "service_id") long serviceId ,
                                            @RequestParam(name = "week", required = false) String week) {
        LOGGER.info("============ REQUEST =========== \n {}", serviceId + " , " + week);
        return response(toResult(service.getGameRanking(serviceId, week)));
    }
    
    @GetMapping("/gift")
    public ResponseEntity<?> getRankingGift(@RequestParam(name = "service_id") long serviceId) {
        LOGGER.info("============ REQUEST =========== \n {}", serviceId );
        return response(toResult(service.getRankingGift(serviceId)));
    }
    
    @PostMapping("/awards")
    public ResponseEntity<?> rankingAward(@RequestBody GameRankingAwardRequest request) {
        LOGGER.info("============ REQUEST =========== \n {}", JsonMapperUtils.toJson(request) );
        return response(toResult(service.rankingAward(request)));
    }

}
