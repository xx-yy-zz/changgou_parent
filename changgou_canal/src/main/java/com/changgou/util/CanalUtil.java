package com.changgou.util;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanalUtil {

    /**
     * 列集合对象转换为map
     * @param columnsList
     * @return
     */
    public static Map<String,String> convertToMap(List<CanalEntry.Column> columnsList){
        Map<String,String> map=new HashMap<>(  );
        columnsList.forEach( column -> map.put( column.getName(),column.getValue() )  );
        return map;
    }


}
