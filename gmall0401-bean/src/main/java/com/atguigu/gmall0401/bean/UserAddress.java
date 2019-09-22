package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author xtsky
 * @create 2019-09-22 12:28
 */
@Data
@NoArgsConstructor
public class UserAddress implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    @Column
    String userAddress;

    @Column
    String userId;

    @Column
    String consignee;

    @Column
    String phoneNum;

    @Column
    String isDefault;


}
