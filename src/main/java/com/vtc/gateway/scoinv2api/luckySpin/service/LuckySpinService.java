/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.vtc.common.dao.entity.LuckySpin;
import com.vtc.common.dao.entity.LuckySpinItem;
import com.vtc.common.dto.request.LuckySpinGetRequest;
import com.vtc.common.dto.response.LuckySpinDetailResponse;
import com.vtc.common.dto.response.UserXuInfoResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.service.AbstractInterfaceService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 7, 2019
 */
public interface LuckySpinService extends AbstractInterfaceService<LuckySpin, Long> {
    
    List<LuckySpin> getLuckySpinActive(LuckySpinGetRequest request);
    
    List<LuckySpin> findLuckySpin(@Param("spinId") Long spinId,
                                  @Param("status") String status,
                                  Pageable pageable);

    int countLuckySpin(LuckySpinGetRequest request);

    Optional<LuckySpinDetailResponse> getLuckySpinDetail(Long luckySpinId) throws ScoinBusinessException;

    LuckySpinItem luckySpinAward(Long luckySpinId) throws ScoinBusinessException;

    UserXuInfoResponse getBalanceXu(Long scoinId) throws ScoinBusinessException;
    
    List<String> getLuckyNumbers(long cardValue, int quantity) throws Exception;
    
    String createLuckyNumber(int digitNumber) throws ScoinBusinessException;
    
}
