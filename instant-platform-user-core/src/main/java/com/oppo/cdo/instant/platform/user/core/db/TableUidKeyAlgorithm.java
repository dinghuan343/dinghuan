/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.oppo.cdo.instant.platform.user.core.db;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TableUidKeyAlgorithm implements SingleKeyTableShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(TableUidKeyAlgorithm.class);

    private final int factor = 1;

    private final int tableNum = 500;
    
    @Override
    public String doEqualSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        String tableName = shardingValue.getLogicTableName() +"_"+ (Math.abs(shardingValue.getValue().hashCode()) % tableNum);
        logger.warn("uid sharding tableName end,tabelName {},uid:{}", tableName,shardingValue);
        return tableName;
    }
    
    @Override
    public Collection<String> doInSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for(String value : shardingValue.getValues()){
            ShardingValue<String> sv = new ShardingValue(shardingValue.getLogicTableName(),shardingValue.getColumnName(),value);
            result.add(this.doEqualSharding(availableTargetNames,sv));
        }
        return result;
    }
    
    @Override
    public Collection<String> doBetweenSharding(final Collection<String> availableTargetNames, final ShardingValue<String> shardingValue) {
        return null;
    }


    public static void main(String[] args){
        String openId = "103106011555870";
        String uid = "1000012824";
        System.out.println((Math.abs(openId.hashCode()) % 500));
        System.out.println((Math.abs(uid.hashCode()) % 500));
    }
}
