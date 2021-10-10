package com.xiao.domain.strategy.repository;

import com.xiao.domain.strategy.model.aggregates.StrategyRich;
import com.xiao.infrastructure.po.Award;

/**
 * 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 * 公众号：bugstack虫洞栈
 * Create by 小傅哥(fustack)
 */
public interface IStrategyRepository {

    StrategyRich queryStrategyRich(Long strategyId);

    Award queryAwardInfo(String awardId);

}
