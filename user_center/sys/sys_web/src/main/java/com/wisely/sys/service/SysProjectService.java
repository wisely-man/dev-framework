package com.wisely.sys.service;

import com.github.pagehelper.PageInfo;
import com.wisely.framework.entity.PageVo;
import com.wisely.sys.entity.SysProject;

public interface SysProjectService {


    PageInfo list(SysProject query, PageVo pageVo);

}
