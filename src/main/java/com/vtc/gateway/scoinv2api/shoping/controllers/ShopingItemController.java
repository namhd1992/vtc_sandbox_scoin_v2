/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.shoping.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vtc.common.AbstractController;
import com.vtc.common.dto.request.ShopingItemGetRequest;
import com.vtc.common.utils.JsonMapperUtils;
import com.vtc.gateway.scoinv2api.shoping.service.ShopingItemService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Apr 23, 2019
 */
@RestController
public class ShopingItemController extends AbstractController<ShopingItemService> {

    @GetMapping("/anonymous/shopingItem")
    public ResponseEntity<?> getShopingItem(ShopingItemGetRequest request){
        LOGGER.info("===============REQUEST============== \n\n {}", 
                JsonMapperUtils.toJson(request));
        return response(toResult(service.getShopingItem(request), service.countListShopingItem(request)));
    }

}
