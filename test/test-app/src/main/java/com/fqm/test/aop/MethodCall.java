package com.fqm.test.aop;

import org.objectweb.asm.*;

import com.fqm.test.service.TestService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodCall {

    public static void main(String[] args) throws IOException {
        
        Class methodCallClass = TestService.class;
        // 读取类的字节码
        ClassReader classReader = new ClassReader(methodCallClass.getName());

        Map<String, Method> methodMap = new HashMap<>();
        Method[] methods = methodCallClass.getMethods();
        // 获取有标签的方法
        for (Method method : methods) {
            FileUseNotify fileUseNotify = method.getAnnotation(FileUseNotify.class);
            if (null != fileUseNotify) {
                methodMap.put(method.getName(), method);
            }
        }
        System.out.println(methodMap);
        
        // 创建一个ClassVisitor
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                // 仅处理非构造函数的方法
                if (!name.equals("<init>")) {
                    System.out.println("Method: " + name);
                    // 创建一个MethodVisitor访问方法的字节码
                    MethodVisitor mv = new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                        // 该方法中调用了哪些方法
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            System.out.println(opcode + "  Invoke: " + owner + "." + name +"-"+ descriptor);
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };

                    return mv;
                }

                return methodVisitor;
            }
        };

        // 分析类的字节码
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
    }


}
