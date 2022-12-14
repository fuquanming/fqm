package com.fqm.test.dynamic.module.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.core.ModuleClassLoaderFactory;
import com.fqm.dynamic.module.filter.ModuleLoaderFilter;
import com.fqm.dynamic.module.filter.ModuleUnloadFilter;
import com.fqm.dynamic.module.filter.mybatis.MyBatisMapLoaderFilter;
import com.fqm.dynamic.module.filter.mybatis.MybatisPlusMapUnloadFilter;
import com.fqm.dynamic.module.filter.spring.MybatisSpringLoaderFilter;
import com.fqm.dynamic.module.filter.spring.SpringLoaderFilter;
import com.fqm.dynamic.module.filter.spring.SpringUnloadFilter;
import com.fqm.dynamic.module.filter.spring.SwaggerSpringLoaderFilter;
import com.fqm.dynamic.module.filter.spring.SwaggerSpringUnLoaderFilter;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.common.spring.util.SpringUtil;
import com.fqm.framework.common.swagger.config.SwaggerProperties;

@RestController
public class ClassLoaderController {
    
    public static final String DYNAMIC = "dynamic";
    
    @GetMapping(value = "/loadJar")
    public Result<List<?>> loadJar(String jarPath, int index) {
        String moduleName = "module-dept";
        String mybatisPackage = "com.fqm.module.dept.dao";
        if (index == 1 || index == 0) {
//            moduleName = "module-user";
//            mybatisPackage = "com.fqm.module.test.dao";
//            jarPath = "D:/fqm/gitee/fuquanming/code-master/testApp-jar/target/testAppJar-0.0.1.jar";
            jarPath = "D:/fqm/github/fuquanming/fqm/test/test-module-dynamic/test-module-dynamic-module-2-dept/target/test-module-dynamic-module-2-dept-1.0.0.jar";
        } else {
            moduleName = "module-dept";
            mybatisPackage = "com.fqm.module.dept.dao";
            // 子模块，二次加载失败，Dao找不到->service extends ServiceImpl<DeptDao, Dept>出现->卸载没完成deptDao卸载，加载后出现2个deptDao，无法赋值
            // 解决：SpringUnloadFilter 移除 mergedBeanDefinitions 对应的bean
            jarPath = "D:/fqm/github/fuquanming/fqm/test/test-module-dynamic/test-module-dynamic-module-dept/target/test-module-dynamic-module-dept-1.0.0.jar";
        }
        
        try {
            System.out.println("loadJar=" + jarPath);
            
            ModuleClassLoader old = ModuleClassLoaderFactory.getModuleClassLoader(moduleName);
            if (old != null) {
                ModuleClassLoaderFactory.unloadModule(moduleName);
            }

            List<ModuleLoaderFilter> loaderFilters = new ArrayList<>();
            List<ModuleUnloadFilter> unloadFilters = new ArrayList<>();

            loaderFilters.add(new MyBatisMapLoaderFilter(SpringUtil.getBean(SqlSessionFactory.class)));
            loaderFilters.add(new MybatisSpringLoaderFilter(mybatisPackage));
            loaderFilters.add(new SpringLoaderFilter());
            SwaggerProperties swaggerProperties = new SwaggerProperties();
            swaggerProperties.setBasePackage("com.fqm.module.dept.controller");
            swaggerProperties.setGroupName(moduleName);
            loaderFilters.add(new SwaggerSpringLoaderFilter(swaggerProperties));

            unloadFilters.add(new MybatisPlusMapUnloadFilter(SpringUtil.getBean(SqlSessionFactory.class)));
            unloadFilters.add(new SwaggerSpringUnLoaderFilter(moduleName));
            unloadFilters.add(new SpringUnloadFilter());

            ModuleClassLoaderFactory.build(jarPath, moduleName, loaderFilters, unloadFilters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
//        swagger(moduleName);
        
        return Result.ok(getAllBean());
    }

    public List<Map<String, Object>> getAllBean() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] beans = SpringUtil.getApplicationContext().getBeanDefinitionNames();

        for (String beanName : beans) {
            Class<?> beanType = SpringUtil.getApplicationContext().getType(beanName);
            if (beanType.getName().startsWith("springfox.boot.starter.")) {
                Map<String, Object> map = new HashMap<>();
                map.put("BeanName", beanName);
                map.put("beanType", beanType);
                map.put("package", beanType.getPackage());
                list.add(map);
            }
        }
        return list;
    }
    
    @GetMapping(value = "/bean")
    public Result<?> getBean() {
//        Object bean = SpringUtil.getBean("deptDao");
//        System.out.println(bean);
////        ((DefaultListableBeanFactory)SpringUtil.getBeanFactory()).containsSingleton("deptService")
//        bean = SpringUtil.getBean("deptService");
//        System.out.println(bean);
        System.out.println(JsonUtil.toJsonStr(SpringUtil.getBean("springfox.documentation-springfox.boot.starter.autoconfigure.SpringfoxConfigurationProperties")));
        
        return Result.ok(getAllBean());
    }
}
