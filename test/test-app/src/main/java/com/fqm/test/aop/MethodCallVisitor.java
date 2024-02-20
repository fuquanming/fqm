//package com.fqm.test.aop;
//
//import org.objectweb.asm.*;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//public class MethodCallVisitor extends ClassVisitor {
//    private String targetMethodName;
//    private String targetMethodDesc;
// 
//    public MethodCallVisitor(String targetMethodName, String targetMethodDesc) {
//        super(Opcodes.ASM9);
//        this.targetMethodName = targetMethodName;
//        this.targetMethodDesc = targetMethodDesc;
//    }
// 
//    @Override
//    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
//        if (mv != null) {
//            return new CheckMethodCallAdapter(mv, targetMethodName, targetMethodDesc);
//        }
//        return mv;
//    }
// 
//    private static class CheckMethodCallAdapter extends MethodWriter {
//        private String targetMethodName;
//        private String targetMethodDesc;
// 
//        public CheckMethodCallAdapter(MethodVisitor mv, String targetMethodName, String targetMethodDesc) {
//            super(Opcodes.ASM9, mv, null);
//            this.targetMethodName = targetMethodName;
//            this.targetMethodDesc = targetMethodDesc;
//        }
// 
//        @Override
//        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//            if (name.equals(targetMethodName) && descriptor.equals(targetMethodDesc)) {
//                System.out.println("Found method call to " + targetMethodName + " in method " + getClassName() + "." + getMethodName());
//            }
//            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//        }
//    }
//    
//    // 使用示例
//    public static void main(String[] args) throws IOException {
//        ClassReader cr = new ClassReader("YourClassFullNameHere");
//        ClassWriter cw = new ClassWriter(0);
//        MethodCallVisitor mcv = new MethodCallVisitor("yourTargetMethodName", "()V"); // 替换为目标方法名和描述符
//        cr.accept(mcv, ClassReader.EXPAND_FRAMES);
//    }
//}
// 
//
