package com.fqm.test.aop;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.fqm.test.service.TestService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodFindInvokeVisitor extends ClassVisitor {
    private final String methodName;
    private final String methodDesc;

    public MethodFindInvokeVisitor(int api, ClassVisitor classVisitor, String methodName, String methodDesc) {
        super(api, classVisitor);
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        if (methodName.equals(name) && methodDesc.equals(descriptor)) {
            return new MethodFindInvokeAdapter(api, null, name);
//        }
//        return null;
    }

    private static class MethodFindInvokeAdapter extends MethodVisitor {
        private final List<String> list = new ArrayList<>();
        private String methodName;
        
        public MethodFindInvokeAdapter(int api, MethodVisitor methodVisitor, String methodName) {
            super(api, methodVisitor);
            this.methodName = methodName;
            System.out.println("methodName=" + methodName);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 首先，处理自己的代码逻辑
            String info = String.format("  %s %s.%s%s", opcode + "", owner, name, descriptor);
//            if (!list.contains(info)) {
//                list.add(info);
//            }
            System.out.println(info);

            // 其次，调用父类的方法实现
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitEnd() {
            // 首先，处理自己的代码逻辑
            for (String item : list) {
                System.out.println(item);
            }

            // 其次，调用父类的方法实现
            super.visitEnd();
        }
    }
    
    public static void main(String[] args) throws IOException {
        Class methodCallClass = TestService.class;
        // 读取类的字节码
        ClassReader cr = new ClassReader(methodCallClass.getName());
        //（2）分析ClassVisitor
        int api = Opcodes.ASM9;
        ClassVisitor cv = new MethodFindInvokeVisitor(api, null, "test", "(II)V");

        //（3）结合ClassReader和ClassVisitor
        int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
        cr.accept(cv, parsingOptions);
    }
}
