/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.shoping.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vtc.common.dao.entity.ShopItemPro;
import com.vtc.common.dao.entity.ShopingItem;
import com.vtc.common.dao.repository.ShopingItemRepository;
import com.vtc.common.dto.request.ShopingItemGetRequest;
import com.vtc.common.dto.response.GetShopingItemResponse;
import com.vtc.common.exception.ScoinBusinessException;
import com.vtc.common.exception.ScoinNotFoundEntityException;
import com.vtc.common.service.AbstractService;
import com.vtc.gateway.scoinv2api.shoping.service.ShopItemProService;
import com.vtc.gateway.scoinv2api.shoping.service.ShopingItemService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 23, 2019
 */
@Service("shopingItemService")
public class ShopingItemServiceImpl extends
        AbstractService<ShopingItem, Long, ShopingItemRepository> implements ShopingItemService {
    
    @Autowired
    ShopItemProService shopItemService;

    @Override
    public List<GetShopingItemResponse> getShopingItem(ShopingItemGetRequest request)
            throws ScoinBusinessException {
        
        Pageable pageable = PageRequest.of(request.getOffset(), request.getLimit());
        List<ShopingItem> shopingItems = repo.getShopingItem(request.getItemId(), 
                request.getSearchValue(), 
                request.getFilter(), 
                request.getShopId(), 
                request.getItemType(),
                pageable);
        
        if (CollectionUtils.isEmpty(shopingItems)) {
            return new ArrayList<>();
        }
        
        var responses = new ArrayList<GetShopingItemResponse>();
        for(ShopingItem item : shopingItems) {
            if (item.getItemType() == 3) continue;
            
            GetShopingItemResponse response = new GetShopingItemResponse();
            if (item.isHasPromotion()) {
                ShopItemPro shopItemPro = shopItemService.findByShopItem(item.getId())
                        .orElseThrow(() -> new ScoinNotFoundEntityException("Not found Promotion of this item"));
                response.setPromotion(shopItemPro);
            }
            response.setShopingItem(item);
            responses.add(response);
        }
        
        return responses;
    }

    @Override
    public int countListShopingItem(ShopingItemGetRequest request) {
        return repo.countGetShopingItem(request.getItemId(), 
                request.getSearchValue(), 
                request.getFilter(), 
                request.getShopId(), 
                request.getItemType());
    }

}
