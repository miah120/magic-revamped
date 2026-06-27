package lunar.tinkerer.consequences.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lunar.tinkerer.consequences.Consequence;
import lunar.tinkerer.consequences.ConsequenceEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.item.ItemStack;

public record RunFunction(Identifier function) implements ConsequenceEffect {
    public static MapCodec<RunFunction> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Identifier.CODEC.fieldOf("function").forGetter(RunFunction::function)
        ).apply(i, RunFunction::new)
    );

    @Override
    public MapCodec<? extends ConsequenceEffect> codec() { return CODEC; }

    @Override
    public ItemStack apply(Consequence.RunInfo info) {
        MinecraftServer server = info.world().getServer();
        ServerFunctionManager functions = server.getFunctions();
        functions.get(this.function).ifPresent(function -> {
            CommandSourceStack source = server.createCommandSourceStack()
                .withPermission(LevelBasedPermissionSet.GAMEMASTER)
                .withSuppressedOutput()
                .withEntity(info.player())
                .withLevel(info.world())
                .withPosition(info.player().position())
                .withRotation(info.player().getRotationVector());
            functions.execute(function, source);
        });
        return ItemStack.EMPTY;
    }
}

