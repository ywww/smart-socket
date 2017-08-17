package net.vinote.smart.socket.transport.aio;

import net.vinote.smart.socket.enums.IoSessionStatusEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;

class ReadCompletionHandler implements CompletionHandler<Integer, AioSession> {
    private Logger logger = LogManager.getLogger(ReadCompletionHandler.class);


    @Override
    public void completed(Integer result, AioSession aioSession) {
        if (result == -1) {
            aioSession.close();
            return;
        }

        ByteBuffer readBuffer = aioSession.readBuffer;
        readBuffer.flip();
        aioSession.read(readBuffer);

        //会话已不可用,终止读
        if (aioSession.getStatus() != IoSessionStatusEnum.ENABLED) {
            return;
        }
        //数据读取完毕
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {// 仅当发生数据读取时调用compact,减少内存拷贝
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }
        if (aioSession.isServer) {
            if (aioSession.writeCacheQueue.size() < AioSession.FLOW_LIMIT_LINE) {
                aioSession.registerReadHandler(false);
            } else {
                aioSession.flowLimit.set(true);
                aioSession.readSemaphore.release();
            }
        } else {
            aioSession.registerReadHandler(false);
        }
    }

    @Override
    public void failed(Throwable exc, AioSession aioSession) {
        if (exc instanceof AsynchronousCloseException) {
            logger.debug(exc);
        } else {
            exc.printStackTrace();
        }
        aioSession.close();

    }
}