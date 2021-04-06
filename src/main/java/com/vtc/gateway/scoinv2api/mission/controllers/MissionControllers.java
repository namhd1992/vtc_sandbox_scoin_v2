/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.mission.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vtc.common.AbstractController;
import com.vtc.common.dto.request.AbstractResquest;
import com.vtc.common.utils.JsonMapperUtils;
import com.vtc.gateway.scoinv2api.mission.service.MissionService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 17, 2019
 */
@RestController
@RequestMapping("/mission")
public class MissionControllers extends AbstractController<MissionService> {
    
    @GetMapping("/active")
    public ResponseEntity<?> getMissionActive(AbstractResquest request) {
        LOGGER.info("============ REQUEST =========== \n {}", JsonMapperUtils.toJson(request));
        return response(toResult(service.getMissionActive(request), service.countMissionActive()));
    }
    
    @GetMapping("/action")
    public ResponseEntity<?> missionAction(@RequestParam("mission_id") Long missionId) {
        LOGGER.info("============ REQUEST =========== \n {}", JsonMapperUtils.toJson(missionId));
        return response(toResult(service.missionAction(missionId), service.countMissionActive()));
    }
    
    @GetMapping("/finish")
    public ResponseEntity<?> missionFinish(@RequestParam("mission_id") Long missionId) {
        LOGGER.info("============ REQUEST =========== \n {}", JsonMapperUtils.toJson(missionId));
        return response(toResult(service.finishMission(missionId)));
    }
    
}
