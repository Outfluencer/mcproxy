package dev.outfluencer.mcproxy.networking.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/*
 * This class is only added to tha ServerBootstrap handler, it always runs on one thread so we can
 * add connection throttling that does not require sync here.
 */
public class InboundConnectionLimiter extends ChannelInboundHandlerAdapter {

    private final int maxPerWindow;
    private final long windowMillis;
    private final Map<InetAddress, long[]> connections = new HashMap<>();
    private int acceptCount;

    public InboundConnectionLimiter(int maxPerWindow, long windowMillis) {
        this.maxPerWindow = maxPerWindow;
        this.windowMillis = windowMillis;
    }

    private boolean checkThrottle(InetAddress ip) {
        long[] entry = connections.get(ip);

        if (entry != null && ++entry[0] <= maxPerWindow) {
            return true;
        }

        long now = System.currentTimeMillis();

        if (entry == null) {
            connections.put(ip, new long[] {1, now});
        } else if (now - entry[1] > windowMillis) {
            entry[0] = 1;
            entry[1] = now;
        } else {
            return false;
        }

        if ((++acceptCount & 8191) == 0) {
            long expireBefore = now - windowMillis;
            connections.values().removeIf(v -> v[1] < expireBefore);
        }
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Channel channel && channel.remoteAddress() instanceof InetSocketAddress address) {
            if (!checkThrottle(address.getAddress())) {
                channel.unsafe().closeForcibly();
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
