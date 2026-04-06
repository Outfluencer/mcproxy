package dev.outfluencer.mcproxy.networking.protocol.registry;

import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class PacketRegistry {
    private final Protocol protocol;
    private final TreeMap<Integer, Map<Integer, Supplier<? extends Packet>>> idToFactory = new TreeMap<>();
    private final TreeMap<Integer, Map<Class<? extends Packet>, Integer>> classToId = new TreeMap<>();

    // per-version set of packet classes that have an explicit mapping at that version
    // used to know which IDs are "owned" by a version vs inherited from older versions
    private final TreeMap<Integer, Map<Class<? extends Packet>, Supplier<? extends Packet>>> registeredAt = new TreeMap<>();

    /**
     * Registers a packet with a per-version packet ID map.
     * Use {@link Protocol#map(int, int)} to build the map.
     */
    public <P extends Packet> void registerPacket(Class<P> packetClass, Supplier<P> factory, Protocol.Mapping... mappings) {
        for (Protocol.Mapping mapping : mappings) {
            idToFactory.computeIfAbsent(mapping.protocolVersion(), _ -> new HashMap<>()).put(mapping.packetId(), factory);
            classToId.computeIfAbsent(mapping.protocolVersion(), _ -> new HashMap<>()).put(packetClass, mapping.packetId());
            registeredAt.computeIfAbsent(mapping.protocolVersion(), _ -> new HashMap<>()).put(packetClass, factory);
        }
    }

    /**
     * Creates a new packet instance for the given protocol version and packet ID.
     * Searches down through versions for the packet class registered at that ID,
     * but only if that class doesn't have a NEWER mapping that would change its ID.
     */
    public Packet createPacket(int protocolVersion, int packetId) {
        for (Integer key = idToFactory.floorKey(protocolVersion); key != null; key = idToFactory.lowerKey(key)) {
            Supplier<? extends Packet> factory = idToFactory.get(key).get(packetId);
            if (factory == null) continue;

            // found a factory at this version for this ID — but check if a newer version
            // remapped this same packet class to a different ID (which means this ID
            // is stale and now belongs to a different packet or nothing)
            Integer packetClassId = getPacketId(protocolVersion, getPacketClass(key, packetId));
            if (packetClassId != null && packetClassId == packetId) {
                return factory.get();
            }
            // the packet class was remapped to a different ID at a newer version, skip
        }
        return null;
    }

    /**
     * Finds the packet class registered at a specific version and ID.
     */
    private Class<? extends Packet> getPacketClass(int version, int packetId) {
        Map<Class<? extends Packet>, Integer> map = classToId.get(version);
        if (map == null) return null;
        for (var entry : map.entrySet()) {
            if (entry.getValue() == packetId) return entry.getKey();
        }
        return null;
    }

    /**
     * Returns the packet ID for the given class and protocol version, or -1 if not registered.
     * Falls back to the mapping of the closest older registered version.
     */
    public int getPacketId(int protocolVersion, Class<? extends Packet> packetClass) {
        for (Integer key = classToId.floorKey(protocolVersion); key != null; key = classToId.lowerKey(key)) {
            Integer id = classToId.get(key).get(packetClass);
            if (id != null) return id;
        }
        return -1;
    }

    public boolean hasPacket(int protocolVersion, int packetId) {
        return createPacket(protocolVersion, packetId) != null;
    }

    public TreeMap<Integer, Map<Class<? extends Packet>, Integer>> getClassToId() {
        return classToId;
    }
}
