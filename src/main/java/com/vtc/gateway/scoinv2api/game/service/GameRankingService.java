/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.game.service;

import com.vtc.common.dao.entity.GameRanking;
import com.vtc.common.dto.request.GameRankingAwardRequest;
import com.vtc.common.dto.response.GameRankingGetResponse;
import com.vtc.common.dto.response.GameRankingGiftResponse;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 12, 2020
 */
public interface GameRankingService extends AbstractInterfaceService<GameRanking, Long> {
    
    GameRankingGetResponse getGameRanking(Long serviceId, String week);
    
    GameRankingGiftResponse getRankingGift(Long serviceId);
    
    String rankingAward(GameRankingAwardRequest request);

}
