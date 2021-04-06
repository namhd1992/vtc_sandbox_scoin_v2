/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.mission;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtc.common.EnvironmentKey;
import com.vtc.common.constant.Constant;
import com.vtc.common.dao.entity.CheckinHistory;
import com.vtc.common.dao.entity.Game;
import com.vtc.common.dao.entity.LuckySpin;
import com.vtc.common.dao.entity.LuckySpinHistory;
import com.vtc.common.dao.entity.Mission;
import com.vtc.common.dao.entity.MissionUser;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.CheckinHistoryRepository;
import com.vtc.common.dao.repository.LuckySpinHistoryRepository;
import com.vtc.common.dao.repository.MissionUserRepository;
import com.vtc.common.dto.response.MissionGetResponse;
import com.vtc.common.dto.response.MissionProgressDetail;
import com.vtc.common.dto.response.ScoinAccountApiResponse;
import com.vtc.common.dto.response.ScoinLoginServiceHistoryResponse;
import com.vtc.common.dto.response.TopupCardHistoryResponse;
import com.vtc.common.exception.ScoinFailedToExecuteException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.service.CommonCardScoinService;
import com.vtc.common.utils.ApiExchangeServiceUtil;
import com.vtc.common.utils.DateUtils;
import com.vtc.gateway.scoinv2api.game.service.GameService;
import com.vtc.gateway.scoinv2api.luckySpin.service.LuckySpinService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 30, 2019
 */
@Component
public class MissionConditionUtils {
    
    protected Logger LOGGER = LoggerFactory.getLogger(MissionConditionUtils.class);
    
    @Autowired
    LuckySpinHistoryRepository spinHistoryRepo;

    @Autowired
    LuckySpinService           luckySpinService;

    @Autowired
    MissionUserRepository      missionUserRepo;

    @Autowired
    CheckinHistoryRepository   checkinHistoryRepo;

    @Autowired
    GameService                gameService;

    @Autowired
    CommonCardScoinService     commonCardScoinService;

    private String           SPLAY_API_SECRET;

    public MissionConditionUtils(Environment env) {
        SPLAY_API_SECRET = env.getProperty(EnvironmentKey.SPLAY_API_SECRET.getKey());
  }

 // Mission lucky spin
    public Boolean checkActionLuckySpin(Mission mission, MissionGetResponse missionResponse,
                                         UserInfo userInfo, List<MissionGetResponse> responses) {
        LuckySpin luckySpin = luckySpinService.get(mission.getObjectId()).orElseThrow(
                () -> new ScoinNotFoundEntityException("Not found Lucky Spin of this id"));
        
        // Check spin history
        if (mission.isCyclic()) { // Cyclic
          List<LuckySpinHistory> spinHistorys = spinHistoryRepo
                    .findByUserInfoAndLuckySpinAndCreateOnBetween(userInfo, luckySpin,
                            new Timestamp(DateUtils.startDate(new Date()).getTime()), 
                            new Timestamp(DateUtils.endDate(new Date()).getTime()));
            if (CollectionUtils.isEmpty(spinHistorys)) {
                responses.add(missionResponse);
                return false;
            }
            
        } else {
          List<LuckySpinHistory> spinHistorys = spinHistoryRepo
                    .findByUserInfoAndLuckySpinAndCreateOnBetween(userInfo, luckySpin,
                            new Timestamp(mission.getFromDate().getTime()),
                            new Timestamp(mission.getToDate().getTime()));
            if (CollectionUtils.isEmpty(spinHistorys)) {
                responses.add(missionResponse);
                return false;
            }
            
        }
        
        missionResponse.getMissionProgress().get(0).setIsFinish(true);
        MissionUser missionUser = new MissionUser(mission, userInfo,
                "Luckyspin-" + mission.getObjectId(), null);
        missionUserRepo.save(missionUser);
        missionResponse.setFinish(true);

        return true;
    }
    
    // Mission điểm danh
    public Boolean checkCheckinDayly(Mission mission, MissionGetResponse missionResponse,
                                     UserInfo userInfo, List<MissionGetResponse> responses) {
      
        List<CheckinHistory> checkinHistory = checkinHistoryRepo.findByUserInfoOrderByCreateOnDesc(userInfo);
        
        // Cyclic
        if (mission.isCyclic()) {
            // Get checkin history
            if (CollectionUtils.isEmpty(checkinHistory)
                    || !checkinHistory.get(0).getCreateOn().after(DateUtils.startDate(new Date()))
                    || !checkinHistory.get(0).getCreateOn().before(DateUtils.endDate(new Date()))) {
                responses.add(missionResponse);
                return false;
            }
            
            missionResponse.getMissionProgress().get(0).setIsFinish(true);
        } else {
            //get list date from date -> to date
            List<String> progressDates = new ArrayList<String>(DateUtils.toStringDaysBetweenTwoDay(
                    mission.getFromDate(), mission.getToDate(), DateUtils.DATE_MYSQL_FORMAT));
            List<MissionProgressDetail> progessDetails = new ArrayList<MissionProgressDetail>();
            
            int progressStt = 1;
            boolean isFinish = true;
            //compare date in list
            for (String progressDate : progressDates) {
                Date dateProgress = DateUtils.toDateFromStr(progressDate, DateUtils.DATE_MYSQL_FORMAT);
                // get login history of user in date
                Optional<CheckinHistory> optCheckinHistory = checkinHistoryRepo.findByUserInfoAndCreateOnBetween(
                              userInfo, 
                              DateUtils.startDate(dateProgress), 
                              DateUtils.endDate(dateProgress));
                MissionProgressDetail missionProgressDetail = new MissionProgressDetail(progressStt, true);
                if (!optCheckinHistory.isPresent()) {
                    missionProgressDetail.setIsFinish(false);
                    isFinish = false;
                }
                progessDetails.add(missionProgressDetail);
                progressStt++;
            }
            
            missionResponse.setMissionProgress(progessDetails);
            missionResponse.setMissionSatisfying(progressStt - 1);
            if (!isFinish) {
                responses.add(missionResponse);
                return false;
            }
            
        }

        MissionUser missionUser = new MissionUser(mission, 
                                                userInfo,
                                                "checkin", 
                                                null);
        missionUserRepo.save(missionUser);
        missionResponse.setFinish(true);
        return true;
    }
    
    // TODO : Condition Mission Auction
    
    // TODO : Condition Mission Gifcode
    
    // Condition login Game
    public Boolean checkLoginGame(Mission mission, MissionGetResponse missionResponse,
                                  UserInfo userInfo, List<MissionGetResponse> responses) {
        boolean tmp = false;
        Game game = gameService.getGameByServiceId(mission.getObjectId())
                .orElseThrow(() -> new ScoinNotFoundEntityException("Not found Game by this id"));
        missionResponse.setScoinGameId(game.getId());

        // Get login service from scoin
        String URL = Constant.URL_LOGIN_SERVICE_HISTORY 
                            + "api_key=" + SPLAY_API_SECRET
                            + "&accountId=" + userInfo.getUserVTC().getScoinId();
        ScoinAccountApiResponse<List<ScoinLoginServiceHistoryResponse>> scoinAccountApiResponse = ApiExchangeServiceUtil
                .get(URL,new TypeReference<ScoinAccountApiResponse<List<ScoinLoginServiceHistoryResponse>>>() {});

        if (!scoinAccountApiResponse.isStatus()) {
            throw new ScoinFailedToExecuteException("Don't CALL get info user from SCOIN");
        }
        List<ScoinLoginServiceHistoryResponse> loginHistorys = scoinAccountApiResponse.getData();
        
        if (mission.isCyclic()) { // Cyclic
            for (ScoinLoginServiceHistoryResponse loginHistory : loginHistorys) {
                if (loginHistory.getServiceId().equals(game.getScoinGameId())
                        && loginHistory.getLastLogin().after(DateUtils.startDate(new Date()))
                        && loginHistory.getLastLogin().before(DateUtils.endDate(new Date()))) {
                    tmp = true;
                }
            }
        } else {
            for (ScoinLoginServiceHistoryResponse loginHistory : loginHistorys) {
                if (loginHistory.getServiceId().equals(game.getScoinGameId())
                        && loginHistory.getLastLogin().after(mission.getFromDate())
                        && loginHistory.getLastLogin().before(mission.getToDate())) {
                    tmp = true;
                }
            }
        }

        if (!tmp) {
            responses.add(missionResponse);
            return false;
        }
        
        missionResponse.getMissionProgress().get(0).setIsFinish(true);
        MissionUser missionUser = new MissionUser(mission, userInfo,
                "loginservice-" + mission.getObjectId(), null);
        missionUserRepo.save(missionUser);
        missionResponse.setFinish(true);
        return true;
    }
    
    //Condition First login service
    public Boolean checkFirstLoginGame(Mission mission, MissionGetResponse missionResponse,
                                       UserInfo userInfo, List<MissionGetResponse> responses) {
        boolean tmp = false;
        Game game = gameService.getGameByServiceId(mission.getObjectId())
            .orElseThrow(() -> new ScoinNotFoundEntityException("Not found Game by this id"));
        missionResponse.setScoinGameId(game.getId());

        // Get login service from scoin
        String URL = Constant.URL_LOGIN_SERVICE_HISTORY 
                + "api_key=" + SPLAY_API_SECRET
                + "&accountId=" + userInfo.getUserVTC().getScoinId();
        ScoinAccountApiResponse<List<ScoinLoginServiceHistoryResponse>> scoinAccountApiResponse = ApiExchangeServiceUtil
                .get(URL,new TypeReference<ScoinAccountApiResponse<List<ScoinLoginServiceHistoryResponse>>>() {});

        if (!scoinAccountApiResponse.isStatus()) {
            throw new ScoinFailedToExecuteException("Don't CALL get info user from SCOIN");
        }
        List<ScoinLoginServiceHistoryResponse> loginHistorys = scoinAccountApiResponse.getData();
        
        for (ScoinLoginServiceHistoryResponse loginHistory : loginHistorys) {
            if (loginHistory.getServiceId().equals(game.getScoinGameId())
                    && loginHistory.getLastLogin().after(mission.getFromDate())
                    && loginHistory.getLastLogin().before(mission.getToDate())) {
                tmp = true;
            }
        }

        if (!tmp) {
            missionResponse.setCondition(false);
            responses.add(missionResponse);
            return false;
        }
        
        missionResponse.getMissionProgress().get(0).setIsFinish(true);
        MissionUser missionUser = new MissionUser(mission, userInfo,
                "loginservice-" + mission.getObjectId(), null);
        missionUserRepo.save(missionUser);
        missionResponse.setFinish(true);
        return true;
    }
    
    public Boolean topupCardLowValue(Mission mission, MissionGetResponse missionResponse,
                                         UserInfo userInfo, List<MissionGetResponse> responses) {
        // get history topup card
        String scoinToken = "k1547920971.1560496665.YwBMAGIALwBnADIAbQBpAFYAOABkAGkAbgBtAE0ANwBlAHgAMwAvAEcAdwA9AD0A";
        List<TopupCardHistoryResponse> topupCardHistorys = commonCardScoinService
                .getTopupCardHistory(scoinToken, new Date(), mission.getObjectId());
        if (CollectionUtils.isEmpty(topupCardHistorys)) {
            responses.add(missionResponse);
            return false;
        }
        
        boolean tmp = false;
        if (mission.isCyclic()) {
            for (TopupCardHistoryResponse topupCardHistory : topupCardHistorys) {
                if (topupCardHistory.getAmount() == Constant.CARD_VALUE_10000
                        || topupCardHistory.getAmount() == Constant.CARD_VALUE_20000) {
                    tmp = true;
                }
            }
            
            if (!tmp) {
                responses.add(missionResponse);
                return false;
            }
        }
        
        missionResponse.getMissionProgress().get(0).setIsFinish(true);
        MissionUser missionUser = new MissionUser(mission, userInfo,
                "TopupCardLowValue-" + mission.getObjectId(), null);
        missionUserRepo.save(missionUser);
        missionResponse.setFinish(true);
        return true;
    }

}
