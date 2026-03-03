// 节点类型枚举
export enum NodeType {
  START = 'START',       // 发起人
  APPROVER = 'APPROVER', // 审批人
  CC = 'CC',             // 抄送人
  CONDITION = 'CONDITION', // 条件分支
  ROUTE = 'ROUTE',       // 路由容器
  EMPTY = 'EMPTY'        // 空节点（用于占位）
}

// 节点基础属性
export interface NodeProps {
  // 这里存放业务属性，如：审批人ID、发起人权限等
  [key: string]: any;
}

// 核心节点接口
export interface WorkflowNode {
  id: string;
  name: string;
  type: NodeType;
  props: NodeProps;
  childNode?: WorkflowNode | null; // 链表结构：指向下一个节点
  conditionNodes?: WorkflowNode[]; // 仅当 type === ROUTE 时有效
}