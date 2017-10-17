public class Main {

    private static class Test {
        final String s = test();

        private String test() {
            System.out.println(s.toString());
            return "abc";
        }
    }


    public static void main(String[] args) {
        Test a = new Test();
        System.out.println("heyo");
    }
}
