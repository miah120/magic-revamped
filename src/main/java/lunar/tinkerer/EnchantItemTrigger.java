package lunar.tinkerer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class EnchantItemTrigger extends SimpleCriterionTrigger<EnchantItemTrigger.TriggerInstance> {
    @Override
    public Codec<EnchantItemTrigger.TriggerInstance> codec() {
        return EnchantItemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(
        final ServerPlayer player,
        boolean success,
        boolean decorationPresent,
        boolean changeMade
    ) {
        this.trigger(player, t -> t.matches(success, decorationPresent, changeMade));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<Boolean> success,
        Optional<Boolean> decorationPresent,
        Optional<Boolean> changeMade
    )
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<EnchantItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EnchantItemTrigger.TriggerInstance::player),
                    Codec.BOOL.optionalFieldOf("success").forGetter(EnchantItemTrigger.TriggerInstance::success),
                    Codec.BOOL.optionalFieldOf("decoration_present").forGetter(EnchantItemTrigger.TriggerInstance::decorationPresent),
                    Codec.BOOL.optionalFieldOf("change_made").forGetter(EnchantItemTrigger.TriggerInstance::changeMade)
                )
                .apply(i, EnchantItemTrigger.TriggerInstance::new)
        );

        public boolean matches(
            boolean success,
            boolean decorationPresent,
            boolean changeMade
        ) {
            return this.success.map(s -> s == success).orElse(true)
                && this.decorationPresent.map(s -> s == decorationPresent).orElse(true)
                && this.changeMade.map(s -> s == changeMade).orElse(true);
        }
    }
}
