package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author xtsky
 * @create 2019-09-08 22:50
 */
@Data
@NoArgsConstructor
public class SpuSaleAttrValue implements Serializable {

    @Id
    @Column
    String id ;

    @Column
    String spuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrValueName;

    @Transient
    String isChecked;

}
