/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.vtc.common.constant.Constant;
import com.vtc.common.constant.MessageConstant;
import com.vtc.common.dao.entity.FundsCardScoin;
import com.vtc.common.dao.entity.LuckyNumber;
import com.vtc.common.dao.entity.LuckySpin;
import com.vtc.common.dao.entity.LuckySpinBuyTurnHistory;
import com.vtc.common.dao.entity.LuckySpinHistory;
import com.vtc.common.dao.entity.LuckySpinItem;
import com.vtc.common.dao.entity.LuckySpinItemOfLuckySpin;
import com.vtc.common.dao.entity.LuckySpinPersonalTurnoverItem;
import com.vtc.common.dao.entity.LuckySpinPersonalTurnoverRadius;
import com.vtc.common.dao.entity.LuckySpinSetting;
import com.vtc.common.dao.entity.LuckySpinUser;
import com.vtc.common.dao.entity.TopupCardHistory;
import com.vtc.common.dao.entity.TransactionHistory;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.GiftcodeRepository;
import com.vtc.common.dao.repository.LuckyNumberRepository;
import com.vtc.common.dao.repository.LuckySpinBuyTurnHistoryRepository;
import com.vtc.common.dao.repository.LuckySpinGiftRepository;
import com.vtc.common.dao.repository.LuckySpinHistoryFakeRepository;
import com.vtc.common.dao.repository.LuckySpinHistoryRepository;
import com.vtc.common.dao.repository.LuckySpinItemOfLuckySpinRepository;
import com.vtc.common.dao.repository.LuckySpinItemOfUserRepository;
import com.vtc.common.dao.repository.LuckySpinItemRepository;
import com.vtc.common.dao.repository.LuckySpinPersonalTurnoverItemRepository;
import com.vtc.common.dao.repository.LuckySpinPersonalTurnoverRadiusRepository;
import com.vtc.common.dao.repository.LuckySpinRadiusPersonalTopupRepository;
import com.vtc.common.dao.repository.LuckySpinRepository;
import com.vtc.common.dao.repository.LuckySpinSettingRepository;
import com.vtc.common.dao.repository.LuckySpinUserRepository;
import com.vtc.common.dao.repository.TopupCardHistoryRepository;
import com.vtc.common.dto.request.LuckySpinGetRequest;
import com.vtc.common.dto.request.TransactionHistoryCreateRequest;
import com.vtc.common.dto.request.XuExchangeRequest;
import com.vtc.common.dto.response.LuckySpinDetailResponse;
import com.vtc.common.dto.response.UserTurnSpinDetailResponse;
import com.vtc.common.dto.response.UserXuInfoResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.exception.ScoinFailedToExecuteException;
import com.vtc.common.exception.ScoinInvalidDataRequestException;
import com.vtc.common.exception.ScoinNotEnoughtException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.exception.ScoinTimingStartAndEndException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.service.CommonCardScoinService;
import com.vtc.common.service.PaymentService;
import com.vtc.common.service.TransactionHistoryService;
import com.vtc.common.utils.DateUtils;
import com.vtc.common.utils.StringUtils;
import com.vtc.gateway.scoinv2api.luckySpin.ConditionTurnover;
import com.vtc.gateway.scoinv2api.luckySpin.RamdomItems;
import com.vtc.gateway.scoinv2api.luckySpin.service.LuckySpinService;
import com.vtc.gateway.scoinv2api.userInfo.service.UserInfoService;
import com.vtc.gateway.scoinv2api.userInfo.service.UserVTCService;

//import app.lib.vtcpay.BASE64Decoder;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 7, 2019
 */
@Service("luckySpinService")
public class LuckySpinServiceImpl extends AbstractService<LuckySpin, Long, LuckySpinRepository>
        implements LuckySpinService {
    
    @Autowired
    LuckySpinSettingRepository             luckySpinSettingRepo;

    @Autowired
    LuckySpinItemRepository                luckySpinItemRepo;

    @Autowired
    LuckySpinItemOfLuckySpinRepository     itemOfSpinRepo;

    @Autowired
    LuckySpinUserRepository                luckySpinUserRepo;

    @Autowired
    UserInfoService                        userInfoService;

    @Autowired
    LuckySpinHistoryRepository             spinHistoryRepo;

    @Autowired
    GiftcodeRepository                     giftcodeRepo;

    @Autowired
    TransactionHistoryService              transactionHistoryService;

    @Autowired
    UserVTCService                         userVTCService;

    @Autowired
    CommonCardScoinService                 cardScoinService;

    @Autowired
    LuckyNumberRepository                  luckyNumberRepo;

    @Autowired
    TopupCardHistoryRepository             topupCardHistoryRepo;

    @Autowired
    ConditionTurnover                      conditionTurnover;

    @Autowired
    PaymentService                         paymentService;

    @Autowired
    LuckySpinBuyTurnHistoryRepository      buyTurnHistoryRepo;

    @Autowired
    LuckySpinRadiusPersonalTopupRepository radiusPersonalTopupRepo;

    @Autowired
    LuckySpinItemOfUserRepository          luckySpinItemOfUserRepo;

    @Autowired
    LuckySpinGiftRepository                luckySpinGiftRepo;
    
    @Autowired
    LuckySpinPersonalTurnoverRadiusRepository luckySpinPersonalTurnoverRadiusRepo;
    
    @Autowired
    LuckySpinPersonalTurnoverItemRepository luckySpinPersonalTurnoverItemRepo;
    
    @Autowired
    LuckySpinHistoryFakeRepository luckySpinHistoryFakeRepository;
    
//    private String             TUDO_URL;
//    private String             TUDO_API_KEY;
//    private String             TUDO_API_SECRET;
//
//    public LuckySpinServiceImpl(Environment env) {
//        TUDO_URL = env.getProperty(EnvironmentKey.LIVE_TUDO_URL.getKey());
//        TUDO_API_KEY = env.getProperty(EnvironmentKey.LIVE_TUDO_API_KEY.getKey());
//        TUDO_API_SECRET = env.getProperty(EnvironmentKey.LIVE_TUDO_API_SECRET.getKey());
//    }
    
    @Override
    public List<LuckySpin> getLuckySpinActive(LuckySpinGetRequest request) {
        Pageable pageable = PageRequest.of(request.getOffset(), request.getLimit());
        return repo.findLuckySpin(request.getSpinId(),
                Constant.STATUS_ACTIVE, pageable);
    }

    @Override
    public int countLuckySpin(LuckySpinGetRequest request) {
        return repo.countLuckySpinGet(request.getSpinId(), Constant.STATUS_ACTIVE);
    }

    @Override
    public Optional<LuckySpinDetailResponse> getLuckySpinDetail(Long luckySpinId)
            throws ScoinBusinessException {
      
        UserInfo userInfo = verifyAccessTokenUser();
        
        if(ObjectUtils.isEmpty(luckySpinId)) {
            throw new ScoinInvalidDataRequestException();
        }
        
        //get LuckySpin
        Optional<LuckySpin> optLuckySpin = repo.findById(luckySpinId);
        LuckySpin luckySpin = optLuckySpin.orElseThrow(
                () -> new ScoinNotFoundEntityException("Not found LuckySpin by this ID"));
        
        //get all spin item
        List<LuckySpinItemOfLuckySpin> luckySpinItemOfLuckySpins = luckySpin.getSpinItems();
        if (CollectionUtils.isEmpty(luckySpinItemOfLuckySpins)) {
            throw new ScoinNotFoundEntityException("Not found LuckySpinItemOfLuckySpin by this LuckySpin");
        }
        
        // Get List LuckySpinItem by List LuckySpinItemOfLuckySpin
        List<LuckySpinItem> luckySpinItems = luckySpinItemOfLuckySpins.stream()
                .map(luckySpinItemOfLuckySpin -> luckySpinItemOfLuckySpin.getItem())
                .collect(Collectors.toList());
        
        UserTurnSpinDetailResponse userTurnSpinDetailResponse = new UserTurnSpinDetailResponse();
        
//        if (!ObjectUtils.isEmpty(userInfo)) {
            //update UserTurnSpin
//            LuckySpinUser luckySpinUser = updateUserTurnSpin(userInfo, luckySpin);
            
            //get user buy turn info
//            long totalTopupOfUser = 0;
//            UserBuyTurnResponse turnsBuyInfo = new UserBuyTurnResponse();
            
//            List<TopupCardHistory> TopupCardHistorys = topupCardHistoryRepo.
//                findByScoinIdAndAndPaymentTimeAfterAndPaymentTimeBefore(userInfo.getUserVTC().getScoinId(), 
//                                                            luckySpin.getStartDate(), 
//                                                            luckySpin.getEndDate());
//            if (!CollectionUtils.isEmpty(TopupCardHistorys)) {
//                for (TopupCardHistory TopupCardHistory : TopupCardHistorys) {
//                    totalTopupOfUser += TopupCardHistory.getTotalPayment();
//                }
//            }
            
//            turnsBuyInfo.setTotalTopupOfUser(totalTopupOfUser);
//            turnsBuyInfo.setAccumulationPoint(luckySpinUser.getBalance());
//            
//            if (luckySpinUser.getBalance() != 0) {
//                turnsBuyInfo.setCardBalanceRounding((luckySpin.getPricePerTurn() - luckySpinUser.getBalance()) / 2);
//            } else {
//                turnsBuyInfo.setCardBalanceRounding(luckySpin.getPricePerTurn() / 2);
//            }
//            
//            if (luckySpinUser.getBalance() != 0) {
//                turnsBuyInfo.setScoinBalanceRounding(luckySpin.getPricePerTurn() - luckySpinUser.getBalance());
//            } else {
//                turnsBuyInfo.setScoinBalanceRounding(luckySpin.getPricePerTurn());
//            }
            
            //get userTurnSpin response
            userTurnSpinDetailResponse.setUserId(userInfo.getId());
            userTurnSpinDetailResponse.setSpinId(luckySpin.getId());
//            userTurnSpinDetailResponse.setTurnsBuy(luckySpinUser.getTurnsBuy());
//            userTurnSpinDetailResponse.setTurnsFree(luckySpinUser.getTurnsFree());
            if (!StringUtils.isEmpty(userInfo.getFullName()))
                userTurnSpinDetailResponse.setCurrName(userInfo.getFullName());
            userTurnSpinDetailResponse.setCurrName(userInfo.getUserVTC().getUsername());
            userTurnSpinDetailResponse.setScoin(paymentService.updateBalanceSoin(userInfo));
            userTurnSpinDetailResponse.setXu(paymentService.getBalanceXu(userInfo.getUserVTC().getScoinId()).getTotalBalance());
//            userTurnSpinDetailResponse.setTurnsBuyInfo(turnsBuyInfo);
//        }
        
        LuckySpinDetailResponse response = new LuckySpinDetailResponse();
        response.setItemOfSpin(luckySpinItems);
        response.setLuckySpin(luckySpin);
        response.setSettings(null);
        response.setUserTurnSpin(userTurnSpinDetailResponse);
        
        return Optional.of(response);
    }
    
    @Override
    public LuckySpinItem luckySpinAward(Long luckySpinId) throws ScoinBusinessException {
        UserInfo userInfo = verifyAccessTokenUser();
        
//        if (StringUtils.isEmpty(userInfo.getPhoneNumber())) {
//            throw new ScoinUnverifiedAccountException("You don't verify phone number");
//        }
        
        if (ObjectUtils.isEmpty(luckySpinId)) {
            throw new ScoinInvalidDataRequestException();
        }
        
        //get LuckySpin
        Optional<LuckySpin> optLuckySpin = repo.findById(luckySpinId);
        LuckySpin luckySpin = optLuckySpin.orElseThrow(
                () -> new ScoinNotFoundEntityException("Not found LuckySpin by this ID"));
        
        if (luckySpin.getStartDate().after(new Date())
                || luckySpin.getEndDate().before(new Date())) {
            throw new ScoinTimingStartAndEndException();
        }

        // get User Turn Spin and update turn enough
        LuckySpinUser luckySpinUser = updateUserTurnSpin(userInfo, luckySpin);
        String typeOfTurn = null;
        
        //check enought condition to play turn
//        if (luckySpinUser.getTurnsBuy() < 1 
//                && luckySpinUser.getTurnsFree() < 1) {
//            throw new ScoinNotEnoughtException(MessageConstant.SPIN_TURNS_NOT_ENOUGHT);
//        }
        if (luckySpin.getBuyTurnType().equals(Constant.LUCKYSPIN_BUY_TURN_XU)) {
            if (paymentService.getBalanceXu(userInfo.getUserVTC().getScoinId())
                    .getTotalBalance() < luckySpin.getPricePerTurn()) {
                throw new ScoinNotEnoughtException(MessageConstant.SPIN_TURNS_NOT_ENOUGHT);
            }
            
            luckySpinExchangeXu(userInfo, luckySpin.getPricePerTurn(), luckySpin.getName(), 
                Constant.XU_DEDUCT, "spinId: " + luckySpinUser.getLuckySpin().getId());
        }

        // Make ratio item to group user
        
        List<RamdomItems> ratioItems = makeRatioItemToUser(luckySpin, luckySpinUser.getPersonalTopup(), userInfo);
        
        ratioItems.forEach(ratioItem -> {
            LOGGER.info("Item ======= : {}", ratioItem.getSpinItemId() + ", ratio =====: " + ratioItem.getRadioItem());
        });

        // skip item if quantity = 0
        LuckySpinItemOfLuckySpin resultItem = null;
        while (resultItem == null) {
            long positionRamdom = ramdomInList(ratioItems);
            if (positionRamdom == -1)
                continue;
            resultItem = itemOfSpinRepo.findById(positionRamdom)
                    .orElseThrow(() -> new ScoinNotFoundEntityException("Not found spin item by this id ramdom"));
            
            LuckySpinItem item = resultItem.getItem();

            long totalTurnover = 0;
            LuckySpinSetting turnoverSetting = luckySpinSettingRepo.
                    findByKeyNameAndStatus(Constant.LUCKYSPIN_TURNOVER_KEYNAME_TOTAL, Constant.STATUS_ACTIVE);
            if (!ObjectUtils.isEmpty(turnoverSetting))
                totalTurnover = turnoverSetting.getIntValue();

                boolean passItem = conditionTurnover.
                            checkPickItemFolowTurnover(luckySpin, totalTurnover, item);
                if (!passItem) {
                    LOGGER.info("PASS ITEM : ============== : {}" + item.getType() + " , Value = " + item.getValue());
                    resultItem = null;
                    continue;
                }
                
            // verify user folow topup card
//            if (item.getType().equals(Constant.LUCKYSPIN_GIFT_SCOIN_CARD)
//                    && item.getValue() >= 50000) {
//                boolean passItemScoinCard = conditionTurnover.checkPickUserFolowTopupCard(userInfo, item);
//                if (!passItemScoinCard) {
//                    LOGGER.info("PASS USER : ============== : {}" + item.getType() 
//                                                            + " , Value = " + item.getValue() 
//                                                            + "User Name : " + userInfo.getFullName());
//                    resultItem = null;
//                    continue;
//                }
//            }
        }
            
        LOGGER.info("ITEM TYPE KQ ================ {}", resultItem.getItem().getType());

        // Create spin transaction
        LuckySpinHistory spinHistory = new LuckySpinHistory();
        spinHistory.setUserInfo(luckySpinUser.getUserInfo());
        spinHistory.setLuckySpin(luckySpinUser.getLuckySpin());
        spinHistory.setItem(resultItem.getItem());
        spinHistory.setStatus(Constant.STATUS_RECIEVED);
        spinHistory.setTurnType(typeOfTurn);
            
        switch (resultItem.getItem().getType()) {
        // Handle item ACTION 
        case Constant.LUCKYSPIN_GIFT_ACTION:
            if (resultItem.getItem().getKeyName().equals("themluot")) {
                luckySpinUser.setTurnsFree(luckySpinUser.getTurnsFree() + 1);
                luckySpinUserRepo.save(luckySpinUser);
                spinHistory.setMessage("Bạn đã được thêm 1 lượt");
            } else {
                spinHistory.setMessage("Chúc bạn may mắn lần sau");
            }

            spinHistory.setValue(0);
            spinHistory.setDescription(resultItem.getItem().getName());
            luckySpin.setSpinTimes(luckySpinUser.getLuckySpin().getSpinTimes() + 1);
            save(luckySpin);
            break;
            
        // Handle item XU
        case Constant.LUCKYSPIN_GIFT_XU:
            awardIsXu(resultItem, luckySpin, userInfo, luckySpinUser, spinHistory);
            break;
        
        case Constant.LUCKYSPIN_GIFT_SCOIN:
            int multiplier = 0;
            if (resultItem.getItem().getValue() > 1000000) {
                multiplier = (int) resultItem.getItem().getValue() / 1000000;
                for (int i = 0; i < multiplier; i++) {
                    cardScoinService.topupScoin(1000000, userInfo.getUserVTC().getUsername());
                }
            } else {
                cardScoinService.topupScoin(resultItem.getItem().getValue(), userInfo.getUserVTC().getUsername());
            }
            
            spinHistory.setValue(resultItem.getItem().getValue());
            spinHistory.setMessage(resultItem.getItem().getWinningTitle());
            spinHistory.setDescription(resultItem.getItem().getType());
            break;
            
        // Handle item SCOIN CARD
//        case Constant.LUCKYSPIN_GIFT_SCOIN_CARD:
//            awardIsScoinCard(luckySpin, 
//                    userInfo, 
//                    resultItem,
//                    spinHistory);
//            break;
            
        case Constant.LUCKYSPIN_GIFT_REALITY:
          spinHistory.setValue(resultItem.getItem().getValue());
          spinHistory.setMessage(resultItem.getItem().getWinningTitle());
          spinHistory.setDescription(resultItem.getItem().getType());
          break;
        }
        
        // user lost turn spin
        luckySpinUser.setTurnsFree(luckySpinUser.getTurnsFree() - 1);
        luckySpinUser = luckySpinUserRepo.save(luckySpinUser);
        if (ObjectUtils.isEmpty(luckySpinUser)) {
            throw new ScoinFailedToExecuteException("Failed to update User Turn Spin");
        }
        
        LuckySpinHistory history = spinHistoryRepo.save(spinHistory);
        if (ObjectUtils.isEmpty(history)) {
            throw new ScoinFailedToExecuteException("Can't create Spin History");
        }

        // update quantity Spin_item
        resultItem.setReceivedQuantity(resultItem.getReceivedQuantity() + 1);
        if (resultItem.getTotalQuantity() > 0) 
            resultItem.setTotalQuantity(resultItem.getTotalQuantity() - 1);
        itemOfSpinRepo.save(resultItem);

        // Update luckySpinTime
        luckySpin.setSpinTimes(luckySpin.getSpinTimes() + 1);
        save(luckySpin);

        return resultItem.getItem();

    }
    
   
    
    @Override
    public UserXuInfoResponse getBalanceXu(Long scoinId) throws ScoinBusinessException {
        return paymentService.getBalanceXu(scoinId);
    }

    @Override
    public List<String> getLuckyNumbers(long cardValue, int quantity) throws Exception {
        if (cardValue < 1 || quantity < 1)
            throw new ScoinInvalidDataRequestException();
        List<String> responses = new ArrayList<String>();
        for (int i = 0; i < quantity ; i++) {
            FundsCardScoin fundsCardScoin = cardScoinService.buyCard(String.valueOf(cardValue), 1);
            String response = "Mã Thẻ : " + fundsCardScoin.getMainCodeCard()
                        + ", Seri : " + fundsCardScoin.getSeriCard()
                        + ", HSD : " + DateUtils.toStringFormDate(fundsCardScoin.getExpirationDateCard(), DateUtils.DATE_TIME_MYSQL_FORMAT);
            responses.add(response);
        }
        
        return responses;
    }

    @Override
    public String createLuckyNumber(int digitNumber) throws ScoinBusinessException {
        String maxNumberString = "";
        for (int i = 0; i < digitNumber; i++) {
            maxNumberString += "9";
        }
        
        int maxNum = Integer.parseInt(maxNumberString);
//        List<LuckyNumber> luckyNumbers = new ArrayList<LuckyNumber>();
        
        List<Integer> luckyCodes = new ArrayList<Integer>();
        for (int i = 0; i <= maxNum; i++) {
          luckyCodes.add(i);
        }
        
        Collections.shuffle(luckyCodes);
        
        String formatLuckyCode = "%0" + digitNumber + "d";
        for (Integer luckyCode : luckyCodes){
          LuckyNumber luckyNumber = new LuckyNumber();
          luckyNumber.setLuckyCode(String.format(formatLuckyCode, luckyCode));
          luckyNumber.setUsed(false);
          luckyNumberRepo.save(luckyNumber);
        }
        return "Success";
    }
    
    //===================== COMPONENT =============================
    
//    private synchronized LuckyNumber addLuckyNumber(LuckyNumber luckyNumber, UserInfo userInfo) {
//        luckyNumber.setScoinId(userInfo.getUserVTC().getScoinId());
//        luckyNumber.setUsed(true);
//        luckyNumber = luckyNumberRepo.save(luckyNumber);
//        return luckyNumber;
//    }
    
//    private void addItemTuDoScoin(Long scoinId, int itemId, String itemName, String giftDetail) {
//        String description = "Chúc mừng bạn đã sở hữu " + itemName 
//                + ". Chi tiết : " + giftDetail;
//        
//        Long time = new Timestamp(System.currentTimeMillis()).getTime();
//        String sign = StringUtils.toMD5(TUDO_API_KEY
//                                + TUDO_API_SECRET
//                                + scoinId.toString()
//                                + String.valueOf(itemId)
//                                + time.toString());
//        AddItemTuDoRequest addItemTuDoRequest = new AddItemTuDoRequest();
//        addItemTuDoRequest.setApiKey(TUDO_API_KEY);
//        addItemTuDoRequest.setScoinId(scoinId);
//        addItemTuDoRequest.setItemId(itemId);
//        addItemTuDoRequest.setDescription(description);
//        addItemTuDoRequest.setTime(time);
//        addItemTuDoRequest.setSign(sign);
//        
//        CallApiScoinBaseResponse<String> response = ApiExchangeServiceUtil
//                .post(TUDO_URL, addItemTuDoRequest, new TypeReference<CallApiScoinBaseResponse<String>>() {});
//        
//        if (response.getError_code() < 0) {
//            throw new ScoinUnknownErrorException(response.getError_code().toString(),
//                    response.getError_desc());
//        }
//        
//    }
    
    private long ramdomInList(List<RamdomItems> randomItems) {
        int tmp = ThreadLocalRandom.current().nextInt(1000);
        
        List<Long> spinTurnIds = new ArrayList<Long>();
        randomItems.forEach(randomItem -> {
            int totalItemFollowRatio = Math.round((float) randomItem.getRadioItem() * 10);
            long spinItemId = randomItem.getSpinItemId();
            for (int i = 0; i < totalItemFollowRatio; i++) {
                spinTurnIds.add(spinItemId);
            }
        });
        Collections.shuffle(spinTurnIds);
        
      return spinTurnIds.get(tmp);
  }
    
    private LuckySpinUser updateUserTurnSpin(UserInfo userInfo, LuckySpin luckySpin) {
        //process turn spin
        int turnOfUser = 0;
        long topupCardValue = 0;
        long topupScoinValue = 0;
        long balance = 0;
        
        // get LuckySpinUser
        LuckySpinUser luckySpinUser = new LuckySpinUser();
        Optional<LuckySpinUser> optLuckySpinUser = luckySpinUserRepo
                .findByUserInfoAndLuckySpin(userInfo, luckySpin);
        
        if (!optLuckySpinUser.isPresent()) {
            luckySpinUser.setLuckySpin(luckySpin);
            luckySpinUser.setUserInfo(userInfo);
        } else {
            luckySpinUser = optLuckySpinUser.get();
        }
        
        //Update turn for user
        List<TopupCardHistory> topupHistorysOfUser = topupCardHistoryRepo.
                findByLuckyWheelUsedIsFalseAndScoinIdAndPaymentTimeBetween(userInfo.getUserVTC().getScoinId(), 
                                                                        luckySpin.getStartDate(), 
                                                                        luckySpin.getEndDate());
                                                                        
        if (!CollectionUtils.isEmpty(topupHistorysOfUser)) {
            for (TopupCardHistory topupHistoryOfUser : topupHistorysOfUser) {
                if (topupHistoryOfUser.getPaymentType().equals(Constant.LUCKYSPIN_TOPUP_TYPE_CARD)) {
                    topupCardValue += topupHistoryOfUser.getTotalPayment();
                } else if (topupHistoryOfUser.getPaymentType().equals(Constant.LUCKYSPIN_TOPUP_TYPE_SCOIN)) {
                    topupScoinValue += topupHistoryOfUser.getTotalPayment();
                }
                
                topupHistoryOfUser.setLuckyWheelUsed(true);
                topupCardHistoryRepo.save(topupHistoryOfUser);
            }
            
            balance = (topupCardValue * 2) + topupScoinValue + luckySpinUser.getBalance();
            
            
            if (balance >= luckySpin.getPricePerTurn()
                    && (luckySpin.getBuyTurnType().equals(Constant.LUCKYSPIN_TOPUP_TYPE_ALL)
                            || luckySpin.getBuyTurnType().equals(Constant.LUCKYSPIN_TOPUP_TYPE_CARD))) {
                turnOfUser = (int) (balance / luckySpin.getPricePerTurn());
                balance = balance - (turnOfUser * luckySpin.getPricePerTurn());
                
                LuckySpinBuyTurnHistory buyTurnHistory = new LuckySpinBuyTurnHistory();
                buyTurnHistory.setUserInfo(userInfo);
                buyTurnHistory.setLuckySpin(luckySpin);
                buyTurnHistory.setBuyValue(turnOfUser * luckySpin.getPricePerTurn());
                buyTurnHistory.setBuyType(luckySpin.getBuyTurnType());
                buyTurnHistory.setSpinTurn(turnOfUser);
                buyTurnHistory.setBuyQuantity(1);
                buyTurnHistory.setStatus(Constant.STATUS_SUCCESS);
                buyTurnHistoryRepo.save(buyTurnHistory);
                
            }
            
            luckySpinUser.setBalance(balance);
            luckySpinUser.setTurnsFree(luckySpinUser.getTurnsFree() + turnOfUser);
            luckySpinUser.setPersonalTopup(luckySpinUser.getPersonalTopup() + topupCardValue + topupScoinValue);
            luckySpinUser.setAccumulationPoint(luckySpinUser.getAccumulationPoint() + (topupCardValue * 2) + topupScoinValue);
        }
        
        luckySpinUser = luckySpinUserRepo.save(luckySpinUser);
        if (ObjectUtils.isEmpty(luckySpinUser)) {
            throw new ScoinFailedToExecuteException("Don't create new user turn spin");
        }
        
        return luckySpinUser;
    }
    
    private List<RamdomItems> makeRatioItemToUser(LuckySpin luckySpin, long personalTopup, UserInfo userInfo){
        List<RamdomItems> ratioItems = new ArrayList<RamdomItems>();
        int totalIndexItem = 0;
        boolean haveActionItem = false;
        
        List<LuckySpinItemOfLuckySpin> itemOfluckySpins = luckySpin.getSpinItems();
        if (CollectionUtils.isEmpty(itemOfluckySpins)) {
            throw new ScoinNotFoundEntityException("Not found item of this lucky spin");
        }

        //get big item
        List<LuckySpinItemOfLuckySpin> bigItems = itemOfluckySpins.stream()
                .filter(item -> item.getItem().isBigItem())
                .collect(Collectors.toList());

        // sort big item
        bigItems.sort((i1, i2) -> Long.valueOf(i1.getItem().getValue())
                .compareTo(Long.valueOf(i2.getItem().getValue())));
        
        // choose ratio of item for group user
        if (!CollectionUtils.isEmpty(bigItems)
                && personalTopup >= bigItems.get(0).getItem().getValue()*5) {
            
            //get landmark topup personal of this user
            long personalLandmark = conditionTurnover.
                    pickRatioItemFolowPersonalTurnover(personalTopup, bigItems);
            
            // item of landmark topup personal of this user
            LuckySpinItemOfLuckySpin luckySpinBigItem = bigItems.stream()
                    .filter(item -> item.getItem().getValue()*5 == personalLandmark).findFirst()
                    .get();
            
            LOGGER.info("luckySpinBigItem.getItem().getValue() ================== {}", luckySpinBigItem.getItem().getValue());

            // get personal turnover item
            LuckySpinPersonalTurnoverItem luckySpinPersonalTurnoverItem = luckySpinPersonalTurnoverItemRepo.
                                                        findBySpinItem(luckySpinBigItem);
            
            if (ObjectUtils.isEmpty(luckySpinPersonalTurnoverItem)) {
                throw new ScoinNotFoundEntityException("Not fount Personal Turnover Item");
            }
            
            int bigItemLuckySpinHistorie = 0;
            int bigItemLuckySpinHistoryFake = 0;
            if (luckySpinPersonalTurnoverItem.isMainItem()) {
                // count mainItem in history in this day
                bigItemLuckySpinHistorie = spinHistoryRepo
                        .countByItemAndCreateOnBetween(luckySpinPersonalTurnoverItem.getSpinItem().getItem(),
                                                          DateUtils.startDate(new Date()), 
                                                          DateUtils.endDate(new Date()));
                
                // count mainItem in history fake in this day
                bigItemLuckySpinHistoryFake = luckySpinHistoryFakeRepository.
                        countByLuckySpinIdAndItemValueAndCreateOnBetween(luckySpin.getId(), 
                                          luckySpinPersonalTurnoverItem.getSpinItem().getItem().getValue(), 
                                          DateUtils.startDate(new Date()), 
                                          DateUtils.endDate(new Date()));
                }
            
            // count mainItem recied of user
            int itemThisUserRecied = spinHistoryRepo.
                    countByUserInfoAndItem(userInfo, luckySpinPersonalTurnoverItem.getSpinItem().getItem());

            // if quantity mainItem < quantity can recied and mainItem don't have in this day and fake history
            if (itemThisUserRecied < luckySpinPersonalTurnoverItem.getLimitPerUser()
                    && bigItemLuckySpinHistorie < 1
                    && bigItemLuckySpinHistoryFake < 1) {
                List<LuckySpinPersonalTurnoverRadius> personalTurnoversRadius = luckySpinPersonalTurnoverRadiusRepo.findAll();
                
                if (ObjectUtils.isEmpty(personalTurnoversRadius)) {
                    throw new ScoinNotFoundEntityException("Not fount Personal Turnover other Item");
                }
                    
                // add to items random
                for (LuckySpinPersonalTurnoverRadius personalTurnoverRadius : personalTurnoversRadius) {
                    totalIndexItem += personalTurnoverRadius.getRatioIndex();
                    if (personalTurnoverRadius.getRatioIndex() <= 0) continue;
                    ratioItems.add(new RamdomItems(personalTurnoverRadius.getSpinItem().getId(),
                            personalTurnoverRadius.getRatioIndex(),
                            personalTurnoverRadius.getSpinItem().getItem().getType()));
                }
                
             // check is lucky spin has item
                if (totalIndexItem < 1 && haveActionItem == true) {
                    throw new ScoinNotFoundEntityException("Not found items off this lucky spin");
                }
                
                ratioItems.add(new RamdomItems(luckySpinPersonalTurnoverItem.getSpinItem().getId(),
                        (double) 100 - totalIndexItem,
                        luckySpinPersonalTurnoverItem.getSpinItem().getItem().getType()));
                LOGGER.info("personalLandmark ============== : {}", personalLandmark);
                return ratioItems;
            }
            
        } 
        // User have ratio basic
        //total ratio index and check is lucky spin has item
        for (LuckySpinItemOfLuckySpin itemOfUser : luckySpin.getSpinItems()) {
            totalIndexItem += itemOfUser.getRatioIndex();
            if (itemOfUser.getItem().getType().equals(Constant.LUCKYSPIN_GIFT_ACTION))
                haveActionItem = true;
        }
            
        // check is lucky spin has item
        if (totalIndexItem < 1 && haveActionItem == true) {
            throw new ScoinNotFoundEntityException("Not found items off this lucky spin");
        }
        
        // add to items random
        for (LuckySpinItemOfLuckySpin itemOfUser : luckySpin.getSpinItems()) {
            if (itemOfUser.getRatioIndex() > 0
                    || itemOfUser.getItem().getType().equals(Constant.LUCKYSPIN_GIFT_ACTION)) {
                ratioItems.add(new RamdomItems(itemOfUser.getId(),
                        (double) 100 / totalIndexItem * itemOfUser.getRatioIndex(),
                        itemOfUser.getItem().getType()));
            }
        }
        
        return ratioItems;
    }
    
    private UserXuInfoResponse luckySpinExchangeXu(UserInfo userInfo, 
                                                   long amount, 
                                                   String luckySpinName, 
                                                   String typeExchange,
                                                   String dataRequest) {
        String content = createDescription(userInfo, luckySpinName, amount);
        
        //create Transaction History
        TransactionHistoryCreateRequest historyCreateRequest = new TransactionHistoryCreateRequest();
        historyCreateRequest.setUserInfo(userInfo);
        historyCreateRequest.setAmount(amount);
        historyCreateRequest.setDataRequest(dataRequest);
        historyCreateRequest.setBalanceBefore(userInfo.getUserVTC().getXu());
        historyCreateRequest.setBalanceAfter(userInfo.getUserVTC().getXu());
        historyCreateRequest.setServiceType(Constant.SERVICE_TYPE_DEDUCT);
        historyCreateRequest.setSourceType(Constant.SOURCE_TYPE_LUCKYSPIN);
        TransactionHistory trans = 
        transactionHistoryService.createTransactionHistory(historyCreateRequest);
        
        //TOPUP xu
        XuExchangeRequest xuExchangeRequest = new XuExchangeRequest();
        xuExchangeRequest.setScoin_id(userInfo.getUserVTC().getScoinId());
        xuExchangeRequest.setAmount(amount);
        xuExchangeRequest.setTransid(trans.getId());
        xuExchangeRequest.setContent(content);
        UserXuInfoResponse userXuInfo = 
                paymentService.exchangeXu(xuExchangeRequest, typeExchange);
        
        //update balance xu user
        userInfo.getUserVTC().setXu(userXuInfo.getTotalBalance());
        userVTCService.save(userInfo.getUserVTC());
        
        //set success to transaction
        historyCreateRequest.setBalanceAfter(userInfo.getUserVTC().getXu());
        historyCreateRequest.setStatus(Constant.STATUS_SUCCESS);
        transactionHistoryService.save(trans);
        return  userXuInfo;
    }
    
    private String createDescription(UserInfo userInfo, String luckySpinName, long amount) {
        return "Scoin ID : " + userInfo.getUserVTC().getScoinId()
                + ", UserName : " + userInfo.getUserVTC().getUsername()
                + ", requirement : " + "Topup nhận thưởng luckySpin : " + luckySpinName
                + ", Date : " + DateUtils.toStringFormDate(new Date(), DateUtils.DATE_TIME_MYSQL_FORMAT) 
                + ", Amount : " + amount;
    }
    
    private void awardIsXu(LuckySpinItemOfLuckySpin resultItem, 
                           LuckySpin luckySpin, 
                           UserInfo userInfo, 
                           LuckySpinUser luckySpinUser, 
                           LuckySpinHistory spinHistory) {
        long itemValue = resultItem.getItem().getValue();
        
        //TOPUP xu when user win item xu
        luckySpinExchangeXu(userInfo, itemValue, luckySpin.getName(), 
                Constant.XU_TOPUP, "spinId: " + luckySpinUser.getLuckySpin().getId());
        String message = resultItem.getItem().getWinningTitle() + " " + itemValue + " COIN";
        spinHistory.setDescription(resultItem.getItem().getType());
        spinHistory.setValue(itemValue);
        spinHistory.setMessage(message);
    }
    
//    private void awardIsJackpot(LuckySpinItem resultItem, LuckySpin luckySpin, UserInfo userInfo,
//                                UserTurnSpin userTurnSpin, LuckySpinHistory spinHistory,
//                                long amountJackpotWinner) {
//        spinHistory.setValue(amountJackpotWinner);
//        spinHistory.setMessage(resultItem.getItem().getWinningTitle() + " " + amountJackpotWinner + "SP");
//        
//        //TOPUP xu when win JACKPOT
//        String requirement ="Cộng xu nhận thưởng JACKPOT luckyspin : " + luckySpin.getName();
//        luckySpinExchangeXu(userInfo, 
//            amountJackpotWinner, 
//            requirement, 
//            Constant.XU_DEDUCT, 
//            "spinId: " + userTurnSpin.getLuckySpin().getId());
//    }
    
//    private void awardIsGiftcode(LuckySpinItem resultItem, UserInfo userInfo, LuckySpinHistory spinHistory) {
//        if (resultItem.getQuantity() < 1) throw new ScoinNotEnoughtException("Not enought gift");
//        List<Giftcode> giftcodes = giftcodeRepo
//                .findByItemSpinAndUserLostIsNullAndDeviceIDIsNull(resultItem.getItem());
//        if (CollectionUtils.isEmpty(giftcodes)) {
//            throw new ScoinNotEnoughtException("Not enought giftcode");
//        }
//        
//        Giftcode gc = giftcodes.get(0);
//        gc.setUserLost(userInfo);
//        if (!ObjectUtils.isEmpty(userInfo.getDeviceId())) 
//            gc.setDeviceID(userInfo.getDeviceId());
//        giftcodeRepo.save(gc);
//        
//        addItemTuDoScoin(userInfo.getUserVTC().getScoinId(),
//                Constant.SCOIN_TUDO_ITEM_TYPE_GIFTCODE, 
//                resultItem.getItem().getName(), 
//                gc.getMainCode());
//        
//        spinHistory.setValue(0);
//        spinHistory.setDescription(gc.getMainCode());
//        spinHistory.setMessage(resultItem.getItem().getWinningTitle() + " " + gc.getMainCode());
//
//    }

//    private void awardIsLuckyNumber(LuckySpin luckySpin, UserInfo userInfo,LuckyNumber luckyNumber, LuckySpinItem resultItem, LuckySpinHistory spinHistory) {
////        addItemTuDoScoin(userInfo.getUserVTC().getScoinId(),
////                Constant.SCOIN_TUDO_ITEM_TYPE_LUCKY_NUMBER, 
////                resultItem.getItem().getName(), 
////                luckyNumber.getLuckyCode());
//        
//        spinHistory.setDescription(luckyNumber.getLuckyCode());
//        spinHistory.setValue(Math.round(resultItem.getItem().getValue()));
//        spinHistory.setMessage(resultItem.getItem().getWinningTitle() +
//                " - Mã giải(" + spinHistory.getDescription()+
//                ") - "+resultItem.getLuckySpin().getId());
//        
//        LuckySpinSetting luckySpinSetting = new LuckySpinSetting();
//        luckySpinSetting = luckySpinSettingRepo.findByLuckySpinAndKeyNameAndStatus(luckySpin, resultItem.getItem().getType(), Constant.STATUS_ACTIVE);
//        addLuckyNumber(luckyNumber, userInfo);
//        if(ObjectUtils.isEmpty(luckySpinSetting)) {
//          luckySpinSetting = new LuckySpinSetting();
//          luckySpinSetting.setLuckySpin(luckySpin);
//          luckySpinSetting.setName("Lucky Number result");
//          luckySpinSetting.setKeyName(resultItem.getItem().getType());
//          luckySpinSetting.setType("item_result");
//          luckySpinSetting.setIntValue(1);
//          luckySpinSetting.setStatus(Constant.STATUS_ACTIVE);
//        } else {
//            luckySpinSetting.setIntValue(luckySpinSetting.getIntValue() + 1);
//        }
//        
//        luckySpinSettingRepo.save(luckySpinSetting);
//        
//    }
    
//    private void awardIsScoinCard(LuckySpin luckySpin, UserInfo userInfo, LuckySpinItem resultItem, LuckySpinHistory spinHistory) {
//        String valueCard = String.valueOf(resultItem.getItem().getValue());
//        FundsCardScoin fundsCardScoin = cardScoinService.buyCard(valueCard, 1);
//       
//        String scoinCardInfo = "Mã thẻ : " + fundsCardScoin.getMainCodeCard()
//                    + " - Seri : " + fundsCardScoin.getSeriCard()
//                    + " - HSD : " + DateUtils.toStringFormDate(fundsCardScoin.getExpirationDateCard(), DateUtils.DATE_DEFAULT_FORMAT);
//        
//        addItemTuDoScoin(userInfo.getUserVTC().getScoinId(),
//                Constant.SCOIN_TUDO_ITEM_TYPE_SCOIN_CARD, resultItem.getItem().getName(), scoinCardInfo);
//        spinHistory.setValue(Math.round(resultItem.getItem().getValue()));
//        spinHistory.setDescription(scoinCardInfo);
//        spinHistory.setMessage(resultItem.getItem().getWinningTitle() + " - Mã giải ( "
//                + spinHistory.getDescription() + " ) - " + resultItem.getLuckySpin().getId());
//
//        fundsCardScoin.setRecipientUser(userInfo.getUserVTC().getScoinId());
//        
//        cardScoinService.save(fundsCardScoin);
//        LuckySpinSetting luckySpinSetting = new LuckySpinSetting();
//        luckySpinSetting = luckySpinSettingRepo.
//                findByLuckySpinAndKeyNameAndStatus(luckySpin, resultItem.getItem().getType() + "_" + String.valueOf(resultItem.getItem().getValue()), Constant.STATUS_ACTIVE);
//        if(ObjectUtils.isEmpty(luckySpinSetting)) {
//          luckySpinSetting = new LuckySpinSetting();
//          luckySpinSetting.setLuckySpin(luckySpin);
//          luckySpinSetting.setName("Scoin Card result");
//          luckySpinSetting.setKeyName(resultItem.getItem().getType() + "_" + String.valueOf(resultItem.getItem().getValue()));
//          luckySpinSetting.setType("item_result");
//          luckySpinSetting.setIntValue(1);
//          luckySpinSetting.setStatus(Constant.STATUS_ACTIVE);
//        } else {
//            luckySpinSetting.setIntValue(luckySpinSetting.getIntValue() + 1);
//        }
//       
//        luckySpinSettingRepo.save(luckySpinSetting);
//    }

    @Override
    public List<LuckySpin> findLuckySpin(Long spinId, String status, Pageable pageable) {
        return repo.findLuckySpin(spinId, status, pageable);
    }

}
