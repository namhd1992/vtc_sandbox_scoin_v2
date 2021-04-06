/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.userInfo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vtc.common.AbstractController;
import com.vtc.common.utils.JsonMapperUtils;
import com.vtc.gateway.scoinv2api.userInfo.service.UserInfoService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * May 6, 2019
 */
@RestController
public class UserInfoController extends AbstractController<UserInfoService>{
    
    @GetMapping("/name")
    public ResponseEntity<?> getUserInfoByName(@RequestParam("user_name") String userName) {
        LOGGER.info("===============REQUEST============== \n {}",
                JsonMapperUtils.toJson(userName));
        return response(toResult(service.getUserInfoByUserName(userName)));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(@RequestParam("scoin_access_token") String scoinAccessToken) {
      LOGGER.info("===============REQUEST============== \n {}",
          JsonMapperUtils.toJson(scoinAccessToken));
        return response(toResult(service.getMyProfile(scoinAccessToken)));
    }
    
    @GetMapping("/exchange-scoin")
    public ResponseEntity<?> exchangeScoin(@RequestParam("type_exchange") String typeExchange,
                                           @RequestParam("amount") int amount) {
      LOGGER.info("===============REQUEST============== \n {}",
          JsonMapperUtils.toJson(amount));
        return response(toResult(service.exchangeScoin(typeExchange, amount)));
    }
    
//    @GetMapping("/scoin/export-scoin-card")
//    public ResponseEntity<?> exportScoinCard(@RequestParam("value_card") long valueCard,
//                                             @RequestParam("quantity") int quantity) {
//      LOGGER.info("===============REQUEST============== \n {}",
//          JsonMapperUtils.toJson(valueCard));
//        return response(toResult(service.exportCardScoin(valueCard, quantity)));
//    }

}
