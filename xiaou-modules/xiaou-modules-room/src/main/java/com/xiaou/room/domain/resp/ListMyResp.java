package com.xiaou.room.domain.resp;

import com.xiaou.room.domain.entity.DormRoom;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;
@Data
@AutoMapper(target = DormRoom.class)
public class ListMyResp {
    /**
     * 宿舍ID（主键，UUID或雪花ID）
     */
    private String id;


    /**
     * 宿舍楼ID
     */
    private String buildingId;

    /**
     * 宿舍房间号，如A101、B302
     */
    private String roomNumber;

    /**
     * 宿舍最大可容纳人数（床位总数）
     */
    private Integer capacity;

    /**
     * 当前剩余可选床位数（缓存到Redis中做库存控制）
     */
    private Integer available;

    private List<DormBedResp> dormBeds;

    /**
     * 建筑名字
     */
    private String buildingName;
}
