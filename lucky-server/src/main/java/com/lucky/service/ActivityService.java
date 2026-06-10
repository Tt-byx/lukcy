package com.lucky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lucky.entity.Activity;

import java.util.List;

public interface ActivityService extends IService<Activity> {

    Activity createActivity(String name);

    Activity getCurrentActivity();

    List<Activity> getHistoryActivities();
}
