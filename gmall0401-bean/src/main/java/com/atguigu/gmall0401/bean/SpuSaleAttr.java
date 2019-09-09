package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-08 22:49
 */
@Data
@NoArgsConstructor
public class SpuSaleAttr  implements Serializable {

    @Id
    @Column
    String id ;

    @Column
    String spuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrName;


    @Transient
    List<SpuSaleAttrValue> spuSaleAttrValueList;
}
