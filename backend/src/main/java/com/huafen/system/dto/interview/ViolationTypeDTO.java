package com.huafen.system.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 违规类型DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationTypeDTO {

    /**
     * 违规类型代码
     */
    private String code;

    /**
     * 违规类型名称
     */
    private String name;

    /**
     * 违规类型描述
     */
    private String description;

    /**
     * 严重程度 (1-5)
     */
    private Integer severity;
}
