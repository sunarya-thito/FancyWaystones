
import javax.script.*;

public class ScriptTest {
    public static void main(String[] args) throws Throwable {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");
        Bindings bindings = new SimpleBindings();
        bindings.put("a", new A());
        System.out.println(engine.eval("public class Script { public ScriptTest.A a; public java.lang.Object test() {return a.C;} }", bindings));
    }

    public static class A {
        public int C = 10;
        public int D = 231;
    }
}
