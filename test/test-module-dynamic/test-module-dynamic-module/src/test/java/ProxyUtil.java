public class ProxyUtil {

    public void businessLogic() {
    }

    public static void before() {
        System.out.println("目标对象/真实对象      方法调用之前...");
    }

    public static void after() {
        System.out.println("目标对象/真实对象      方法调用之后...");
    }
}