import java.lang.reflect.Method;
public class Test {
    public static void main(String[] args) {
        for (Method m : android.telecom.PhoneAccount.Builder.class.getMethods()) {
            System.out.println(m.getName());
        }
    }
}
