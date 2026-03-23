package com.system.ai.predict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.ai.predict.persistence.AiPredictResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiPredictResultMapper extends BaseMapper<AiPredictResult> {
    int upsert(@Param("e") AiPredictResult entity);

    Long countRange(@Param("deviceId") String deviceId,
                    @Param("predictType") String predictType,
                    @Param("start") java.time.LocalDateTime start,
                    @Param("end") java.time.LocalDateTime end);

    java.util.List<AiPredictResult> selectRange(@Param("deviceId") String deviceId,
                                                @Param("predictType") String predictType,
                                                @Param("start") java.time.LocalDateTime start,
                                                @Param("end") java.time.LocalDateTime end,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit,
                                                @Param("asc") boolean asc);
}
