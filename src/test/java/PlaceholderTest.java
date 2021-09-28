import thito.fancywaystones.*;

import java.util.concurrent.*;

public class PlaceholderTest {
    public static final VariableContent<C> C_CONTENT = new VariableContent<>(C.class);
    public static void main(String[] args) {
        System.out.println(Util.parseTime("18645"));
//        Placeholder pl = new Placeholder();
//        pl.putContent(C_CONTENT, new C(12, "test"));
//        pl.put("test", p -> p.get(C_CONTENT).test);
//        pl.put("test2", p -> p.get(C_CONTENT).test2);
//        System.out.println(pl.replaceWithNewLines("{test2}this is {test} freakin\n {{test2}} awesome dude {test2}"));
    }

    public static class C {
        int test;
        String test2;

        public C(int test, String test2) {
            this.test = test;
            this.test2 = test2;
        }
    }
}
