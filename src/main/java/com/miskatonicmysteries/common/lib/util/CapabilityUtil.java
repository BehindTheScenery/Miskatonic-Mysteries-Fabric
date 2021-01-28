package com.miskatonicmysteries.common.lib.util;

import com.miskatonicmysteries.common.feature.Affiliation;
import com.miskatonicmysteries.common.feature.interfaces.Affiliated;
import com.miskatonicmysteries.common.feature.interfaces.Ascendant;
import com.miskatonicmysteries.common.feature.interfaces.Sanity;
import com.miskatonicmysteries.common.feature.interfaces.SpellCaster;
import com.miskatonicmysteries.common.lib.Constants;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import javax.annotation.Nullable;
import java.util.Optional;

public class CapabilityUtil {
    public static void resetProgress(PlayerEntity player) {
        Sanity.of(player).ifPresent(sanity -> {
            sanity.getSanityCapExpansions().keySet().forEach(sanity::removeSanityCapExpansion);
            sanity.setSanity(sanity.getMaxSanity(), true);
            sanity.setShocked(true);
            sanity.syncSanityData();
        });
        SpellCaster.of(player).ifPresent(caster -> {
            caster.getSpells().clear();
            caster.getLearnedMediums().clear();
            caster.getLearnedEffects().clear();
            caster.setMaxSpells(0);
            caster.setPowerPool(0);
            caster.syncSpellData();
        });
        Ascendant.of(player).ifPresent(ascendant -> {
            ascendant.setStage(0);
        });
    }

    public static void guaranteePower(int power, SpellCaster caster) {
        if (caster.getPowerPool() < power) {
            caster.setPowerPool(power);
        }
    }

    public static boolean isAffiliated(Entity entity) {
        return entity instanceof Affiliated && ((Affiliated) entity).getAffiliation(true) != Affiliation.NONE;
    }

    public static Affiliation getAffiliation(Entity entity, boolean apparent) {
        return isAffiliated(entity) ? ((Affiliated) entity).getAffiliation(apparent) : Affiliation.NONE;
    }

    public static boolean shouldRecognizeAffiliation(Entity entity) {
        return isAffiliated(entity) && ((Affiliated) entity).isSupernatural();
    }

    public static CompoundTag writeSpellData(SpellCaster caster, CompoundTag tag) {
        ListTag spells = new ListTag();
        caster.getSpells().forEach((spell) -> spells.add(spell == null ? new CompoundTag() : spell.toTag(new CompoundTag())));
        ListTag effects = new ListTag();
        caster.getLearnedEffects().forEach((effect) -> effects.add(StringTag.of(effect.getId().toString())));
        ListTag mediums = new ListTag();
        caster.getLearnedMediums().forEach((medium) -> mediums.add(StringTag.of(medium.getId().toString())));
        tag.put(Constants.NBT.SPELL_LIST, spells);
        tag.put(Constants.NBT.SPELL_EFFECTS, effects);
        tag.put(Constants.NBT.SPELL_MEDIUMS, mediums);
        return tag;
    }

    public static int getStage(PlayerEntity player) {
        Optional<Ascendant> ascendant = Ascendant.of(player);
        return ascendant.map(Ascendant::getStage).orElse(0);
    }

    public static Affiliation getApparentAffiliationFromEquipment(@Nullable ItemStack exclude, PlayerEntity player) {
        Inventory trinkets = TrinketsApi.getTrinketsInventory(player);
        for (int i = 0; i < trinkets.size(); i++) {
            ItemStack stack = trinkets.getStack(i);
            if (stack.equals(exclude)) {
                continue;
            }
            if (stack.getItem() instanceof Affiliated) {
                return ((Affiliated) stack.getItem()).getAffiliation(true);
            }
        }
        return Affiliation.NONE;
    }
}
