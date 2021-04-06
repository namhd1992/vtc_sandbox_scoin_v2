/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.mission.service;

import java.util.List;

import com.vtc.common.dto.request.AbstractResquest;
import com.vtc.common.dto.response.MissionGetResponse;
import com.vtc.common.exception.ScoinBusinessException;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 17, 2019
 */
public interface MissionService {
    
    List<MissionGetResponse> getMissionActive(AbstractResquest request) throws ScoinBusinessException;

    public int countMissionActive();
    
    public String missionAction(Long missionId) throws ScoinBusinessException;
    
    public String finishMission(Long missionId) throws ScoinBusinessException;
}
