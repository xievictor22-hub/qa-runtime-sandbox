-- ==========================================
-- 1. 流程配置域 (满足需求 8, 9)
-- ==========================================

-- 流程分类 (如：人事、行政、财务)
CREATE TABLE `bpm_category`
(
    `id`   bigint      NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `code` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

-- 审批独用角色表 (满足需求 9)
CREATE TABLE `bpm_role`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT,
    `name`        varchar(50) NOT NULL COMMENT '角色名称',
    `remark`      varchar(200) DEFAULT NULL,
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 审批角色与用户关联表
CREATE TABLE `bpm_role_user`
(
    `role_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    PRIMARY KEY (`role_id`, `user_id`)
);

-- ==========================================
-- 2. 流程定义域 (满足需求 1-7, 8a-e)
-- ==========================================

-- 流程模型表 (核心配置表)
-- 这里存储前端设计器生成的 JSON
CREATE TABLE `bpm_model`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `category_id`  bigint      NOT NULL COMMENT '分类ID',
    `name`         varchar(64) NOT NULL COMMENT '流程名称',
    `process_key`  varchar(64) NOT NULL COMMENT 'Flowable Key (如 leave)',
    `icon`         varchar(64) DEFAULT NULL COMMENT '图标',

    -- 【核心】表单配置 (JSON) - 满足需求 1-7
    -- 包含：字段定义、必填校验、显隐规则、关联属性
    `form_conf`    longtext COMMENT '表单配置JSON',

    -- 【核心】流程节点配置 (JSON) - 满足需求 8
    -- 包含：节点审批人规则、分支条件、节点表单权限(只读/隐藏/编辑)
    `process_conf` longtext COMMENT '流程节点配置JSON',

    `status`       tinyint     DEFAULT 0 COMMENT '状态: 0=草稿, 1=已发布',
    `deploy_id`    varchar(64) DEFAULT NULL COMMENT '关联Flowable部署ID',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`process_key`)
);

-- ==========================================
-- 3. 流程实例域 (满足需求 10, 11)
-- ==========================================

-- 统一申请主表 (所有流程的入口)
CREATE TABLE `oa_application`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `process_key`     varchar(50)  NOT NULL COMMENT '流程标识 (leave)',
    `process_inst_id` varchar(64) DEFAULT NULL COMMENT 'Flowable实例ID',
    `business_id`     bigint       NOT NULL COMMENT '具体业务表ID (如 sys_leave.id)',

    `title`           varchar(200) NOT NULL COMMENT '标题',
    `user_id`         bigint       NOT NULL COMMENT '发起人',
    `dept_id`         bigint       NOT NULL COMMENT '发起人部门',
    `status`          tinyint     DEFAULT 0 COMMENT '状态: 0=草稿, 1=审批中, 2=通过, 3=驳回, 4=撤销',

    -- 冗余字段用于快速查询和权限过滤
    `apply_time`      datetime    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_dept` (`dept_id`),
    KEY `idx_inst` (`process_inst_id`)
);

-- ==========================================
-- 4. 业务域 (以请假为例)
-- ==========================================

-- 请假单 (独立附表)
CREATE TABLE `biz_leave`
(
    `id`         bigint        NOT NULL AUTO_INCREMENT,
    -- 只需要存业务特有字段，公共字段去 oa_application 查
    `type`       varchar(20)   NOT NULL COMMENT '请假类型',
    `start_time` datetime      NOT NULL,
    `end_time`   datetime      NOT NULL,
    `days`       decimal(5, 1) NOT NULL,
    `reason`     varchar(500) DEFAULT NULL,
    `files`      text COMMENT '附件JSON (满足需求4)',

    -- 预留扩展字段，用于存储表单设计器里新增的动态字段
    `extra_data` longtext COMMENT '扩展字段JSON',

    PRIMARY KEY (`id`)
);