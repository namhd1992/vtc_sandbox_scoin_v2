/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.shoping.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vtc.common.dao.entity.ShopItemPro;
import com.vtc.common.dao.repository.ShopItemProRepository;
import com.vtc.common.service.AbstractService;
import com.vtc.gateway.scoinv2api.shoping.service.ShopItemProService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 25, 2019
 */
@Service("shopItemProService")
public class ShopItemProServiceImpl extends
        AbstractService<ShopItemPro, Long, ShopItemProRepository> implements ShopItemProService {

    @Override
    public Optional<ShopItemPro> findByShopItem(long ShopingItemId) {
        return repo.findByShopItem(ShopingItemId);
    }

}
