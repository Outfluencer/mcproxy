package dev.outfluencer.mcproxy.networking.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class PipelineUtil {

    private PipelineUtil() {}

    public static boolean isEpoll() {
        return Epoll.isAvailable();
    }

    public static boolean isKQueue() {
        return KQueue.isAvailable();
    }

    public static EventLoopGroup newEventLoopGroup(int threads) {
        if (isEpoll()) return new EpollEventLoopGroup(threads);
        if (isKQueue()) return new KQueueEventLoopGroup(threads);
        return new NioEventLoopGroup(threads);
    }

    public static Class<? extends ServerChannel> serverChannelType() {
        if (isEpoll()) return EpollServerSocketChannel.class;
        if (isKQueue()) return KQueueServerSocketChannel.class;
        return NioServerSocketChannel.class;
    }

    public static Class<? extends Channel> clientChannelType() {
        if (isEpoll()) return EpollSocketChannel.class;
        if (isKQueue()) return KQueueSocketChannel.class;
        return NioSocketChannel.class;
    }
}
