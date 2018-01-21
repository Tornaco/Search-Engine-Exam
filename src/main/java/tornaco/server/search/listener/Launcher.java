package tornaco.server.search.listener;

public class Launcher {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.gecko.driver", "C:\\firefoxdriver\\geckodriver.exe");
        new RequestListener().start();
    }
}
