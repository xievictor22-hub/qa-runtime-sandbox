// src/utils/download.ts
import { ElMessage } from 'element-plus';

/**
 * 通用 Blob 下载函数
 * @param data 后端返回的 Blob 数据
 * @param fileName 下载的文件名 (带后缀，如 user.xlsx)
 */
export const downloadBlob = (data: any, fileName: string) => {
  if (!data) {
    ElMessage.warning('文件下载失败，数据为空');
    return;
  }
  
  // 1. 创建 Blob 对象
  const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  
  // 2. 创建临时下载链接
  const href = window.URL.createObjectURL(blob);
  
  // 3. 创建隐藏的 a 标签并触发点击
  const downloadElement = document.createElement('a');
  downloadElement.style.display = 'none';
  downloadElement.href = href;
  downloadElement.download = fileName; // 设置文件名
  document.body.appendChild(downloadElement);
  downloadElement.click();
  
  // 4. 清理内存
  document.body.removeChild(downloadElement); // 下载完成移除元素
  window.URL.revokeObjectURL(href); // 释放掉blob对象
};