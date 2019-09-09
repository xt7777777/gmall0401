package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author xtsky
 * @create 2019-09-08 22:32
 */
@Data
@NoArgsConstructor
public class BaseSaleAttr implements Serializable {

    @Id
    @Column
    String id ;

    @Column
    String name;

}
