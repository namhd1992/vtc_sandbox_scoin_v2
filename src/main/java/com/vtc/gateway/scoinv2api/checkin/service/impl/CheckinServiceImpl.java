/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.checkin.service.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vtc.common.dao.entity.CheckinHistory;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.CheckinHistoryRepository;
import com.vtc.common.exception.ScoinFailedToExecuteException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.utils.DateUtils;
import com.vtc.gateway.scoinv2api.checkin.service.CheckinService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jun 11, 2019
 */
@Service("checkinService")
public class CheckinServiceImpl extends
        AbstractService<CheckinHistory, Long, CheckinHistoryRepository> implements CheckinService {

    @Override
    public String checkin() {
        UserInfo userInfo = verifyAccessTokenUser();
        
        Optional<CheckinHistory> optCheckinHistory = repo.findByUserInfoAndCreateOnBetween(
                                          userInfo, 
                                          DateUtils.startDate(new Date()), 
                                          DateUtils.endDate(new Date()));
        if (optCheckinHistory.isPresent()) {
            throw new ScoinFailedToExecuteException("You are checked in today");
        }
        
        CheckinHistory checkinHistory = new CheckinHistory();
        checkinHistory.setUserInfo(userInfo);
        checkinHistory.setCreateOn(new Date());
        save(checkinHistory).orElseThrow(() -> {
            throw new ScoinNotFoundEntityException("Dont't Create checkin history");
        });
        return "Checkin Successful";
    }
    
    

}
