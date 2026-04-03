package dev.outfluencer.mcproxy.api.events.unsafe;

import io.netty.channel.Channel;

public record ChannelInitializedEvent(Channel channel, Type type) {
    public enum Type {
        FRONTEND, BACKEND
    }
}
