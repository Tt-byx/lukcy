package com.lucky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.entity.Activity;
import com.lucky.mapper.ActivityMapper;
import com.lucky.service.ActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity>
        implements ActivityService {

    @Override
    public Activity createActivity(String name) {
        Activity activity = new Activity();
        activity.setName(name);
        activity.setStatus(0);
        save(activity);
        return activity;
    }

    @Override
    public Activity getCurrentActivity() {
        // 获取最新的进行中或未开始的活动
        return getOne(new LambdaQueryWrapper<Activity>()
                .in(Activity::getStatus, 0, 1)
                .orderByDesc(Activity::getCreatedAt)
                .last("LIMIT 1"));
    }

    @Override
    public List<Activity> getHistoryActivities() {
        return list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 2)
                .orderByDesc(Activity::getCreatedAt));
    }
}
