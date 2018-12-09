package com.edlplan.audiov.scan;


import com.edlplan.audiov.core.utils.Consumer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface ISongListScanner {

    /**
     * 按自定义的规则扫描歌曲
     * @param consumer 接受扫描的的铺面的接口
     * @throws Exception 可能抛出任何错误x
     */
    void scan(Consumer<SongEntry> consumer) throws Exception;

    void initial(JSONObject data) throws Exception;

    default List<SongEntry> scanAsList() throws Exception {
        ArrayList<SongEntry> list = new ArrayList<>();
        scan(list::add);
        return list;
    }

    /**
     * 删除这个Scanner时被调用，用来删除一些缓存文件
     */
    default void onDelete() {

    }
}
