/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.mission.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtc.common.EnvironmentKey;
import com.vtc.common.constant.Constant;
import com.vtc.common.dao.entity.Giftcode;
import com.vtc.common.dao.entity.LuckySpin;
import com.vtc.common.dao.entity.LuckySpinUser;
import com.vtc.common.dao.entity.Mission;
import com.vtc.common.dao.entity.MissionUser;
import com.vtc.common.dao.entity.TransactionHistory;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.GiftcodeRepository;
import com.vtc.common.dao.repository.LuckySpinUserRepository;
import com.vtc.common.dao.repository.MissionRepository;
import com.vtc.common.dao.repository.MissionUserRepository;
import com.vtc.common.dto.request.AbstractResquest;
import com.vtc.common.dto.request.AddItemTuDoRequest;
import com.vtc.common.dto.request.LuckySpinGetRequest;
import com.vtc.common.dto.request.TransactionHistoryCreateRequest;
import com.vtc.common.dto.request.XuExchangeRequest;
import com.vtc.common.dto.response.CallApiScoinBaseResponse;
import com.vtc.common.dto.response.MissionGetResponse;
import com.vtc.common.dto.response.MissionProgressDetail;
import com.vtc.common.dto.response.UserXuInfoResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.exception.ScoinDuplicateEntityException;
import com.vtc.common.exception.ScoinFailedToExecuteException;
import com.vtc.common.exception.ScoinInvalidDataRequestException;
import com.vtc.common.exception.ScoinNotEnoughtException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.exception.ScoinUnknownErrorException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.service.PaymentService;
import com.vtc.common.service.TransactionHistoryService;
import com.vtc.common.utils.ApiExchangeServiceUtil;
import com.vtc.common.utils.DateUtils;
import com.vtc.common.utils.StringUtils;
import com.vtc.gateway.scoinv2api.luckySpin.service.LuckySpinService;
import com.vtc.gateway.scoinv2api.mission.MissionConditionUtils;
import com.vtc.gateway.scoinv2api.mission.service.MissionService;
import com.vtc.gateway.scoinv2api.userInfo.service.UserVTCService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 17, 2019
 */
@Service("missionService")
public class MissionServiceImpl extends AbstractService<Mission, Long, MissionRepository>
        implements MissionService {
    
    @Autowired
    MissionUserRepository missionUserRepo;
    
    @Autowired
    MissionConditionUtils condition;
    
    @Autowired
    PaymentService       exchangeCoinService;

    @Autowired
    TransactionHistoryService transactionHistoryService;

    @Autowired
    UserVTCService            userVTCService;

    @Autowired
    LuckySpinService          luckySpinService;

    @Autowired
    LuckySpinUserRepository    luckySpinUserRepo;
    
    @Autowired
    GiftcodeRepository giftcodeRepository;
    
    private String            TUDO_URL;

    private String            TUDO_API_KEY;

    private String            TUDO_API_SECRET;

    public MissionServiceImpl(Environment env) {
        TUDO_URL = env.getProperty(EnvironmentKey.SANDBOX_TUDO_URL.getKey());
        TUDO_API_KEY = env.getProperty(EnvironmentKey.SANDBOX_TUDO_API_KEY.getKey());
        TUDO_API_SECRET = env.getProperty(EnvironmentKey.SANDBOX_TUDO_API_SECRET.getKey());
    }

    @Override
    public List<MissionGetResponse> getMissionActive(AbstractResquest request) {
        UserInfo userInfo = verifyAccessTokenUser();
        
        Pageable pageable = PageRequest.of(request.getOffset(), request.getLimit());
        List<Mission> missionList = repo.findByFromDateBeforeAndToDateAfterAndStatus(
                                      DateUtils.addDate(new Date(), 1),
                                      DateUtils.addDate(new Date(), -4), 
                                      Constant.STATUS_ACTIVE, pageable);
        
        List<MissionGetResponse> responses = new ArrayList<>();
        for (Mission mission : missionList) {
            if (new Date().getTime() >= mission.getToDate().getTime()
                && mission.getStatus().equals("active")) {
                mission.setStatus("inactive");
              repo.save(mission);
            }
              // Check mission finish
            boolean isFinish = true;
            boolean isReceived = true;
            Date startDate = DateUtils.startDate(new Date());
            Date endDate = DateUtils.endDate(new Date());
            if (!mission.isCyclic()) {
                startDate = mission.getFromDate();
                endDate = mission.getToDate();
            }
            Optional<MissionUser> optMissionUser = missionUserRepo.findByMissionAndUserInfoAndCreateOnBetween
                                                                                    (mission,
                                                                                    userInfo, 
                                                                                    startDate, 
                                                                                    endDate);
            if (optMissionUser.isEmpty()) {
                isFinish = false;
                isReceived = false;
            } else if (!optMissionUser.get().isReceived()) {
                isFinish = true;
                isReceived = false;
            }
            
            //create Response
            MissionGetResponse missionResponse = toMissionGetResponse(mission, isFinish, isReceived);            
            
            // check limit reward of mision
            if (!StringUtils.isEmpty(mission.getTypeLimit())
                    && !ObjectUtils.isEmpty(mission.getValueLimit())) {
                Long awardAvailable = getGiftReceived(mission);
                if (awardAvailable >= 0)
                    missionResponse.setAwardAvailable(awardAvailable);
            }

            // Condition of mission
            if (!missionResponse.isFinish()) {

                switch (mission.getAction()) {
                  // Action luckyspin (1)
                case Constant.ACTION_LUCKY_SPIN:
                    Boolean flag1 = condition.checkActionLuckySpin(mission, 
                                                          missionResponse,
                                                          userInfo, 
                                                          responses);
                    if (flag1 == false) continue;
                    break;

                  // action điểm danh (2)
                case Constant.ACTION_CHECKIN_DAYLY:
                    Boolean flag2 = condition.checkCheckinDayly(mission, 
                                                        missionResponse, 
                                                        userInfo,
                                                        responses);
                    if (flag2 == false) continue;
                    break;

                  // Login game (3)
                  case Constant.ACTION_LOGIN_GAME:
                      Boolean flag5 = condition.checkLoginGame(mission, 
                                                       missionResponse, 
                                                       userInfo, 
                                                       responses);
                      if (flag5 == false) continue;
                      break;
                      
                // Check first login in game(5)
                case Constant.ACTION_FIRST_LOGIN_GAME:
                        Boolean flag9 = condition.checkFirstLoginGame(mission, 
                                                              missionResponse,
                                                              userInfo, 
                                                              responses);
                    if (flag9 == false) continue;
                    break;
                
                // Topup card gift turn spin (5)
                case Constant.ACTION_TOPUP_CARD :
                        Boolean flag10 = condition.topupCardLowValue(mission, 
                                                                missionResponse,
                                                                userInfo, 
                                                                responses);
                        if (flag10 == false) continue;
                    break;
                      
//                  case Constant.ACTION_LOGIN_GAME_CONTINUOUS:
//                      Boolean flag10 = condition.loginGameContinuous(mission, missionResponse, userInfo, responses);
//                      if (flag10 == false)
//                          continue;
                // break;
                }
            } else {
                missionResponse.getMissionProgress().forEach(progress -> progress.setIsFinish(true));
            }
            responses.add(missionResponse);
        }
        Collections.reverse(responses);
        return responses;
    }
    
    @Override
    public int countMissionActive() {
        return repo.countByFromDateBeforeAndToDateAfterAndStatus(
                DateUtils.addDate(new Date(), 1),
                DateUtils.addDate(new Date(), -4),
                Constant.STATUS_ACTIVE);
    }
    
    @Override
    public String missionAction(Long missionId) throws ScoinBusinessException {
        UserInfo userInfo = verifyAccessTokenUser();
        if (ObjectUtils.isEmpty(missionId)) {
            throw new ScoinInvalidDataRequestException();
        }
        
        String respomseMessage = "";
        Mission mission = get(missionId).orElseThrow(
                () -> new ScoinNotFoundEntityException("Not found mission by this id"));
        
        switch (mission.getAction()) {
        case Constant.ACTION_SHARE_LINK_FACEBOOK :
            Optional<MissionUser> otpMissionUser = missionUserRepo.
                    findByMissionAndUserInfoAndCreateOnBetween(mission, userInfo, mission.getFromDate(), mission.getToDate());
            if(otpMissionUser.isPresent()) return "đã thực hiện rồi";
            
            MissionUser missionUser = new MissionUser(mission, userInfo,
                "ShareLinkToFacebook-" + mission.getId(), null);
            missionUserRepo.save(missionUser);
            respomseMessage = "Share link to Facebook successful";
        }
        
        return respomseMessage;
    }
    
    @Override
    public String finishMission(Long missionId) {
        UserInfo userInfo = verifyAccessTokenUser();
        
        if (ObjectUtils.isEmpty(missionId)) throw new ScoinInvalidDataRequestException();
        
        Mission mission = get(missionId).orElseThrow(
                () -> new ScoinNotFoundEntityException("Not found mission by this id"));
        MissionUser missionUser = missionUserRepo.findByMissionAndUserInfoAndCreateOnBetween(mission,
                                                                                    userInfo, 
                                                                                    DateUtils.startDate(new Date()), 
                                                                                    DateUtils.endDate(new Date()))
                .orElseThrow(() -> new ScoinNotFoundEntityException("You not completed the Mission"));
        
        if(missionUser.isReceived()){
            throw new ScoinDuplicateEntityException("You received gift");
        }
        
        //Check limited gift of day or event
        if (!checkLimitedAward(mission)) {
            throw new ScoinNotEnoughtException("Gift of Mission don't enough");
        }
        
        switch (mission.getAward()) {
        case Constant.MISSION_AWARD_XU:
            // TOPUP xu when finish mission
            missionTopupXu(userInfo, mission.getName(), mission.getValueAward());
            break;
            
        case Constant.MISSION_AWARD_TURN_SPIN:
            //Add turn spin when finish mission
            List<LuckySpin> luckySpins = luckySpinService
                    .getLuckySpinActive(new LuckySpinGetRequest(mission.getSpinAwardId()));
            if (CollectionUtils.isEmpty(luckySpins)) {
                throw new ScoinNotFoundEntityException("Not found Lucky Spin to gift turn");
            }
            LuckySpin luckySpin = luckySpins.get(0);
            
            //add turn
            LuckySpinUser luckySpinUser = new LuckySpinUser();
            Optional<LuckySpinUser> optUserTurnSpin = luckySpinUserRepo
                    .findByUserInfoAndLuckySpin(userInfo, luckySpin);
            if (!optUserTurnSpin.isPresent()) {
                luckySpinUser.setUserInfo(userInfo);
                luckySpinUser.setLuckySpin(luckySpin);
//                luckySpinUser.setTurnsFree(luckySpin.getFreeSpinOnStart() + mission.getValueAward());
            } else {
                luckySpinUser = optUserTurnSpin.get();
//                luckySpinUser.setTurnsFree(luckySpinUser.getTurnsFree() + mission.getValueAward());
            }
            
            luckySpinUser = luckySpinUserRepo.save(luckySpinUser);
            if (ObjectUtils.isEmpty(luckySpinUser)) {
                throw new ScoinFailedToExecuteException("Don't update or create User Turn Spin");
            }
            
            break;
            
        case Constant.MISSION_AWARD_GIFTCODE:
            List<Giftcode> giftcodes = giftcodeRepository
                    .findByUserInfoIsNullAndEventTypeAndMainEventIdAndSubEventIdOrderById(
                            Constant.GIFTCODE_EVENT_TYPE_MISSION, missionId, null);
            if (CollectionUtils.isEmpty(giftcodes)) {
                throw new ScoinNotFoundEntityException("Not found giftcode");
            }
            Giftcode giftcode = giftcodes.get(0);
            
            addItemTuDoScoin(userInfo.getUserVTC().getScoinId(),
                    Constant.SCOIN_TUDO_ITEM_TYPE_GIFTCODE, mission.getName(), giftcode.getMainCode());
            
            giftcode.setUserInfo(userInfo);
            giftcodeRepository.save(giftcode);
            break;
        }
        
        missionUser.setReceived(true);
        missionUserRepo.save(missionUser);
        return "Reward success";
    }
    
    //============================== FUNCTION COMPONENT ==============================================
    
    private MissionGetResponse toMissionGetResponse(Mission mission, boolean isFinish, boolean isReceived) {
        MissionGetResponse missionResponse = new MissionGetResponse();
        missionResponse.setMissionId(mission.getId());
        missionResponse.setMissionName(mission.getName());
        missionResponse.setDescription(mission.getDescription());
        missionResponse.setActionId(mission.getAction().toString());
        missionResponse.setLinkToShare(mission.getLinkToShare());
        missionResponse.setValueAward(mission.getValueAward());
        missionResponse.setFromDate(mission.getFromDate());
        missionResponse.setToDate(mission.getToDate());
        missionResponse.setCyclic(mission.isCyclic());
        missionResponse.setFinish(isFinish);
        missionResponse.setReceived(isReceived);
        if (!ObjectUtils.isEmpty(mission.getObjectId()))
            missionResponse.setObjectId(mission.getObjectId());
//        missionResponse.setAndroidScheme(mission.getAndroidScheme());
//        missionResponse.setIosScheme(mission.getIosScheme());
        missionResponse.setObjectValue(mission.getObjectValue());
        missionResponse.setAward(mission.getAward());
        missionResponse.setMissionStatus(mission.getStatus());
        missionResponse.setMissionSatisfying(Constant.MISSION_SATISFYING_DEFAULT);
        List<MissionProgressDetail> missionProgressDetails = new ArrayList<MissionProgressDetail>();
        missionProgressDetails.add(new MissionProgressDetail(Constant.MISSION_PROGRESS_DEFAULT, false));
        missionResponse.setMissionProgress(missionProgressDetails);
        missionResponse.setHighLights(mission.isHighLights());
        
        if (mission.getAward().equals(Constant.MISSION_AWARD_TURN_SPIN)) {
            List<LuckySpin> luckySpins = luckySpinService
                    .getLuckySpinActive(new LuckySpinGetRequest(mission.getSpinAwardId()));
            if (CollectionUtils.isEmpty(luckySpins)) {
                throw new ScoinNotFoundEntityException("Not found Lucky Spin to gift turn");
            }

            missionResponse.setSpinAwardName(luckySpins.get(0).getName());
        }
        return missionResponse;
    }
    
    public Long getGiftReceived(Mission mission) {
        Long giftReceired = (long) 0;
        long numberUserReceired = missionUserRepo.countUserReceivedByDate(mission, null);

        if (numberUserReceired != 0) {
            if (mission.getTypeLimit().equals(Constant.EVENT)) {
                giftReceired = numberUserReceired * mission.getValueAward();
            } else if (mission.getTypeLimit().equals(Constant.DAY)) {
                Date dayOfToDay = DateUtils.toDateChangeFormatFromDate(new Date(), DateUtils.DATE_MYSQL_FORMAT);
                giftReceired = missionUserRepo.countUserReceivedByDate(mission, dayOfToDay) * mission.getValueAward();
            }
            
            return (mission.getValueLimit() - giftReceired);
        }
        
        return (long) -1;
    }
    
    private boolean checkLimitedAward(Mission mission) {
        if (!ObjectUtils.isEmpty(mission.getValueLimit()) && mission.getValueLimit() != 0) {
            Long giftReceired = (long) 0;
            long numberUserReceired = missionUserRepo.countUserReceivedByDate(mission, null);

            if (numberUserReceired != 0) {
                if (mission.getTypeLimit().equals(Constant.EVENT)) {
                    giftReceired = (numberUserReceired + 1) * mission.getValueAward();
                } else if (mission.getTypeLimit().equals(Constant.DAY)) {
                    Date dayOfToDay = DateUtils.toDateChangeFormatFromDate(new Date(), DateUtils.DATE_MYSQL_FORMAT);
                    numberUserReceired = missionUserRepo.countUserReceivedByDate(mission, dayOfToDay);
                    giftReceired = (numberUserReceired + 1) * mission.getValueAward();
                }

                if (giftReceired > mission.getValueLimit())
                    return false;
            }
        }

        return true;
    }
    
    private UserXuInfoResponse missionTopupXu(UserInfo userInfo, String missionName, long amount) {
        String content = "Scoin ID : " + userInfo.getUserVTC().getScoinId()
                + ", UserName : " + userInfo.getUserVTC().getUsername()
                + ", requirement : " + "Cộng xu trả thưởng Nhiệm vụ - Mission " + missionName
                + ", Date : " + DateUtils.toStringFormDate(new Date(), DateUtils.DATE_TIME_MYSQL_FORMAT) 
                + ", Amount : " + amount;
        
        //create Transaction History
        TransactionHistoryCreateRequest historyCreateRequest = new TransactionHistoryCreateRequest();
        historyCreateRequest.setUserInfo(userInfo);
        historyCreateRequest.setAmount(amount);
        historyCreateRequest.setDataRequest(content);
        historyCreateRequest.setBalanceBefore(userInfo.getUserVTC().getXu());
        historyCreateRequest.setBalanceAfter(userInfo.getUserVTC().getXu());
        historyCreateRequest.setServiceType(Constant.SOURCE_TYPE_WINNING_SP);
        historyCreateRequest.setSourceType(Constant.SOURCE_FINSH_MISSION);
        TransactionHistory trans = 
        transactionHistoryService.createTransactionHistory(historyCreateRequest);
        
        //TOPUP xu
        XuExchangeRequest xuExchangeRequest = new XuExchangeRequest();
        xuExchangeRequest.setScoin_id(userInfo.getUserVTC().getScoinId());
        xuExchangeRequest.setAmount(amount);
        xuExchangeRequest.setTransid(trans.getId());
        xuExchangeRequest.setContent(content);
        UserXuInfoResponse userXuInfo = 
                exchangeCoinService.exchangeXu(xuExchangeRequest, Constant.XU_TOPUP);
        
        //update balance xu user
        userInfo.getUserVTC().setXu(userXuInfo.getTotalBalance());
        userVTCService.save(userInfo.getUserVTC());
        
        //set success to transaction
        historyCreateRequest.setBalanceAfter(userInfo.getUserVTC().getXu());
        historyCreateRequest.setStatus(Constant.STATUS_SUCCESS);
        transactionHistoryService.save(trans);
        return  userXuInfo;
    }
    
    private void addItemTuDoScoin(Long scoinId, int itemId, String itemName, String giftDetail) {
      String description = "Chúc mừng bạn đã sở hữu " + itemName 
              + ". Chi tiết : " + giftDetail;
      
      Long time = new Timestamp(System.currentTimeMillis()).getTime();
      String sign = StringUtils.toMD5(TUDO_API_KEY
                              + TUDO_API_SECRET
                              + scoinId.toString()
                              + String.valueOf(itemId)
                              + time.toString());
      AddItemTuDoRequest addItemTuDoRequest = new AddItemTuDoRequest();
      addItemTuDoRequest.setApiKey(TUDO_API_KEY);
      addItemTuDoRequest.setScoinId(scoinId);
      addItemTuDoRequest.setItemId(itemId);
      addItemTuDoRequest.setDescription(description);
      addItemTuDoRequest.setTime(time);
      addItemTuDoRequest.setSign(sign);
      
      CallApiScoinBaseResponse<String> response = ApiExchangeServiceUtil
              .post(TUDO_URL, addItemTuDoRequest, new TypeReference<CallApiScoinBaseResponse<String>>() {});
      
      if (response.getError_code() < 0) {
          throw new ScoinUnknownErrorException(response.getError_code().toString(),
                  response.getError_desc());
      }
      
  }

}
