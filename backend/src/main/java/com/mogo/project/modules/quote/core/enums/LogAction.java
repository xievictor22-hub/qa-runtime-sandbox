package com.mogo.project.modules.quote.core.enums;

public enum LogAction {
    // --- 报价员动作 ---
    CREATE("创建报价单"),
    UPDATE("更新基础信息"),       // 修改项目名称、税率等
    IMPORT_DETAIL("导入明细"),    // 导入Excel
    SAVE_DETAIL("保存明细"),      // 手动修改明细/维护制品
    SUBMIT_AUDIT("提交审核"),     // 0/4 -> 1
    CREATE_VERSION("创建新版本"), // 4 -> 4 (版本号+1)

    // --- 审核员动作 ---
    AUDIT_PASS("审核通过"),       // 1 -> 2
    AUDIT_REJECT("审核驳回"),     // 1 -> 4

    // --- 业务员动作 ---
    BUSINESS_SAVE("保存价格调整"), // 保存折扣/最终价
    COMPLETED("确认完成"),        // 2 -> 3
    RE_OPEN("发起重新调整"),      // 3 -> 5

    // --- 系统/管理员动作 ---
    ASSIGN("指派处理人"),         // 管理员干预
    SYSTEM_AUTO("系统自动处理");

    private final String desc;
    LogAction(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
