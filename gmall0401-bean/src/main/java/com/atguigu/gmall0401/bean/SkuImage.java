package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author xtsky
 * @create 2019-09-09 20:47
 */
@Data
@NoArgsConstructor
public class SkuImage implements Serializable {

    @Id
    @Column
    String id;

    @Column
    String skuId;

    @Column
    String imgName;

    @Column
    String imgUrl;

    @Column
    String spuImgId;

    @Column
    String isDefault;


}
