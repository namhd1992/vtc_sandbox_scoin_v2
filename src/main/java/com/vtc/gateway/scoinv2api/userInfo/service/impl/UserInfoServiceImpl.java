/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.userInfo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vtc.common.dao.entity.FundsCardScoin;
import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.entity.UserVTC;
import com.vtc.common.dao.repository.GroupRoleRepository;
import com.vtc.common.dao.repository.UserInfoRepository;
import com.vtc.common.dto.response.ProfileGetResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.exception.ScoinInvalidDataRequestException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.service.CommonCardScoinService;
import com.vtc.common.service.PaymentService;
import com.vtc.common.utils.DateUtils;
import com.vtc.common.utils.StringUtils;
import com.vtc.gateway.scoinv2api.userInfo.service.UserInfoService;
import com.vtc.gateway.scoinv2api.userInfo.service.UserVTCService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 6, 2019
 */
@Service("userInfoService")
public class UserInfoServiceImpl extends AbstractService<UserInfo, Long, UserInfoRepository>
        implements UserInfoService {
    
    @Autowired
    UserVTCService         userVTCService;

    @Autowired
    GroupRoleRepository    groupRoleRepo;

    @Autowired
    CommonCardScoinService CardScoinService;

    @Autowired
    PaymentService         paymentService;
    
    @Override
    public UserInfo getUserInfoByUserName(String userName) throws ScoinBusinessException {
        if (StringUtils.isEmpty(userName)) {
            throw new ScoinInvalidDataRequestException();
        }

        Optional<UserVTC> userVTC = userVTCService.findByUserName(userName);
        if (!userVTC.isPresent()) {
            return repo.findByFullName(userName);
        }

        UserInfo userInfo = userVTC.get().getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ScoinNotFoundEntityException("Not fount user info by this username");
        }

        userInfo.setUrlAvatar(userVTC.get().getUsername());
        return userInfo;
    }

    @Override
    public ProfileGetResponse getMyProfile(String scoinToken) throws ScoinBusinessException {
        UserInfo userInfo = verifyAccessTokenUser();

        if (StringUtils.isEmpty(scoinToken)) {
            throw new ScoinInvalidDataRequestException();
        }
        
        paymentService.updateBalanceSoin(userInfo);
        ProfileGetResponse profileResponse = new ProfileGetResponse();
        profileResponse.setUserId(userInfo.getId());
        if (!StringUtils.isEmpty(userInfo.getFullName())) {
            profileResponse.setFullName(userInfo.getFullName());
        } else {
            profileResponse.setFullName(userInfo.getUserVTC().getUsername());
        }
        
        profileResponse.setUrlAvatar(userInfo.getUrlAvatar());
        profileResponse.setAccountNumber(userInfo.getUserVTC().getScoinId());
        profileResponse.setVipLevel(userInfo.getVipLevel());
        if (!StringUtils.isEmpty(userInfo.getPhoneNumber()))
            profileResponse.setPhoneNumber(StringUtils.hiddenCharString(userInfo.getPhoneNumber(), 3));

        if (!StringUtils.isEmpty(userInfo.getEmail()))
            profileResponse.setEmail(StringUtils.encodeEmail(userInfo.getEmail()));

        return profileResponse;
    }
    
    @Override
    public List<String> exportCardScoin(long valueCard, int quantity) {
        if (valueCard == 0 || quantity == 0) {
            throw new ScoinInvalidDataRequestException();
        }
        List<String> responses = new ArrayList<String>();
        for (int i = 0; i < quantity; i++) {
            FundsCardScoin fundsCardScoin = CardScoinService.buyCard(String.valueOf(valueCard), 1);
            if (ObjectUtils.isEmpty(fundsCardScoin)) {
                throw new ScoinNotFoundEntityException("Not export card SCOIN");
            }
            String response = "Mã thẻ : " + fundsCardScoin.getMainCodeCard()
                    + " - Seri : " + fundsCardScoin.getSeriCard() 
                    + " - HSD : " + DateUtils.toStringFormDate(fundsCardScoin.getExpirationDateCard(), DateUtils.DATE_TIME_MYSQL_FORMAT)
                    + " - Mệnh giá : " + fundsCardScoin.getValueCard();
            responses.add(response);
        }
        
        return responses;
    }
    
    @Override
    public UserInfo getByScoinId(Long scoinId) throws ScoinBusinessException {
        return repo.getByScoinId(scoinId);
    }

    @Override
    public UserVTC exchangeScoin(String typeExchange, int amount) throws ScoinBusinessException {
        UserInfo userInfo = verifyAccessTokenUser();
        paymentService.exchangeScoin(userInfo, typeExchange, amount, "Test " + typeExchange + " Scoin, amount : " + amount);
        paymentService.updateBalanceSoin(userInfo);
        return userInfo.getUserVTC();
    }
    
}
