import { ref, toRefs } from 'vue';
import { getDicts } from '@/api/system/dict';

/**
 * 字典 Hook
 * @param args 字典类型字符串数组
 */
export function useDict(...args: string[]) {
  const res = ref<any>({});
  
  return (() => {
    args.forEach((dictType) => {
      res.value[dictType] = [];
      getDicts(dictType).then((resp:any) => {
        // 这里把后端返回的增强字段(listClass, cssClass)都带上
        res.value[dictType] = resp.map((p: any) => ({ 
            label: p.dictLabel, 
            value: p.dictValue, 
            elTagType: p.listClass, // 用于 Element Plus Tag 组件
            class: p.cssClass 
        }));
      });
    });
    return toRefs(res.value);
  })();
}