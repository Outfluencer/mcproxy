package dev.outfluencer.mcproxy.api.events;

import dev.outfluencer.mcproxy.api.connection.Connection;

public record CompressionChangeEvent(int threshold, Connection connection) {
}
