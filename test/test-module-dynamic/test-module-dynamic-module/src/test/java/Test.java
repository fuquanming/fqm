import org.apache.commons.lang3.reflect.MethodUtils;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Opcodes;

public class Test extends ClassLoader implements Opcodes {
    public static void main(String[] args) throws Exception {
        ClassReader cr = new ClassReader("TargetObject");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classAdapter = new TargetObjectAdapter(Opcodes.ASM4, cw);
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
        byte[] data = cw.toByteArray();
        Test loader = new Test();
        Class<?> appClass = loader.defineClass(null, data, 0, data.length);
        appClass.getMethod("businessLogic", new Class[] {}).invoke(appClass.newInstance(), new Object[] {});
        System.out.println("---");
        TargetObject.businessLogic();
        System.out.println("---");
        MethodUtils.invokeStaticMethod(appClass, "businessLogic", null);
    }
}