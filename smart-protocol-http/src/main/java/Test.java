import org.smartboot.socket.protocol.HttpEntity;
import org.smartboot.socket.protocol.HttpProtocol;
import org.smartboot.socket.protocol.HttpServerMessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws ClassNotFoundException {

        // 定义服务器接受的消息类型以及各类消息对应的处理器
//        config.setFilters(new SmartFilter[] { new QuickMonitorTimer<HttpEntity>() });
        HttpServerMessageProcessor processor = new HttpServerMessageProcessor();
        AioQuickServer<HttpEntity> server = new AioQuickServer<HttpEntity>()
                .setThreadNum(8)
                .setProtocol(new HttpProtocol())
                .setProcessor(processor);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
