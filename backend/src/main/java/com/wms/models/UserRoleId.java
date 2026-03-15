package com.wms.models;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRoleId implements Serializable {

    private Long user;
    private Integer role;
}
