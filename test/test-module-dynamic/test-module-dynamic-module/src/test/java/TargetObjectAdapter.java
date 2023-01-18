import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class TargetObjectAdapter extends ClassVisitor {

    private MethodVisitor mv;

    public TargetObjectAdapter(int api, ClassVisitor cw) {
        super(api, cw);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("businessLogic")) {
            return new MethodVisitor(this.api, mv) {
                public void visitCode() {
                    super.visitCode();
                    //                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "ProxyUtil", "before", "()V");
                    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn("before");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
                }

                public void visitInsn(int opcode) {
                    if (opcode == Opcodes.RETURN) {
                        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        mv.visitLdcInsn("after");
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
                        //                        this.visitMethodInsn(Opcodes.INVOKESTATIC, "ProxyUtil", "after", "()V");
                    }
                    super.visitInsn(opcode);
                }
            };
        }
        return mv;
    }
}