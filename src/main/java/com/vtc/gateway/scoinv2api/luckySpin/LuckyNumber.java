/***************************************************************************
 * Product made by Quang Dat *
 **************************************************************************/
package com.vtc.gateway.scoinv2api.luckySpin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Author : Dat Le Quang
 * Email: Quangdat0993@gmail.com
 * Jul 4, 2019
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LuckyNumber {

    private long    id;

    private String  luckyCode;

    private Long    scoinId;

    private boolean used;

}
