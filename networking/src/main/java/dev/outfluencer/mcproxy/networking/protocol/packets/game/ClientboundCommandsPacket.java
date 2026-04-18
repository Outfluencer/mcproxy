package dev.outfluencer.mcproxy.networking.protocol.packets.game;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.outfluencer.mcproxy.networking.protocol.packets.Packet;
import dev.outfluencer.mcproxy.networking.protocol.registry.MinecraftVersion;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientboundCommandsPacket extends Packet<ClientboundGamePacketListener> {

    private static final int MASK_NODE_TYPE = 0x03;
    private static final int BIT_EXECUTABLE = 0x04;
    private static final int BIT_HAS_REDIRECT = 0x08;
    private static final int BIT_HAS_SUGGESTION = 0x10;

    private static final int TYPE_ROOT = 0;
    private static final int TYPE_LITERAL = 1;
    private static final int TYPE_ARGUMENT = 2;

    private static final Command<?> NOOP_COMMAND = _ -> 0;

    @SuppressWarnings({"rawtypes"})
    private RootCommandNode root;

    @Override
    public void read(ByteBuf buf, int protocolVersion) {
        int count = readVarInt(buf);
        ParsedNode[] parsedNodes = new ParsedNode[count];

        for (int i = 0; i < count; i++) {
            parsedNodes[i] = readSingleNode(buf, protocolVersion);
        }

        int rootIdx = readVarInt(buf);
        root = buildCommandTree(parsedNodes, rootIdx);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ParsedNode readSingleNode(ByteBuf buf, int protocolVersion) {
        byte flags = buf.readByte();
        int[] childIndices = readVarIntArray(buf);
        int redirectTarget = (flags & BIT_HAS_REDIRECT) != 0 ? readVarInt(buf) : -1;

        ArgumentBuilder<?, ?> builder = switch (flags & MASK_NODE_TYPE) {
            case TYPE_ROOT -> null;
            case TYPE_LITERAL -> LiteralArgumentBuilder.literal(readString(buf));
            case TYPE_ARGUMENT -> {
                String argName = readString(buf);
                RequiredArgumentBuilder argBuilder = RequiredArgumentBuilder.argument(argName, ArgumentCodec.deserialize(buf, protocolVersion));
                if ((flags & BIT_HAS_SUGGESTION) != 0) {
                    argBuilder.suggests(CompletionProviders.byIdentifier(readString(buf)));
                }
                yield argBuilder;
            }
            default -> throw new IllegalStateException("Invalid node type: " + (flags & MASK_NODE_TYPE));
        };

        return new ParsedNode(builder, flags, redirectTarget, childIndices);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RootCommandNode buildCommandTree(ParsedNode[] nodes, int rootIdx) {
        CommandNode[] built = new CommandNode[nodes.length];

        // resolve nodes iteratively until all are built (handles forward references)
        boolean progress;
        do {
            progress = false;
            for (int i = 0; i < nodes.length; i++) {
                if (built[i] != null) {
                    continue;
                }

                ParsedNode pn = nodes[i];

                // root nodes need no dependencies
                if (pn.builder == null) {
                    built[i] = new RootCommandNode<>();
                    progress = true;
                    continue;
                }

                // if this node redirects, the target must be built first
                if (pn.redirectTarget >= 0 && built[pn.redirectTarget] == null) {
                    continue;
                }

                if (pn.redirectTarget >= 0) {
                    pn.builder.redirect(built[pn.redirectTarget]);
                }
                if ((pn.flags & BIT_EXECUTABLE) != 0) {
                    pn.builder.executes(NOOP_COMMAND);
                }
                built[i] = pn.builder.build();
                progress = true;
            }
        } while (progress);

        // attach children (a second pass so all nodes exist)
        for (int i = 0; i < nodes.length; i++) {
            if (built[i] == null) {
                throw new IllegalStateException("Command tree contains unresolvable nodes");
            }
            for (int childIdx : nodes[i].childIndices) {
                if (!(built[childIdx] instanceof RootCommandNode)) {
                    built[i].addChild(built[childIdx]);
                }
            }
        }

        return (RootCommandNode) built[rootIdx];
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void write(ByteBuf buf, int protocolVersion) {
        // assign each node a sequential index via BFS
        Map<CommandNode, Integer> nodeIndices = new LinkedHashMap<>();
        Deque<CommandNode> traversal = new ArrayDeque<>();
        traversal.add(root);

        while (!traversal.isEmpty()) {
            CommandNode node = traversal.poll();
            if (nodeIndices.containsKey(node)) {
                continue;
            }

            nodeIndices.put(node, nodeIndices.size());
            traversal.addAll(node.getChildren());
            if (node.getRedirect() != null) {
                traversal.add(node.getRedirect());
            }
        }

        writeVarInt(nodeIndices.size(), buf);

        for (Map.Entry<CommandNode, Integer> entry : nodeIndices.entrySet()) {
            writeSingleNode(entry.getKey(), nodeIndices, buf, protocolVersion);
        }

        writeVarInt(nodeIndices.get(root), buf);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeSingleNode(CommandNode node, Map<CommandNode, Integer> indices, ByteBuf buf, int protocolVersion) {
        byte flags = 0;
        if (node.getRedirect() != null) {
            flags |= BIT_HAS_REDIRECT;
        }
        if (node.getCommand() != null) {
            flags |= BIT_EXECUTABLE;
        }

        switch (node) {
            case RootCommandNode _ -> flags |= TYPE_ROOT;
            case LiteralCommandNode _ -> flags |= TYPE_LITERAL;
            case ArgumentCommandNode argNode -> {
                flags |= TYPE_ARGUMENT;
                if (argNode.getCustomSuggestions() != null) {
                    flags |= BIT_HAS_SUGGESTION;
                }
            }
            default -> throw new IllegalStateException("Unknown node type: " + node);
        }

        buf.writeByte(flags);

        Collection<CommandNode> children = node.getChildren();
        writeVarInt(children.size(), buf);
        for (CommandNode child : children) {
            writeVarInt(indices.get(child), buf);
        }

        if (node.getRedirect() != null) {
            writeVarInt(indices.get(node.getRedirect()), buf);
        }

        if (node instanceof LiteralCommandNode literal) {
            writeString(literal.getLiteral(), buf);
        } else if (node instanceof ArgumentCommandNode argNode) {
            writeString(argNode.getName(), buf);
            ArgumentCodec.serialize(argNode.getType(), buf, protocolVersion);
            if (argNode.getCustomSuggestions() != null) {
                writeString(CompletionProviders.toIdentifier(argNode.getCustomSuggestions()), buf);
            }
        }
    }

    @Override
    public boolean handle(ClientboundGamePacketListener listener) {
        return listener.handle(this);
    }

    // -- internal data structures --

    @SuppressWarnings({"rawtypes"})
    private record ParsedNode(ArgumentBuilder builder, byte flags, int redirectTarget, int[] childIndices) {
    }

    // -- argument (de)serialization --

    private static final class ArgumentCodec {

        private static final Map<String, ArgCodec<?>> BY_NAME;
        private static final ArgCodec<?>[] TABLE_1_20_5;
        private static final ArgCodec<?>[] TABLE_1_21_5;
        private static final ArgCodec<?>[] TABLE_1_21_6;
        private static final Map<Class<?>, TypedArgCodec<?>> KNOWN_TYPES = new HashMap<>();

        private static final ArgCodec<Void> EMPTY = new ArgCodec<>() {
            @Override
            public Void decode(ByteBuf buf) {
                return null;
            }

            @Override
            public void encode(ByteBuf buf, Void v) {
            }
        };

        private static final ArgCodec<Byte> SINGLE_BYTE = new ArgCodec<>() {
            @Override
            public Byte decode(ByteBuf buf) {
                return buf.readByte();
            }

            @Override
            public void encode(ByteBuf buf, Byte v) {
                buf.writeByte(v);
            }
        };

        private static final ArgCodec<Integer> SINGLE_INT = new ArgCodec<>() {
            @Override
            public Integer decode(ByteBuf buf) {
                return buf.readInt();
            }

            @Override
            public void encode(ByteBuf buf, Integer v) {
                buf.writeInt(v);
            }
        };

        private static final ArgCodec<String> IDENTIFIER_STRING = new ArgCodec<>() {
            @Override
            public String decode(ByteBuf buf) {
                return Packet.readString(buf);
            }

            @Override
            public void encode(ByteBuf buf, String v) {
                Packet.writeString(v, buf);
            }
        };

        private static final ArgCodec<FloatArgumentType> RANGED_FLOAT = new ArgCodec<>() {
            @Override
            public FloatArgumentType decode(ByteBuf buf) {
                byte bits = buf.readByte();
                float lo = (bits & 1) != 0 ? buf.readFloat() : -Float.MAX_VALUE;
                float hi = (bits & 2) != 0 ? buf.readFloat() : Float.MAX_VALUE;
                return FloatArgumentType.floatArg(lo, hi);
            }

            @Override
            public void encode(ByteBuf buf, FloatArgumentType t) {
                boolean hasLo = t.getMinimum() != -Float.MAX_VALUE;
                boolean hasHi = t.getMaximum() != Float.MAX_VALUE;
                buf.writeByte((hasLo ? 1 : 0) | (hasHi ? 2 : 0));
                if (hasLo) {
                    buf.writeFloat(t.getMinimum());
                }
                if (hasHi) {
                    buf.writeFloat(t.getMaximum());
                }
            }
        };

        private static final ArgCodec<DoubleArgumentType> RANGED_DOUBLE = new ArgCodec<>() {
            @Override
            public DoubleArgumentType decode(ByteBuf buf) {
                byte bits = buf.readByte();
                double lo = (bits & 1) != 0 ? buf.readDouble() : -Double.MAX_VALUE;
                double hi = (bits & 2) != 0 ? buf.readDouble() : Double.MAX_VALUE;
                return DoubleArgumentType.doubleArg(lo, hi);
            }

            @Override
            public void encode(ByteBuf buf, DoubleArgumentType t) {
                boolean hasLo = t.getMinimum() != -Double.MAX_VALUE;
                boolean hasHi = t.getMaximum() != Double.MAX_VALUE;
                buf.writeByte((hasLo ? 1 : 0) | (hasHi ? 2 : 0));
                if (hasLo) {
                    buf.writeDouble(t.getMinimum());
                }
                if (hasHi) {
                    buf.writeDouble(t.getMaximum());
                }
            }
        };

        private static final ArgCodec<IntegerArgumentType> RANGED_INT = new ArgCodec<>() {
            @Override
            public IntegerArgumentType decode(ByteBuf buf) {
                byte bits = buf.readByte();
                int lo = (bits & 1) != 0 ? buf.readInt() : Integer.MIN_VALUE;
                int hi = (bits & 2) != 0 ? buf.readInt() : Integer.MAX_VALUE;
                return IntegerArgumentType.integer(lo, hi);
            }

            @Override
            public void encode(ByteBuf buf, IntegerArgumentType t) {
                boolean hasLo = t.getMinimum() != Integer.MIN_VALUE;
                boolean hasHi = t.getMaximum() != Integer.MAX_VALUE;
                buf.writeByte((hasLo ? 1 : 0) | (hasHi ? 2 : 0));
                if (hasLo) {
                    buf.writeInt(t.getMinimum());
                }
                if (hasHi) {
                    buf.writeInt(t.getMaximum());
                }
            }
        };

        private static final ArgCodec<LongArgumentType> RANGED_LONG = new ArgCodec<>() {
            @Override
            public LongArgumentType decode(ByteBuf buf) {
                byte bits = buf.readByte();
                long lo = (bits & 1) != 0 ? buf.readLong() : Long.MIN_VALUE;
                long hi = (bits & 2) != 0 ? buf.readLong() : Long.MAX_VALUE;
                return LongArgumentType.longArg(lo, hi);
            }

            @Override
            public void encode(ByteBuf buf, LongArgumentType t) {
                boolean hasLo = t.getMinimum() != Long.MIN_VALUE;
                boolean hasHi = t.getMaximum() != Long.MAX_VALUE;
                buf.writeByte((hasLo ? 1 : 0) | (hasHi ? 2 : 0));
                if (hasLo) {
                    buf.writeLong(t.getMinimum());
                }
                if (hasHi) {
                    buf.writeLong(t.getMaximum());
                }
            }
        };

        private static final TypedArgCodec<StringArgumentType> STRING_CODEC = new TypedArgCodec<>(5, "brigadier:string") {
            @Override
            public StringArgumentType decode(ByteBuf buf) {
                return switch (readVarInt(buf)) {
                    case 0 -> StringArgumentType.word();
                    case 1 -> StringArgumentType.string();
                    case 2 -> StringArgumentType.greedyString();
                    default -> throw new IllegalStateException("Unknown string argument mode");
                };
            }

            @Override
            public void encode(ByteBuf buf, StringArgumentType t) {
                writeVarInt(t.getType().ordinal(), buf);
            }
        };

        static {
            ImmutableMap.Builder<String, ArgCodec<?>> builder = ImmutableMap.builder();
            builder.put("brigadier:bool", EMPTY);
            builder.put("brigadier:float", RANGED_FLOAT);
            builder.put("brigadier:double", RANGED_DOUBLE);
            builder.put("brigadier:integer", RANGED_INT);
            builder.put("brigadier:long", RANGED_LONG);
            builder.put("brigadier:string", STRING_CODEC);
            builder.put("minecraft:entity", SINGLE_BYTE);
            builder.put("minecraft:game_profile", EMPTY);
            builder.put("minecraft:block_pos", EMPTY);
            builder.put("minecraft:column_pos", EMPTY);
            builder.put("minecraft:vec3", EMPTY);
            builder.put("minecraft:vec2", EMPTY);
            builder.put("minecraft:block_state", EMPTY);
            builder.put("minecraft:block_predicate", EMPTY);
            builder.put("minecraft:item_stack", EMPTY);
            builder.put("minecraft:item_predicate", EMPTY);
            builder.put("minecraft:color", EMPTY);
            builder.put("minecraft:component", EMPTY);
            builder.put("minecraft:message", EMPTY);
            builder.put("minecraft:nbt_compound_tag", EMPTY);
            builder.put("minecraft:nbt_tag", EMPTY);
            builder.put("minecraft:nbt_path", EMPTY);
            builder.put("minecraft:objective", EMPTY);
            builder.put("minecraft:objective_criteria", EMPTY);
            builder.put("minecraft:operation", EMPTY);
            builder.put("minecraft:particle", EMPTY);
            builder.put("minecraft:angle", EMPTY);
            builder.put("minecraft:rotation", EMPTY);
            builder.put("minecraft:scoreboard_slot", EMPTY);
            builder.put("minecraft:score_holder", SINGLE_BYTE);
            builder.put("minecraft:swizzle", EMPTY);
            builder.put("minecraft:team", EMPTY);
            builder.put("minecraft:item_slot", EMPTY);
            builder.put("minecraft:resource_location", EMPTY);
            builder.put("minecraft:mob_effect", EMPTY);
            builder.put("minecraft:function", EMPTY);
            builder.put("minecraft:entity_anchor", EMPTY);
            builder.put("minecraft:int_range", EMPTY);
            builder.put("minecraft:float_range", EMPTY);
            builder.put("minecraft:item_enchantment", EMPTY);
            builder.put("minecraft:entity_summon", EMPTY);
            builder.put("minecraft:dimension", EMPTY);
            builder.put("minecraft:time", EMPTY);
            builder.put("minecraft:resource_or_tag", IDENTIFIER_STRING);
            builder.put("minecraft:resource", IDENTIFIER_STRING);
            builder.put("minecraft:uuid", EMPTY);
            builder.put("minecraft:nbt", EMPTY);
            BY_NAME = builder.build();

            KNOWN_TYPES.put(StringArgumentType.class, STRING_CODEC);

            // @formatter:off
            TABLE_1_20_5 = new ArgCodec<?>[] {
                EMPTY,              // brigadier:bool
                RANGED_FLOAT,       // brigadier:float
                RANGED_DOUBLE,      // brigadier:double
                RANGED_INT,         // brigadier:integer
                RANGED_LONG,        // brigadier:long
                STRING_CODEC,       // brigadier:string
                SINGLE_BYTE,        // minecraft:entity
                EMPTY,              // minecraft:game_profile
                EMPTY,              // minecraft:block_pos
                EMPTY,              // minecraft:column_pos
                EMPTY,              // minecraft:vec3
                EMPTY,              // minecraft:vec2
                EMPTY,              // minecraft:block_state
                EMPTY,              // minecraft:block_predicate
                EMPTY,              // minecraft:item_stack
                EMPTY,              // minecraft:item_predicate
                EMPTY,              // minecraft:color
                EMPTY,              // minecraft:component
                EMPTY,              // minecraft:style
                EMPTY,              // minecraft:message
                EMPTY,              // minecraft:nbt_compound_tag
                EMPTY,              // minecraft:nbt_tag
                EMPTY,              // minecraft:nbt_path
                EMPTY,              // minecraft:objective
                EMPTY,              // minecraft:objective_criteria
                EMPTY,              // minecraft:operation
                EMPTY,              // minecraft:particle
                EMPTY,              // minecraft:angle
                EMPTY,              // minecraft:rotation
                EMPTY,              // minecraft:scoreboard_slot
                SINGLE_BYTE,        // minecraft:score_holder
                EMPTY,              // minecraft:swizzle
                EMPTY,              // minecraft:team
                EMPTY,              // minecraft:item_slot
                EMPTY,              // minecraft:item_slots
                EMPTY,              // minecraft:resource_location
                EMPTY,              // minecraft:function
                EMPTY,              // minecraft:entity_anchor
                EMPTY,              // minecraft:int_range
                EMPTY,              // minecraft:float_range
                EMPTY,              // minecraft:dimension
                EMPTY,              // minecraft:gamemode
                SINGLE_INT,         // minecraft:time
                IDENTIFIER_STRING,  // minecraft:resource_or_tag
                IDENTIFIER_STRING,  // minecraft:resource_or_tag_key
                IDENTIFIER_STRING,  // minecraft:resource
                IDENTIFIER_STRING,  // minecraft:resource_key
                EMPTY,              // minecraft:template_mirror
                EMPTY,              // minecraft:template_rotation
                EMPTY,              // minecraft:heightmap
                EMPTY,              // minecraft:loot_table
                EMPTY,              // minecraft:loot_predicate
                EMPTY,              // minecraft:loot_modifier
                EMPTY,              // minecraft:uuid
            };

            TABLE_1_21_5 = new ArgCodec<?>[] {
                EMPTY,              // brigadier:bool
                RANGED_FLOAT,       // brigadier:float
                RANGED_DOUBLE,      // brigadier:double
                RANGED_INT,         // brigadier:integer
                RANGED_LONG,        // brigadier:long
                STRING_CODEC,       // brigadier:string
                SINGLE_BYTE,        // minecraft:entity
                EMPTY,              // minecraft:game_profile
                EMPTY,              // minecraft:block_pos
                EMPTY,              // minecraft:column_pos
                EMPTY,              // minecraft:vec3
                EMPTY,              // minecraft:vec2
                EMPTY,              // minecraft:block_state
                EMPTY,              // minecraft:block_predicate
                EMPTY,              // minecraft:item_stack
                EMPTY,              // minecraft:item_predicate
                EMPTY,              // minecraft:color
                EMPTY,              // minecraft:component
                EMPTY,              // minecraft:style
                EMPTY,              // minecraft:message
                EMPTY,              // minecraft:nbt_compound_tag
                EMPTY,              // minecraft:nbt_tag
                EMPTY,              // minecraft:nbt_path
                EMPTY,              // minecraft:objective
                EMPTY,              // minecraft:objective_criteria
                EMPTY,              // minecraft:operation
                EMPTY,              // minecraft:particle
                EMPTY,              // minecraft:angle
                EMPTY,              // minecraft:rotation
                EMPTY,              // minecraft:scoreboard_slot
                SINGLE_BYTE,        // minecraft:score_holder
                EMPTY,              // minecraft:swizzle
                EMPTY,              // minecraft:team
                EMPTY,              // minecraft:item_slot
                EMPTY,              // minecraft:item_slots
                EMPTY,              // minecraft:resource_location
                EMPTY,              // minecraft:function
                EMPTY,              // minecraft:entity_anchor
                EMPTY,              // minecraft:int_range
                EMPTY,              // minecraft:float_range
                EMPTY,              // minecraft:dimension
                EMPTY,              // minecraft:gamemode
                SINGLE_INT,         // minecraft:time
                IDENTIFIER_STRING,  // minecraft:resource_or_tag
                IDENTIFIER_STRING,  // minecraft:resource_or_tag_key
                IDENTIFIER_STRING,  // minecraft:resource
                IDENTIFIER_STRING,  // minecraft:resource_key
                IDENTIFIER_STRING,  // minecraft:resource_selector
                EMPTY,              // minecraft:template_mirror
                EMPTY,              // minecraft:template_rotation
                EMPTY,              // minecraft:heightmap
                EMPTY,              // minecraft:loot_table
                EMPTY,              // minecraft:loot_predicate
                EMPTY,              // minecraft:loot_modifier
                EMPTY,              // minecraft:uuid
            };

            TABLE_1_21_6 = new ArgCodec<?>[] {
                EMPTY,              // brigadier:bool
                RANGED_FLOAT,       // brigadier:float
                RANGED_DOUBLE,      // brigadier:double
                RANGED_INT,         // brigadier:integer
                RANGED_LONG,        // brigadier:long
                STRING_CODEC,       // brigadier:string
                SINGLE_BYTE,        // minecraft:entity
                EMPTY,              // minecraft:game_profile
                EMPTY,              // minecraft:block_pos
                EMPTY,              // minecraft:column_pos
                EMPTY,              // minecraft:vec3
                EMPTY,              // minecraft:vec2
                EMPTY,              // minecraft:block_state
                EMPTY,              // minecraft:block_predicate
                EMPTY,              // minecraft:item_stack
                EMPTY,              // minecraft:item_predicate
                EMPTY,              // minecraft:color
                EMPTY,              // minecraft:hex_color
                EMPTY,              // minecraft:component
                EMPTY,              // minecraft:style
                EMPTY,              // minecraft:message
                EMPTY,              // minecraft:nbt_compound_tag
                EMPTY,              // minecraft:nbt_tag
                EMPTY,              // minecraft:nbt_path
                EMPTY,              // minecraft:objective
                EMPTY,              // minecraft:objective_criteria
                EMPTY,              // minecraft:operation
                EMPTY,              // minecraft:particle
                EMPTY,              // minecraft:angle
                EMPTY,              // minecraft:rotation
                EMPTY,              // minecraft:scoreboard_slot
                SINGLE_BYTE,        // minecraft:score_holder
                EMPTY,              // minecraft:swizzle
                EMPTY,              // minecraft:team
                EMPTY,              // minecraft:item_slot
                EMPTY,              // minecraft:item_slots
                EMPTY,              // minecraft:resource_location
                EMPTY,              // minecraft:function
                EMPTY,              // minecraft:entity_anchor
                EMPTY,              // minecraft:int_range
                EMPTY,              // minecraft:float_range
                EMPTY,              // minecraft:dimension
                EMPTY,              // minecraft:gamemode
                SINGLE_INT,         // minecraft:time
                IDENTIFIER_STRING,  // minecraft:resource_or_tag
                IDENTIFIER_STRING,  // minecraft:resource_or_tag_key
                IDENTIFIER_STRING,  // minecraft:resource
                IDENTIFIER_STRING,  // minecraft:resource_key
                IDENTIFIER_STRING,  // minecraft:resource_selector
                EMPTY,              // minecraft:template_mirror
                EMPTY,              // minecraft:template_rotation
                EMPTY,              // minecraft:heightmap
                EMPTY,              // minecraft:loot_table
                EMPTY,              // minecraft:loot_predicate
                EMPTY,              // minecraft:loot_modifier
                EMPTY,              // minecraft:dialog
                EMPTY,              // minecraft:uuid
            };
            // @formatter:on
        }

        private static ArgCodec<?>[] tableForVersion(int protocolVersion) {
            if (protocolVersion >= MinecraftVersion.V1_21_6) {
                return TABLE_1_21_6;
            }
            if (protocolVersion >= MinecraftVersion.V1_21_5) {
                return TABLE_1_21_5;
            }
            if (protocolVersion >= MinecraftVersion.V1_20_5) {
                return TABLE_1_20_5;
            }
            throw new UnsupportedOperationException("Unsupported protocol version: " + protocolVersion);
        }

        @SuppressWarnings("unchecked")
        static ArgumentType<?> deserialize(ByteBuf buf, int protocolVersion) {
            if (protocolVersion >= MinecraftVersion.V1_19) {
                int id = readVarInt(buf);
                ArgCodec<?> codec = tableForVersion(protocolVersion)[id];
                Object value = codec.decode(buf);
                if (value != null && KNOWN_TYPES.containsKey(value.getClass())) {
                    return (ArgumentType<?>) value;
                }
                return new OpaqueArgument<>(id, (ArgCodec<Object>) codec, value);
            } else {
                String key = readString(buf);
                ArgCodec<?> codec = BY_NAME.get(key);
                if (codec == null) {
                    throw new IllegalArgumentException("Unknown argument type: " + key);
                }
                Object value = codec.decode(buf);
                if (value != null && KNOWN_TYPES.containsKey(value.getClass())) {
                    return (ArgumentType<?>) value;
                }
                return new OpaqueArgument<>(key, (ArgCodec<Object>) codec, value);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        static void serialize(ArgumentType<?> type, ByteBuf buf, int protocolVersion) {
            TypedArgCodec typedCodec = KNOWN_TYPES.get(type.getClass());
            if (typedCodec != null) {
                if (protocolVersion >= MinecraftVersion.V1_19) {
                    writeVarInt(typedCodec.numericId, buf);
                } else {
                    writeString(typedCodec.stringId, buf);
                }
                typedCodec.encode(buf, type);
                return;
            }

            if (!(type instanceof OpaqueArgument<?>(Object id, ArgCodec<?> codec, Object payload))) {
                throw new IllegalArgumentException("Cannot serialize unknown argument type: " + type.getClass());
            }

            if (id instanceof Integer numId) {
                writeVarInt(numId, buf);
            } else {
                writeString((String) id, buf);
            }
            @SuppressWarnings("unchecked") ArgCodec<Object> rawCodec = (ArgCodec<Object>) codec;
            rawCodec.encode(buf, payload);
        }

        private record OpaqueArgument<T>(Object id, ArgCodec<T> codec, T payload) implements ArgumentType<T> {
            @Override
            public T parse(StringReader reader) {
                throw new UnsupportedOperationException();
            }
        }

        private abstract static class ArgCodec<T> {
            abstract T decode(ByteBuf buf);

            abstract void encode(ByteBuf buf, T value);
        }

        private abstract static class TypedArgCodec<T> extends ArgCodec<T> {
            final int numericId;
            final String stringId;

            TypedArgCodec(int numericId, String stringId) {
                this.numericId = numericId;
                this.stringId = stringId;
            }
        }
    }

    // -- completion/suggestion provider handling --

    public static final class CompletionProviders {

        public static final SuggestionProvider<?> ASK_SERVER = new IdentifiedSuggestionProvider("minecraft:ask_server");

        private static final Map<String, SuggestionProvider<?>> REGISTRY = new HashMap<>();

        static {
            REGISTRY.put("minecraft:ask_server", ASK_SERVER);
            for (String id : List.of("minecraft:all_recipes", "minecraft:available_sounds", "minecraft:available_biomes", "minecraft:summonable_entities")) {
                REGISTRY.put(id, new IdentifiedSuggestionProvider(id));
            }
        }

        static SuggestionProvider<?> byIdentifier(String id) {
            SuggestionProvider<?> provider = REGISTRY.get(id);
            if (provider == null) {
                throw new IllegalArgumentException("Unknown suggestion provider: " + id);
            }
            return provider;
        }

        static String toIdentifier(SuggestionProvider<?> provider) {
            if (provider instanceof IdentifiedSuggestionProvider(String identifier)) {
                return identifier;
            }
            throw new IllegalArgumentException("Cannot determine identifier for provider: " + provider);
        }

        public record IdentifiedSuggestionProvider(String identifier) implements SuggestionProvider<Object> {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> ctx, SuggestionsBuilder builder) {
                return builder.buildFuture();
            }
        }
    }
}
