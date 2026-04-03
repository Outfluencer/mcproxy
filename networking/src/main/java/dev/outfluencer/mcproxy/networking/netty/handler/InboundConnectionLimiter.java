package dev.outfluencer.mcproxy.networking.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * This class is only added to tha ServerBootstrap handler, it always runs on one thread so we can
 * add connection throttling that does not require sync here.
 */
public class InboundConnectionLimiter extends ChannelInboundHandlerAdapter {

    private final int maxPerWindow;
    private final long windowMillis;
    private final Map<InetAddress, long[]> connections = new HashMap<>();

    public InboundConnectionLimiter(int maxPerWindow, long windowMillis) {
        this.maxPerWindow = maxPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
        ctx.executor().scheduleAtFixedRate(() -> {
            long expireBefore = System.currentTimeMillis() - windowMillis;
            connections.values().removeIf(v -> v[1] < expireBefore);
        }, 5, 5, TimeUnit.SECONDS);
    }

    private boolean checkThrottle(InetAddress ip) {
        Map<InetAddress, long[]> map = connections;
        long[] entry = map.get(ip);

        if (entry != null && ++entry[0] <= maxPerWindow) {
            return true;
        }

        long now = System.currentTimeMillis();

        if (entry == null) {
            map.put(ip, new long[]{1, now});
        } else if (now - entry[1] > windowMillis) {
            entry[0] = 1;
            entry[1] = now;
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Channel channel && channel.remoteAddress() instanceof InetSocketAddress address) {
            if (!checkThrottle(address.getAddress())) {
                channel.unsafe().closeForcibly();
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
