import org.mozilla.javascript.*;

public class ScriptTest {
    public double radius;
    public static void main(String[] args) throws Throwable {
        Context context = Context.enter();
        Script script = context.compileString("radius = radius + 0.1; radius = radius + 0.2", "a", 0, null);
        ScriptTest adapter = new ScriptTest();
        Scriptable scriptable = JavaAdapter.createAdapterWrapper(context.initSafeStandardObjects(), adapter);
        script.exec(context, scriptable);
        System.out.println(adapter.radius);
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("java");
//        Bindings bindings = new SimpleBindings();
//        bindings.put("a", new A());
//        System.out.println(engine.eval("public class Script { public ScriptTest.A a; public java.lang.Object test() {return a.C;} }", bindings));
    }

    public static class A {
        public int C = 10;
        public int D = 231;
    }
}
