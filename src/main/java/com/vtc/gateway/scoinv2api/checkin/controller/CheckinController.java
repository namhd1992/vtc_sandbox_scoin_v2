/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.checkin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vtc.common.AbstractController;
import com.vtc.gateway.scoinv2api.checkin.service.CheckinService;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jun 11, 2019
 */
@RestController
@RequestMapping("/checkin")
public class CheckinController extends AbstractController<CheckinService> {
    
    @PostMapping
    public ResponseEntity<?> checkin() {
        return response(toResult(service.checkin()));
    }

}
