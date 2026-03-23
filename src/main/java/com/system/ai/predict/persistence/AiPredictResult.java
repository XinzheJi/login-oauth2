package com.system.ai.predict.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_predict_result")
public class AiPredictResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private String predictType; // AGING / FAULT
    private LocalDateTime predictionTime;
    private String resultJson;
    private LocalDateTime createdAt;
}

