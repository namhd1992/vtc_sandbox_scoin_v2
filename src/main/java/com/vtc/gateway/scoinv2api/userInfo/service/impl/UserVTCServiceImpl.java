/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.userInfo.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vtc.common.dao.entity.UserVTC;
import com.vtc.common.dao.repository.UserVTCRepository;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.exception.ScoinInvalidDataRequestException;
import com.vtc.common.service.AbstractService;
import com.vtc.common.utils.StringUtils;
import com.vtc.gateway.scoinv2api.userInfo.service.UserVTCService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 6, 2019
 */
@Service("userVTCService")
public class UserVTCServiceImpl extends AbstractService<UserVTC, Long, UserVTCRepository>
        implements UserVTCService {

    @Override
    public Optional<UserVTC> findByUserName(String userName) throws ScoinBusinessException {
        if (StringUtils.isEmpty(userName)) {
            throw new ScoinInvalidDataRequestException();
        }
        
        return repo.findByUsername(userName);
    }

    @Override
    public Optional<UserVTC> findByScoinId(Long scoinId) {
        return repo.findByScoinId(scoinId);
    }

}
