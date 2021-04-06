/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.game.service;

import java.util.List;
import java.util.Optional;

import com.vtc.common.dao.entity.Game;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 31, 2019
 */
public interface GameService extends AbstractInterfaceService<Game, Long> {
    
    List<Game> getAllGameActive() throws ScoinBusinessException;
    
    Optional<Game> getGameByServiceId(Long serviceId) throws ScoinBusinessException;
    
    List<Game> getGameByGameTags(List<Long> gameTagIds) throws ScoinBusinessException;
    
}
