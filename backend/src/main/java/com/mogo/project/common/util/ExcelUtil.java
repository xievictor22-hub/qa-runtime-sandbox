package com.mogo.project.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtil {

    /**
     * 通用导出方法
     * @param response 响应
     * @param data 数据列表
     * @param clazz 导出对象的 Class
     * @param sheetName sheet 名称
     * @param fileName 下载的文件名
     */
    public static <T> void writeExcel(HttpServletResponse response, List<T> data, Class<T> clazz, String sheetName, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 防止中文乱码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            // 这里应该重置 response 并返回 JSON 格式的错误提示，根据你的全局异常处理来写
            e.printStackTrace();
            throw new RuntimeException("导出 Excel 失败");
        }
    }
}