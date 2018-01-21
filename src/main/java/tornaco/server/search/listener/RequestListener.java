package tornaco.server.search.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.UnsupportedEncodingException;

class RequestListener {

    public RequestListener() {
        prepareWeb();
    }

    void start() throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MessageHandler());
                    }
                });

        // Start the client.
        ChannelFuture ch = bootstrap.bind(8082).sync();

        System.out.println("start server success, you can telnet 127.0.0.1 8082 to send massage for this server");

        // Wait until the connection is closed.
        ch.channel().closeFuture().sync();
    }

    private WebDriver driver;
    private WebElement inputBox, submitButton;

    private final Object lock = new Object();

    private void prepareWeb() {
        driver = new FirefoxDriver();
        driver.get("http://www.baidu.com");
        inputBox = driver.findElement(By.xpath("//*[@id=\"kw\"]"));
        submitButton = driver.findElement(By.xpath("//*[@id=\"su\"]"));
    }

    private void handleMessage(String decodeMsg) {
        System.out.println("handleMessage: " + decodeMsg);
        if (decodeMsg == null || decodeMsg.trim().length() == 0) {
            return;
        }

        synchronized (lock) {
            try {
                inputBox.clear();
                inputBox.sendKeys(decodeMsg);
                submitButton.click();
            } catch (Throwable ignored) {
                try {
                    driver.close();
                } catch (Throwable e) {

                }
                prepareWeb();
            }
        }
    }

    class MessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
            ByteBuf buf = (ByteBuf) msg;

            byte[] bytes = new byte[buf.readableBytes()];
            int readerIndex = buf.readerIndex();
            buf.getBytes(readerIndex, bytes);

            String decodeMsg = new String(bytes);
            handleMessage(decodeMsg);

//            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }
    }
}
