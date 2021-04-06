/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.userInfo.service;

import java.util.List;

import com.vtc.common.dao.entity.UserInfo;
import com.vtc.common.dao.entity.UserVTC;
import com.vtc.common.dto.response.ProfileGetResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 6, 2019
 */
public interface UserInfoService extends AbstractInterfaceService<UserInfo, Long> {

    public UserInfo getUserInfoByUserName(String userName) throws ScoinBusinessException;
    
    public ProfileGetResponse getMyProfile(String scoinToken) throws ScoinBusinessException;
    
    public UserInfo getByScoinId(Long ScoinId) throws ScoinBusinessException;
    
    public UserVTC exchangeScoin(String typeExchange, int amount) throws ScoinBusinessException;
    
    public List<String> exportCardScoin(long valueCard, int quantity);

}
