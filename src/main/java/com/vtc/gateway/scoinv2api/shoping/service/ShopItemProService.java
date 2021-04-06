/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.shoping.service;

import java.util.Optional;

import com.vtc.common.dao.entity.ShopItemPro;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 25, 2019
 */
public interface ShopItemProService {
    
    Optional<ShopItemPro> findByShopItem(long ShopingItemId);

}
