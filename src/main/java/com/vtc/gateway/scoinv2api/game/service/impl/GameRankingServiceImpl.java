/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.game.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtc.common.EnvironmentKey;
import com.vtc.common.constant.Constant;
import com.vtc.common.dao.entity.FundsCardScoin;
import com.vtc.common.dao.entity.GameRanking;
import com.vtc.common.dao.entity.GameRankingHistory;
import com.vtc.common.dao.entity.GameRankingItem;
import com.vtc.common.dao.entity.GameRankingRank;
import com.vtc.common.dao.entity.Giftcode;
import com.vtc.common.dao.entity.TransactionHistory;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.repository.GameRankingHistoryRepository;
import com.vtc.common.dao.repository.GameRankingItemRepository;
import com.vtc.common.dao.repository.GameRankingRepository;
import com.vtc.common.dao.repository.GiftcodeRepository;
import com.vtc.common.dto.request.AddItemTuDoRequest;
import com.vtc.common.dto.request.GameRankingAwardRequest;
import com.vtc.common.dto.request.TransactionHistoryCreateRequest;
import com.vtc.common.dto.request.XuExchangeRequest;
import com.vtc.common.dto.response.BasePhongMaChienResponse;
import com.vtc.common.dto.response.CallApiScoinBaseResponse;
import com.vtc.common.dto.response.GameRankItemResponse;
import com.vtc.common.dto.response.GameRankingGetResponse;
import com.vtc.common.dto.response.GameRankingGiftResponse;
import com.vtc.common.dto.response.GameRankingRankGetResponse;
import com.vtc.common.dto.response.GameRankingUserGetResponse;
import com.vtc.common.dto.response.ServerGamePhongMaChienResponseData;
import com.vtc.common.dto.response.TopTopupPhongmachienResponse;
import com.vtc.common.dto.response.UserInfoPhongMaKiemGetResponse;
import com.vtc.common.dto.response.UserXuInfoResponse;
import com.vtc.common.exception.ScoinDuplicateEntityException;
import com.vtc.common.exception.ScoinFailedToExecuteException;
import com.vtc.common.exception.ScoinInvalidDataRequestException;
import com.vtc.common.exception.ScoinNotEnoughtException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.exception.ScoinUnknownErrorException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.service.CommonCardScoinService;
import com.vtc.common.service.PaymentService;
import com.vtc.common.service.TransactionHistoryService;
import com.vtc.common.utils.ApiExchangeServiceUtil;
import com.vtc.common.utils.DateUtils;
import com.vtc.common.utils.StringUtils;
import com.vtc.gateway.scoinv2api.game.service.GameRankingService;
import com.vtc.gateway.scoinv2api.userInfo.service.UserVTCService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 12, 2020
 */
@Service("rangkingGameService")
public class GameRankingServiceImpl extends AbstractService<GameRanking, Long, GameRankingRepository>
        implements GameRankingService {
    
    @Autowired
    GameRankingHistoryRepository gameRankingHistoryRepo;

    @Autowired
    GameRankingItemRepository    gameRankingItemRepo;

    @Autowired
    GiftcodeRepository           giftcodeRepo;

    @Autowired
    TransactionHistoryService    transactionHistoryService;

    @Autowired
    PaymentService               paymentService;

    @Autowired
    UserVTCService               userVTCService;

    @Autowired
    CommonCardScoinService       cardScoinService;
    
    private String             TUDO_URL;
    private String             TUDO_API_KEY;
    private String             TUDO_API_SECRET;
  
    public GameRankingServiceImpl(Environment env) {
        TUDO_URL = env.getProperty(EnvironmentKey.SANDBOX_TUDO_URL.getKey());
        TUDO_API_KEY = env.getProperty(EnvironmentKey.SANDBOX_TUDO_API_KEY.getKey());
        TUDO_API_SECRET = env.getProperty(EnvironmentKey.SANDBOX_TUDO_API_SECRET.getKey());
    }

    @Override
    public GameRankingGetResponse getGameRanking(Long serviceId, String week) {
        if (ObjectUtils.isEmpty(serviceId))
            throw new ScoinInvalidDataRequestException();
        
        GameRanking gameRanking = repo.findByServiceId(serviceId);
        if (ObjectUtils.isEmpty(gameRanking)) {
            throw new ScoinNotFoundEntityException("This game don't have Ranking");
        }
        
        GameRankingGetResponse response = new GameRankingGetResponse();
        List<GameRankingUserGetResponse> userResponses = new ArrayList<GameRankingUserGetResponse>();
        List<GameRankingRank> rankingRanks = new ArrayList<GameRankingRank>();
        
        //sort rank
        gameRanking.setGameRankingRanks(gameRanking.getGameRankingRanks().stream()
                .sorted((o1, o2) -> Integer.valueOf(o1.getRankPosition())
                        .compareTo(Integer.valueOf(o2.getRankPosition())))
                .collect(Collectors.toList()));
        
        gameRanking.getGameRankingRanks().forEach(rank -> {
            for (int i = 0; i < rank.getRankQuantity(); i++) {
                rankingRanks.add(rank);
            }
        });
        
        //filter game by service id
        if (gameRanking.getServiceId() == Constant.SERVICE_GAME_ID_PHONG_MA_CHIEN) {
          //get all server phong ma chien
            BasePhongMaChienResponse<ServerGamePhongMaChienResponseData> serverInfos = ApiExchangeServiceUtil.
                    get("http://api.phongmachien.vn/game.php/Api/other/get_api_data?action=getServerList", 
                            new TypeReference<BasePhongMaChienResponse<ServerGamePhongMaChienResponseData>>() {});
            
            //get top topup phong ma chien
            String UrlTopInfo = "http://api.phongmachien.vn/game.php/Api/other/get_api_data?action=getListOfRecharge&top=10&startdate=1586476800&enddate=1587168000";
            BasePhongMaChienResponse<TopTopupPhongmachienResponse> topTopupGame = ApiExchangeServiceUtil.
                    get(UrlTopInfo, new TypeReference<BasePhongMaChienResponse<TopTopupPhongmachienResponse>>() {});
            
            // response to top topup
            // show ranking in game
            int size = 1;
            for (TopTopupPhongmachienResponse user : topTopupGame.getContent()) {
                GameRankingUserGetResponse userResponse = new GameRankingUserGetResponse();
                String server = serverInfos.getContent().stream()
                        .filter(serverInfo -> serverInfo.getServerid() == user.getServerid())
                        .collect(Collectors.toList()).get(0).getServername();
                userResponse.setUserName(user.getRolename());
                userResponse.setServer(server);
                userResponse.setScoinTopup((long) user.getTotalpayment());
                userResponse.setPosition(user.getPosition());
                if (user.getPosition() <= rankingRanks.size()) {
                    userResponse.setRankName(rankingRanks.get(user.getPosition() - 1).getRankName());
                    userResponse.setRankIconUrl(rankingRanks.get(user.getPosition() - 1).getRankIconUrl());
                }
               
                userResponses.add(userResponse);
                if (size > 20) break;
            }
            
            //==================== FAKE DATA ====================
            
            while (size < 20) {
                for (TopTopupPhongmachienResponse user : topTopupGame.getContent()) {
                    GameRankingUserGetResponse userResponse = new GameRankingUserGetResponse();
                    String server = serverInfos.getContent().stream()
                            .filter(serverInfo -> serverInfo.getServerid() == user.getServerid())
                            .collect(Collectors.toList()).get(0).getServername();
                    userResponse.setUserName(user.getRolename());
                    userResponse.setServer(server);
                    userResponse.setScoinTopup((long) user.getTotalpayment());
                    userResponse.setPosition(user.getPosition());
                    if (user.getPosition() <= rankingRanks.size()) {
                        userResponse.setRankName(rankingRanks.get(user.getPosition() - 1).getRankName());
                        userResponse.setRankIconUrl(rankingRanks.get(user.getPosition() - 1).getRankIconUrl());
                    }
                   
                    userResponses.add(userResponse);
                    size++;
                }
            }
            
            //==================== FAKE DATA ====================
            
            long MyScoinId = 1400000139;
            
            //get my profile ingame phong ma chien
            BasePhongMaChienResponse<UserInfoPhongMaKiemGetResponse> userInfoGames = ApiExchangeServiceUtil.get(
                    "http://api.phongmachien.vn/game.php/Api/other/webrecharge?action=getServerRole&accountid=" + MyScoinId,
                    new TypeReference<BasePhongMaChienResponse<UserInfoPhongMaKiemGetResponse>>() {});
            
            //response my profile ingame phong ma chien
            if (!CollectionUtils.isEmpty(userInfoGames.getContent())) {
                userInfoGames.getContent().forEach(userInfoGame -> {
                    List<TopTopupPhongmachienResponse> myAccounts = topTopupGame.getContent().stream()
                            .filter(userTop -> userTop.getRoleid() == userInfoGame.getRoleid())
                            .collect(Collectors.toList());
                    
                    if(!CollectionUtils.isEmpty(myAccounts)) {
                        response.setMyGameName(myAccounts.get(0).getRolename());
                        response.setMyGameServer(serverInfos.getContent().stream()
                            .filter(serverInfo -> serverInfo.getServerid() == myAccounts.get(0).getServerid())
                            .collect(Collectors.toList()).get(0).getServername());
                        response.setMyPosition(myAccounts.get(0).getPosition());
                        if (myAccounts.get(0).getPosition() <=  rankingRanks.size()) {
                            response.setMyRankName(rankingRanks.get(myAccounts.get(0).getPosition() - 1).getRankName());
                            response.setMyRankIconUrl(rankingRanks.get(myAccounts.get(0).getPosition() - 1).getRankIconUrl());
                        }
                    }
                });
                
                if (StringUtils.isEmpty(response.getMyGameName()))
                    response.setMyGameName(userInfoGames.getContent().get(userInfoGames.getContent().size() - 1).getRolename());
                if (StringUtils.isEmpty(response.getMyGameServer()))
                    response.setMyGameServer(serverInfos.getContent().stream()
                            .filter(serverInfo -> serverInfo.getServerid() == userInfoGames.getContent().get(userInfoGames.getContent().size() - 1).getServerid())
                            .collect(Collectors.toList()).get(0).getServername());
            }
        }
        
        response.setGameRankingName(gameRanking.getName());
        response.setGameRule(gameRanking.getDescription());
        response.setUsers(userResponses);
        
       
        
//        Calendar cal = Calendar.getInstance();
//        Date startDate = new Date();
//        Date endDate = new Date();
//        
        if (StringUtils.isEmpty(week)) {
//            cal.add(Calendar.WEEK_OF_MONTH, -2);
//            cal.set(Calendar.DAY_OF_WEEK, 2);
//            startDate = DateUtils.startDate(cal.getTime());
//            
//            cal = Calendar.getInstance();
//            cal.add(Calendar.WEEK_OF_MONTH, -1);
//            cal.set(Calendar.DAY_OF_WEEK, 1);
//            endDate = DateUtils.endDate(cal.getTime());
            
            
        } else if (week.equals(Constant.GAME_RANKING_THIS_WEEK)) {
//            cal.add(Calendar.WEEK_OF_MONTH, -1);
//            cal.set(Calendar.DAY_OF_WEEK, 2);
//            startDate = DateUtils.startDate(cal.getTime());
        } else if (week.equals(Constant.GAME_RANKING_WEEK_BEFORE_LAST)) {
//            cal.add(Calendar.WEEK_OF_MONTH, -3);
//            cal.set(Calendar.DAY_OF_WEEK, 2);
//            startDate = DateUtils.startDate(cal.getTime());
            
//            cal = Calendar.getInstance();
//            cal.add(Calendar.WEEK_OF_MONTH, -2);
//            cal.set(Calendar.DAY_OF_WEEK, 1);
//            endDate = DateUtils.endDate(cal.getTime());
        } else {
            throw new ScoinInvalidDataRequestException();
        }
        
        return response;
    }

    @Override
    public GameRankingGiftResponse getRankingGift(Long serviceId) {
        UserInfo userInfo = verifyUserSanbox();
        long MyScoinId = 1400000139;
        GameRanking gameRanking = repo.findByServiceId(serviceId);
        if (ObjectUtils.isEmpty(gameRanking)) {
            throw new ScoinNotFoundEntityException("This game don't have Ranking");
        }
        GameRankingGiftResponse response = new GameRankingGiftResponse();
        List<GameRankingRankGetResponse> rankResponses = new ArrayList<GameRankingRankGetResponse>();
        
        Calendar cal = Calendar.getInstance();
        Date startDate = new Date();
        Date endDate = new Date();

        cal.set(Calendar.DAY_OF_WEEK, 2);
        startDate = DateUtils.startDate(cal.getTime());

        cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        endDate = DateUtils.endDate(cal.getTime());
        response.setEndDateReceivedGift(DateUtils.endDate(endDate));
        
        
        gameRanking.setGameRankingRanks(gameRanking.getGameRankingRanks().stream()
                .sorted((o1, o2) -> Integer.valueOf(o1.getRankPosition())
                        .compareTo(Integer.valueOf(o2.getRankPosition())))
                .collect(Collectors.toList()));
        
        long userRankId = getMyRankIdMaPhongKiem(gameRanking, MyScoinId);
        
        for(GameRankingRank rank : gameRanking.getGameRankingRanks()) {
            GameRankingRankGetResponse gameRankingRankGetResponse = new GameRankingRankGetResponse();
            if (rank.getId() == userRankId) {
                gameRankingRankGetResponse.setMyRank(true);
            } else {
                gameRankingRankGetResponse.setMyRank(false);
            }
            
            List<GameRankItemResponse> itemResponses = new ArrayList<GameRankItemResponse>();
            for (GameRankingItem item : rank.getGameRankingItems()) {
                GameRankItemResponse itemResponse = new GameRankItemResponse();
                itemResponse.setItemId(item.getId());
                itemResponse.setItemName(item.getItemName());
                itemResponse.setItemIconUrl(item.getItemIconUrl());
                itemResponse.setItemType(item.getItemType());
                itemResponse.setQuantity(item.getItemQuantity());
                itemResponse.setContent(item.getDescription());
                itemResponse.setValue(item.getItemValue());
                
                if (StringUtils.isEmpty(item.getItemIconUrl()))
                    itemResponse.setItemIconUrl(Constant.IMAGE_NULL_SOURCE);
                
                GameRankingHistory gameRankingHistory = gameRankingHistoryRepo.
                    findByUserInfoAndItemAndCreateOnBetween(userInfo, item, startDate, endDate);
                if (ObjectUtils.isEmpty(gameRankingHistory)) {
                    itemResponse.setReceived(false);
                } else {
                    itemResponse.setValue(gameRankingHistory.getValue());
                    itemResponse.setReceived(true);
                    if (item.getItemType().equals(Constant.GAME_RANKING_ITEM_TYPE_GIFTCODE)
                            || item.getItemType().equals(Constant.GAME_RANKING_ITEM_TYPE_CARD)) {
                      itemResponse.setContent(gameRankingHistory.getContent());
                    }
                }
                
                itemResponse.setWinningTittle(item.getWinningTittle());
                itemResponses.add(itemResponse);
            }
            
            gameRankingRankGetResponse.setRankName(rank.getRankName());
            gameRankingRankGetResponse.setDecription(rank.getDecription());
            gameRankingRankGetResponse.setRankIconUrl(rank.getRankIconUrl());
            if (StringUtils.isEmpty(rank.getRankIconUrl()))
                gameRankingRankGetResponse.setRankIconUrl(Constant.IMAGE_NULL_SOURCE);
            gameRankingRankGetResponse.setRankPosition(rank.getRankPosition());
            gameRankingRankGetResponse.setRankQuantity(rank.getRankQuantity());
            gameRankingRankGetResponse.setItems(itemResponses);
            
            rankResponses.add(gameRankingRankGetResponse);
        }
        
        rankResponses = rankResponses.stream().sorted(
                (o1, o2) -> Integer.valueOf(o2.getRankPosition()).compareTo(o1.getRankPosition()))
                .collect(Collectors.toList());
        response.setRanks(rankResponses);
        
        return response;
    }

    @Override
    public String rankingAward(GameRankingAwardRequest request) {
        UserInfo userInfo = verifyAccessTokenUser();
        long myScoinId = 1400000139;
//        userInfo.getUserVTC().setScoin(myScoinId);
        
        if (ObjectUtils.isEmpty(request)
                || ObjectUtils.isEmpty(request.getServiceId())
                || ObjectUtils.isEmpty(request.getItemId())) {
            throw new ScoinInvalidDataRequestException();
        }
        
        GameRanking gameRanking = repo.findByServiceId(request.getServiceId());
        if (ObjectUtils.isEmpty(gameRanking)) {
            throw new ScoinNotFoundEntityException("This game don't have Ranking");
        }
        
        GameRankingItem item = gameRankingItemRepo.getOne(request.getItemId());
        if (ObjectUtils.isEmpty(item)) {
            throw new ScoinNotFoundEntityException("Not found item by this id");
        }
        
        //get rank id of last week
        long userRankId = getMyRankIdMaPhongKiem(gameRanking, myScoinId);
        
        if (userRankId != item.getGameRankingRank().getId()) {
            throw new ScoinNotEnoughtException("Không đúng Rank");
        }
        
        Calendar cal = Calendar.getInstance();
        Date startDate = new Date();
        Date endDate = new Date();

        cal.set(Calendar.DAY_OF_WEEK, 2);
        startDate = DateUtils.startDate(cal.getTime());

        cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        endDate = DateUtils.endDate(cal.getTime());
        
        GameRankingHistory gameRankingHistory = gameRankingHistoryRepo.
            findByUserInfoAndItemAndCreateOnBetween(userInfo, item, startDate, endDate);
        
        if (!ObjectUtils.isEmpty(gameRankingHistory)) {
            throw new ScoinDuplicateEntityException("you recieved this gift");
        }
        
        gameRankingHistory = new GameRankingHistory();
        gameRankingHistory.setGameRanking(gameRanking);
        gameRankingHistory.setItem(item);
        gameRankingHistory.setUserInfo(userInfo);
        gameRankingHistory.setItemType(item.getItemType());
        
        switch (item.getItemType()) {
        case Constant.GAME_RANKING_ITEM_TYPE_GIFTCODE:
            List<Giftcode> giftcodes = giftcodeRepo.
                findByUserInfoIsNullAndEventTypeAndMainEventIdAndSubEventIdOrderById(
                                                    Constant.GIFTCODE_EVENT_TYPE_GAME_RANKING, 
                                                    gameRanking.getId(), 
                                                    item.getGameRankingRank().getId());
            if (CollectionUtils.isEmpty(giftcodes)) {
                throw new ScoinNotEnoughtException("Giftcode is empty");
            }
            
            Giftcode giftcode = giftcodes.get(0);
            gameRankingHistory.setContent(giftcode.getMainCode());
            
            giftcode.setUserInfo(userInfo);
            giftcode = giftcodeRepo.save(giftcode);
            if (ObjectUtils.isEmpty(giftcode)) {
                throw new ScoinFailedToExecuteException();
            }
            
            addItemTuDoScoin(userInfo.getUserVTC().getScoinId(), 
                              Constant.SCOIN_TUDO_ITEM_TYPE_GIFTCODE, 
                              item.getItemName(), 
                              giftcode.getMainCode());
            break;
        case Constant.GAME_RANKING_ITEM_TYPE_INGAME:
            gameRankingHistory.setContent(item.getDescription());
            break;
            
        case Constant.GAME_RANKING_ITEM_TYPE_REAL:
            gameRankingHistory.setContent(item.getDescription());
            break;
            
        case Constant.GAME_RANKING_ITEM_TYPE_TURN_LUCKYSPIN:
            gameRankingHistory.setQuantity(item.getItemQuantity());
            break;
            
        case Constant.GAME_RANKING_ITEM_TYPE_XU:
            gameRankingExchangeXu(userInfo, 
                                  item.getItemValue(), 
                                  gameRanking.getName(),
                                  Constant.XU_TOPUP, 
                                  "gameRankingId : " + gameRanking.getId());
            gameRankingHistory.setValue(item.getItemValue());
            break;
            
        case Constant.GAME_RANKING_ITEM_TYPE_SCOIN:
            int multiplier = 0;
            if (item.getItemValue() > 1000000) {
                multiplier = item.getItemValue() / 1000000;
                for (int i = 0; i < multiplier; i++) {
                    cardScoinService.topupScoin(1000000, userInfo.getUserVTC().getUsername());
                }
            } else {
                cardScoinService.topupScoin(item.getItemValue(), userInfo.getUserVTC().getUsername());
            }
            gameRankingHistory.setValue(item.getItemValue());
            break;
            
        case Constant.GAME_RANKING_ITEM_TYPE_CARD:
            String valueCard = String.valueOf(item.getItemValue());
          FundsCardScoin fundsCardScoin = cardScoinService.buyCard(valueCard, 1);
          fundsCardScoin.setRecipientUser(userInfo.getUserVTC().getScoinId());
         
          String scoinCardInfo = "Mã thẻ : " + fundsCardScoin.getMainCodeCard()
                      + " - Seri : " + fundsCardScoin.getSeriCard()
                      + " - HSD : " + DateUtils.toStringFormDate(fundsCardScoin.getExpirationDateCard(), DateUtils.DATE_DEFAULT_FORMAT);
            gameRankingHistory.setContent(scoinCardInfo);
            gameRankingHistory.setValue(item.getItemValue());
            
            addItemTuDoScoin(userInfo.getUserVTC().getScoinId(), 
                    Constant.SCOIN_TUDO_ITEM_TYPE_SCOIN_CARD, 
                    item.getItemName(), 
                    scoinCardInfo);
            
            break;
        }
        
        gameRankingHistory = gameRankingHistoryRepo.save(gameRankingHistory);
        if (ObjectUtils.isEmpty(gameRankingHistory)) {
            throw new ScoinFailedToExecuteException();
        }
        
        return "Success";
    }
    
    private long getMyRankIdMaPhongKiem(GameRanking gameRanking, long myScoinId) {
        List<GameRankingRank> rankingRanks = new ArrayList<GameRankingRank>();
        
      //TODO: add date last week
//        long startDateUnix = startDate.getTime() / 1000;
//        long endDateUnix = endDate.getTime() / 1000;
        
        String UrlTopInfo = "http://api.phongmachien.vn/game.php/Api/other/get_api_data?action=getListOfRecharge&top=10&startdate=1586476800&enddate=1587168000";
        BasePhongMaChienResponse<TopTopupPhongmachienResponse> topTopupGame = ApiExchangeServiceUtil.
                get(UrlTopInfo, new TypeReference<BasePhongMaChienResponse<TopTopupPhongmachienResponse>>() {});
        
        BasePhongMaChienResponse<UserInfoPhongMaKiemGetResponse> userInfoGames = ApiExchangeServiceUtil.get(
                "http://api.phongmachien.vn/game.php/Api/other/webrecharge?action=getServerRole&accountid=" + myScoinId,
                new TypeReference<BasePhongMaChienResponse<UserInfoPhongMaKiemGetResponse>>() {});
        
        long userRankId = 0;
        
        gameRanking.setGameRankingRanks(gameRanking.getGameRankingRanks().stream()
                .sorted((o1, o2) -> Integer.valueOf(o1.getRankPosition())
                        .compareTo(Integer.valueOf(o2.getRankPosition())))
                .collect(Collectors.toList()));
        
        gameRanking.getGameRankingRanks().forEach(rank -> {
            for (int i = 0; i < rank.getRankQuantity(); i++) {
                rankingRanks.add(rank);
            }
        });
        
        if (!CollectionUtils.isEmpty(userInfoGames.getContent())) {
            for (UserInfoPhongMaKiemGetResponse userInfoGame : userInfoGames.getContent()) {
                List<TopTopupPhongmachienResponse> myAccounts = topTopupGame.getContent().stream()
                        .filter(userTop -> userTop.getRoleid() == userInfoGame.getRoleid())
                        .collect(Collectors.toList());
                
                if(!CollectionUtils.isEmpty(myAccounts)) {
                    if (myAccounts.get(0).getPosition() <=  rankingRanks.size())
                        userRankId = rankingRanks.get(myAccounts.get(0).getPosition() - 1).getId();
                }
            }
        }
        
        return userRankId;
    }
    
    private String createDescription(UserInfo userInfo, String gameRankingName, long amount) {
        return "Scoin ID : " + userInfo.getUserVTC().getScoinId()
                + ", UserName : " + userInfo.getUserVTC().getUsername()
                + ", requirement : " + "Topup nhận thưởng BXH : " + gameRankingName
                + ", Date : " + DateUtils.toStringFormDate(new Date(), DateUtils.DATE_TIME_MYSQL_FORMAT) 
                + ", Amount : " + amount;
    }
    
    private UserXuInfoResponse gameRankingExchangeXu(UserInfo userInfo, 
                                                   long amount, 
                                                   String gameRankingName, 
                                                   String typeExchange,
                                                   String dataRequest) {
        String content = createDescription(userInfo, gameRankingName, amount);
        
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
