/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.shoping.service;

import java.util.List;

import com.vtc.common.dto.request.ShopingItemGetRequest;
import com.vtc.common.dto.response.GetShopingItemResponse;
import com.vtc.common.exception.ScoinBusinessException;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 23, 2019
 */
public interface ShopingItemService {
    
    public List<GetShopingItemResponse> getShopingItem(ShopingItemGetRequest request) throws ScoinBusinessException;
    
    public int countListShopingItem(ShopingItemGetRequest request); 

}
