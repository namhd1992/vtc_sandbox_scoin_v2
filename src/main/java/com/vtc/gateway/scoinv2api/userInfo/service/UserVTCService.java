/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.userInfo.service;

import java.util.Optional;

import com.vtc.common.dao.entity.UserVTC;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 6, 2019
 */
public interface UserVTCService extends AbstractInterfaceService<UserVTC, Long> {

    public Optional<UserVTC> findByUserName(String userName) throws ScoinBusinessException;
    
    public Optional<UserVTC> findByScoinId(Long scoinId);

}
