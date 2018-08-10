/*
 * Copyright (c) 2018. Aberic - All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.aberic.fabric.utils;

import cn.aberic.fabric.bean.Block;
import cn.aberic.fabric.bean.Trace;
import cn.aberic.fabric.dao.Channel;
import cn.aberic.fabric.service.PeerService;
import cn.aberic.fabric.service.TraceService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Aberic on 2018/8/10 21:04
 * 邮箱：abericyang@gmail.com
 */
public class DataUtil {

    private static DataUtil instance;

    public static DataUtil obtain() {
        if (null == instance) {
            synchronized (DataUtil.class) {
                if (null == instance) {
                    instance = new DataUtil();
                }
            }
        }
        return instance;
    }

    public List<Block> home(List<Channel> channels, PeerService peerService, TraceService traceService) {
        List<Block> blocks = new ArrayList<>();
        for (Channel channel : channels) {
            try {
                JSONObject blockInfo = JSON.parseObject(traceService.queryBlockChainInfoForIndex(channel.getId()));
                int height = blockInfo.containsKey("data") ? blockInfo.getJSONObject("data").getInteger("height") : 0;

                Trace trace = new Trace();
                trace.setChannelId(channel.getId());
                trace.setTrace(String.valueOf(height - 1));
                JSONObject blockMessage = JSON.parseObject(traceService.queryBlockByNumberForIndex(trace));
                JSONArray envelopes = blockMessage.containsKey("data") ? blockMessage.getJSONObject("data").getJSONArray("envelopes") : new JSONArray();

                Block block = new Block();
                block.setNum(height);
                block.setPeerName(peerService.get(channel.getPeerId()).getName());
                block.setChannelName(channel.getName());
                block.setCalculatedBlockHash(blockMessage.getJSONObject("data").getString("calculatedBlockHash"));
                block.setDate(envelopes.getJSONObject(0).getString("timestamp"));
                blocks.add(block);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        blocks.sort((t1, t2) -> {
            try {
                long td1 = DateUtil.str2Date(t1.getDate(), "yyyy/MM/dd HH:mm:ss").getTime();
                long td2 = DateUtil.str2Date(t2.getDate(), "yyyy/MM/dd HH:mm:ss").getTime();
                return Math.toIntExact(td2 - td1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setIndex(i + 1);
        }
        CacheUtil.putHome(blocks);
        return blocks;
    }
}
